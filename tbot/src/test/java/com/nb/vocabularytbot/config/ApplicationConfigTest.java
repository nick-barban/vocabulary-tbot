package com.nb.vocabularytbot.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigTest {

    @Test
    void testConfigurationProperties() {
        ApplicationConfig config = new ApplicationConfig();
        
        // Test setters and getters
        config.setTelegramBotToken("test-token");
        assertEquals("test-token", config.getTelegramBotToken());

        config.setGoogleClientId("test-client-id");
        assertEquals("test-client-id", config.getGoogleClientId());

        config.setGoogleClientSecret("test-client-secret");
        assertEquals("test-client-secret", config.getGoogleClientSecret());

        config.setWordsPerDay(5);
        assertEquals(5, config.getWordsPerDay());

        config.setScheduleCron("0 9 * * ? *");
        assertEquals("0 9 * * ? *", config.getScheduleCron());
    }
} 