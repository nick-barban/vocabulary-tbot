package com.nb.vocabularytbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateScopes;
import com.nb.vocabularytbot.config.ApplicationConfig;
import com.nb.vocabularytbot.model.VocabularyWord;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class VocabularyService {
    private final ApplicationConfig config;
    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;
    private final Translate translateService;
    private static final String TABLE_NAME = "VocabularyWords";

    @Inject
    public VocabularyService(ApplicationConfig config, DynamoDbClient dynamoDbClient, ObjectMapper objectMapper) 
            throws GeneralSecurityException, IOException {
        this.config = config;
        this.dynamoDbClient = dynamoDbClient;
        this.objectMapper = objectMapper;
        this.translateService = createTranslateService();
    }

    private Translate createTranslateService() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        GoogleCredential credential = new GoogleCredential()
                .setAccessToken(config.getGoogleClientId())
                .setRefreshToken(config.getGoogleClientSecret())
                .setExpiresInSeconds(3600L);

        credential = credential.createScoped(Collections.singleton(TranslateScopes.CLOUD_TRANSLATION));

        return new Translate.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("VocabularyTBot")
                .build();
    }

    public void processScheduledVocabulary() {
        try {
            // Get all users from DynamoDB
            ScanResponse scanResponse = dynamoDbClient.scan(ScanRequest.builder()
                    .tableName(TABLE_NAME)
                    .build());

            // Process each user's vocabulary
            for (Map<String, AttributeValue> item : scanResponse.items()) {
                long chatId = Long.parseLong(item.get("chatId").s());
                List<VocabularyWord> words = getTodayWords(chatId);
                // TODO: Send words to user via Telegram
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<VocabularyWord> getTodayWords(long chatId) {
        try {
            QueryResponse queryResponse = dynamoDbClient.query(QueryRequest.builder()
                    .tableName(TABLE_NAME)
                    .keyConditionExpression("chatId = :chatId")
                    .filterExpression("lastReviewed < :today")
                    .expressionAttributeValues(Map.of(
                            ":chatId", AttributeValue.builder().s(String.valueOf(chatId)).build(),
                            ":today", AttributeValue.builder().s(String.valueOf(System.currentTimeMillis())).build()
                    ))
                    .limit(config.getWordsPerDay())
                    .build());

            return queryResponse.items().stream()
                    .map(this::mapToVocabularyWord)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private VocabularyWord mapToVocabularyWord(Map<String, AttributeValue> item) {
        VocabularyWord word = new VocabularyWord();
        word.setWord(item.get("word").s());
        word.setTranslation(item.get("translation").s());
        word.setContext(item.get("context").s());
        word.setNotes(item.get("notes").s());
        word.setLastReviewed(Long.parseLong(item.get("lastReviewed").n()));
        word.setReviewCount(Integer.parseInt(item.get("reviewCount").n()));
        return word;
    }

    public void saveWord(long chatId, VocabularyWord word) {
        try {
            Map<String, AttributeValue> item = Map.of(
                    "chatId", AttributeValue.builder().s(String.valueOf(chatId)).build(),
                    "word", AttributeValue.builder().s(word.getWord()).build(),
                    "translation", AttributeValue.builder().s(word.getTranslation()).build(),
                    "context", AttributeValue.builder().s(word.getContext()).build(),
                    "notes", AttributeValue.builder().s(word.getNotes()).build(),
                    "lastReviewed", AttributeValue.builder().s(String.valueOf(word.getLastReviewed())).build(),
                    "reviewCount", AttributeValue.builder().n(String.valueOf(word.getReviewCount())).build()
            );

            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 