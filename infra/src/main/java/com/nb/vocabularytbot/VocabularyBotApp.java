package com.nb.vocabularytbot;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

public class VocabularyBotApp {
    public static void main(final String[] args) {
        App app = new App();
        
        Environment env = Environment.builder()
                .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                .region(System.getenv("CDK_DEFAULT_REGION"))
                .build();

        new VocabularyBotStack(app, "VocabularyBotStack", StackProps.builder()
                .env(env)
                .build());

        app.synth();
    }
} 