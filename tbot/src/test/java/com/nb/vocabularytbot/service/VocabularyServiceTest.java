package com.nb.vocabularytbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nb.vocabularytbot.config.ApplicationConfig;
import com.nb.vocabularytbot.model.VocabularyWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VocabularyServiceTest {

    @Mock
    private ApplicationConfig config;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DynamoDbClient dynamoDbClient;

    private VocabularyService vocabularyService;

    @BeforeEach
    void setUp() throws GeneralSecurityException, IOException {
        when(config.getWordsPerDay()).thenReturn(10);
        
        // Mock DynamoDB responses
        when(dynamoDbClient.putItem(any(PutItemRequest.class)))
                .thenReturn(PutItemResponse.builder().build());
                
        Map<String, AttributeValue> item = Map.of(
            "chatId", AttributeValue.builder().s("123456789").build(),
            "word", AttributeValue.builder().s("test").build(),
            "translation", AttributeValue.builder().s("тест").build(),
            "context", AttributeValue.builder().s("Test context").build(),
            "notes", AttributeValue.builder().s("Test notes").build(),
            "lastReviewed", AttributeValue.builder().n(String.valueOf(System.currentTimeMillis())).build(),
            "reviewCount", AttributeValue.builder().n("0").build()
        );
                
        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(List.of(item))
                        .build());

        vocabularyService = new VocabularyService(config, dynamoDbClient, objectMapper);
    }

    @Test
    void testSaveAndGetWord() {
        // Prepare test data
        long chatId = 123456789L;
        VocabularyWord word = new VocabularyWord();
        word.setWord("test");
        word.setTranslation("тест");
        word.setContext("Test context");
        word.setNotes("Test notes");
        word.setLastReviewed(System.currentTimeMillis());
        word.setReviewCount(0);

        // Save word
        vocabularyService.saveWord(chatId, word);

        // Get words
        List<VocabularyWord> words = vocabularyService.getTodayWords(chatId);

        // Verify
        assertFalse(words.isEmpty());
        assertEquals(1, words.size());
        VocabularyWord savedWord = words.get(0);
        assertEquals(word.getWord(), savedWord.getWord());
        assertEquals(word.getTranslation(), savedWord.getTranslation());
        assertEquals(word.getContext(), savedWord.getContext());
        assertEquals(word.getNotes(), savedWord.getNotes());
        assertEquals(word.getReviewCount(), savedWord.getReviewCount());
    }
} 