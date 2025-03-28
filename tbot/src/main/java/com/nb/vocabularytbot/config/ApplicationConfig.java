package com.nb.vocabularytbot.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
@ConfigurationProperties("app")
public class ApplicationConfig {
    private String telegramBotToken;
    private String googleClientId;
    private String googleClientSecret;
    private int wordsPerDay;
    private String scheduleCron;

    public String getTelegramBotToken() {
        return telegramBotToken;
    }

    public void setTelegramBotToken(String telegramBotToken) {
        this.telegramBotToken = telegramBotToken;
    }

    public String getGoogleClientId() {
        return googleClientId;
    }

    public void setGoogleClientId(String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public String getGoogleClientSecret() {
        return googleClientSecret;
    }

    public void setGoogleClientSecret(String googleClientSecret) {
        this.googleClientSecret = googleClientSecret;
    }

    public int getWordsPerDay() {
        return wordsPerDay;
    }

    public void setWordsPerDay(int wordsPerDay) {
        this.wordsPerDay = wordsPerDay;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }
} 