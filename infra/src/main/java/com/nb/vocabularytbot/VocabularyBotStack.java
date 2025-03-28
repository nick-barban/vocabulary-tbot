package com.nb.vocabularytbot;

import software.amazon.awscdk.*;
import software.amazon.awscdk.aws_apigateway.*;
import software.amazon.awscdk.aws_dynamodb.*;
import software.amazon.awscdk.aws_events.*;
import software.amazon.awscdk.aws_events_targets.*;
import software.amazon.awscdk.aws_iam.*;
import software.amazon.awscdk.aws_lambda.*;
import software.amazon.awscdk.aws_lambda_java.*;
import software.amazon.awscdk.aws_logs.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class VocabularyBotStack extends Stack {
    public VocabularyBotStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // Create DynamoDB table
        Table vocabularyTable = Table.Builder.create(this, "VocabularyTable")
                .partitionKey(Attribute.builder()
                        .name("chatId")
                        .type(AttributeType.STRING)
                        .build())
                .sortKey(Attribute.builder()
                        .name("word")
                        .type(AttributeType.STRING)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST)
                .removalPolicy(RemovalPolicy.DESTROY)
                .pointInTimeRecovery(true)
                .build();

        // Create Lambda function
        JavaFunction lambdaFunction = JavaFunction.Builder.create(this, "VocabularyBotFunction")
                .runtime(Runtime.JAVA_17)
                .handler("com.tutorspace.vocabularytbot.handler.LambdaHandler")
                .code(Code.fromAsset("../target/vocabulary-tbot-1.0-SNAPSHOT.jar"))
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .environment(new HashMap<>(Map.of(
                        "TELEGRAM_BOT_TOKEN", System.getenv("TELEGRAM_BOT_TOKEN"),
                        "GOOGLE_CLIENT_ID", System.getenv("GOOGLE_CLIENT_ID"),
                        "GOOGLE_CLIENT_SECRET", System.getenv("GOOGLE_CLIENT_SECRET"),
                        "WORDS_PER_DAY", "5",
                        "SCHEDULE_CRON", "0 9 * * ? *" // Every day at 9 AM UTC
                )))
                .logRetention(RetentionDays.ONE_WEEK)
                .build();

        // Grant DynamoDB permissions to Lambda
        vocabularyTable.grantReadWriteData(lambdaFunction);

        // Create API Gateway
        RestApi api = RestApi.Builder.create(this, "VocabularyBotApi")
                .restApiName("VocabularyBot API")
                .description("API for VocabularyBot Telegram integration")
                .deployOptions(StageOptions.builder()
                        .stageName("prod")
                        .build())
                .build();

        // Create API Gateway integration with Lambda
        LambdaIntegration integration = LambdaIntegration.Builder.create(lambdaFunction)
                .proxy(true)
                .build();

        // Add POST method to API Gateway
        api.getRoot().addMethod("POST", integration);

        // Create EventBridge rule for scheduled execution
        Rule scheduledRule = Rule.Builder.create(this, "ScheduledVocabularyRule")
                .schedule(Schedule.expression("cron(0 9 * * ? *)")) // Every day at 9 AM UTC
                .targets(List.of(LambdaFunction.Builder.create(lambdaFunction).build()))
                .build();

        // Output the API Gateway URL
        CfnOutput.Builder.create(this, "ApiUrl")
                .value(api.getUrl())
                .description("API Gateway URL")
                .build();
    }
} 