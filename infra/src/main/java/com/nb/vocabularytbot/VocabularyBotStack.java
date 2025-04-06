package com.nb.vocabularytbot;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.s3.assets.AssetOptions;
import software.amazon.awscdk.services.apigateway.*;
import software.amazon.awscdk.services.dynamodb.*;
import software.amazon.awscdk.services.events.*;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.logs.*;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        Function lambdaFunction = Function.Builder.create(this, "VocabularyBotFunction")
                .runtime(software.amazon.awscdk.services.lambda.Runtime.JAVA_17)
                .handler("com.tutorspace.vocabularytbot.handler.LambdaHandler")
                .code(Code.fromAsset("../", AssetOptions.builder()
                        .bundling(BundlingOptions.builder()
                                .command(Arrays.asList(
                                        "/bin/sh",
                                        "-c",
                                        "mvn clean package && cp /asset-input/target/vocabulary-tbot-1.0-SNAPSHOT.jar /asset-output/"
                                ))
                                .image(software.amazon.awscdk.services.lambda.Runtime.JAVA_17.getBundlingImage())
                                .user("root")
                                .outputType(BundlingOutput.ARCHIVED)
                                .build())
                        .build()))
                .memorySize(512)
                .timeout(Duration.seconds(30))
                .environment(new HashMap<>(Map.of(
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
                .targets(List.of(new LambdaFunction(lambdaFunction)))
                .build();

        // Output the API Gateway URL
        CfnOutput.Builder.create(this, "ApiUrl")
                .value(api.getUrl())
                .description("API Gateway URL")
                .build();
    }
} 