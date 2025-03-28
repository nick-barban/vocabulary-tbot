package com.nb.vocabularytbot;

import org.junit.jupiter.api.Test;
import software.amazon.awscdk.App;
import software.amazon.awscdk.assertions.Template;
import software.amazon.awscdk.assertions.Match;

class VocabularyBotStackTest {

    @Test
    void testStackCreation() {
        // Create the stack
        App app = new App();
        VocabularyBotStack stack = new VocabularyBotStack(app, "TestStack", null);
        Template template = Template.fromStack(stack);

        // Verify DynamoDB table
        template.hasResourceProperties("AWS::DynamoDB::Table", Match.objectLike(Map.of(
                "TableName", "VocabularyTable",
                "BillingMode", "PAY_PER_REQUEST",
                "PointInTimeRecoverySpecification", Match.objectLike(Map.of(
                        "PointInTimeRecoveryEnabled", true
                ))
        )));

        // Verify Lambda function
        template.hasResourceProperties("AWS::Lambda::Function", Match.objectLike(Map.of(
                "Runtime", "java17",
                "Handler", "com.nb.vocabularytbot.handler.LambdaHandler",
                "MemorySize", 512,
                "Timeout", 30
        )));

        // Verify API Gateway
        template.hasResourceProperties("AWS::ApiGateway::RestApi", Match.objectLike(Map.of(
                "Name", "VocabularyBot API"
        )));

        // Verify EventBridge rule
        template.hasResourceProperties("AWS::Events::Rule", Match.objectLike(Map.of(
                "ScheduleExpression", "cron(0 9 * * ? *)"
        )));
    }
} 