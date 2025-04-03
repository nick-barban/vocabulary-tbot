package com.nb.vocabularytbot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awscdk.App;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class VocabularyBotStackTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        // Set up environment variables for testing
        System.setProperty("TELEGRAM_BOT_TOKEN", "test-token");
        System.setProperty("GOOGLE_CLIENT_ID", "test-client-id");
        System.setProperty("GOOGLE_CLIENT_SECRET", "test-client-secret");

        // Create a mock JAR file
        Path mockJar = tempDir.resolve("vocabulary-tbot-1.0-SNAPSHOT.jar");
        Files.createFile(mockJar);
        Files.write(mockJar, "mock jar content".getBytes());

        // Set the JAR path property
        System.setProperty("lambda.jar.path", mockJar.toAbsolutePath().toString());
    }

    @Test
    void testStackCreation() {
        // Create the stack
        App app = new App();
        VocabularyBotStack stack = new VocabularyBotStack(app, "TestStack", null);
        
        // Verify the stack was created successfully
        assert stack != null;
        
        // Get the synthesized CloudFormation template
        Object template = app.synth().getStackArtifact(stack.getArtifactId()).getTemplate();
        String templateStr = template.toString();
        
        // Print the template for debugging
        System.out.println("Template: " + templateStr);
        
        // Verify DynamoDB table
        assert templateStr.contains("AttributeDefinitions") : "AttributeDefinitions not found";
        assert templateStr.contains("BillingMode") : "BillingMode not found";
        assert templateStr.contains("KeySchema") : "KeySchema not found";
        assert templateStr.contains("PointInTimeRecoveryEnabled") : "PointInTimeRecoveryEnabled not found";
        
        // Verify Lambda function
        assert templateStr.contains("Runtime") : "Runtime not found";
        assert templateStr.contains("Handler") : "Handler not found";
        assert templateStr.contains("MemorySize") : "MemorySize not found";
        assert templateStr.contains("Timeout") : "Timeout not found";
        
        // Verify API Gateway
        assert templateStr.contains("RestApi") : "RestApi not found";
        
        // Verify EventBridge rule
        assert templateStr.contains("ScheduleExpression") : "ScheduleExpression not found";
    }
} 