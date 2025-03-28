package com.nb.vocabularytbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nb.vocabularytbot.config.ApplicationConfig;
import com.nb.vocabularytbot.model.VocabularyWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Testcontainers
class VocabularyServiceTest {

    @Container
    public static GenericContainer<?> dynamoDb = new GenericContainer<>(
            DockerImageName.parse("amazon/dynamodb-local:latest"))
            .withExposedPorts(8000);

    @Mock
    private ApplicationConfig config;

    @Mock
    private ObjectMapper objectMapper;

    private DynamoDbClient dynamoDbClient;
    private VocabularyService vocabularyService;

    @BeforeEach
    void setUp() {
        // Create DynamoDB client pointing to the test container
        dynamoDbClient = DynamoDbClient.builder()
                .endpointOverride(URI.create("http://localhost:" + dynamoDb.getMappedPort(8000)))
                .build();

        // Create table
        createTable();

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

    private void createTable() {
        CreateTableRequest request = CreateTableRequest.builder()
                .tableName("VocabularyWords")
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("chatId")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("word")
                                .keyType(KeyType.RANGE)
                                .build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("chatId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("word")
                                .attributeType(ScalarAttributeType.S)
                                .build()
                )
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(5L)
                                .writeCapacityUnits(5L)
                                .build()
                )
                .build();

        try {
            dynamoDbClient.createTable(request);
        } catch (ResourceInUseException e) {
            // Table already exists
        }
    }
} 