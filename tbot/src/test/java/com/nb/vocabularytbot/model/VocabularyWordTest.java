package com.nb.vocabularytbot.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VocabularyWordTest {

    @Test
    void testVocabularyWordProperties() {
        VocabularyWord word = new VocabularyWord();
        
        // Test setters and getters
        word.setWord("test");
        assertEquals("test", word.getWord());

        word.setTranslation("тест");
        assertEquals("тест", word.getTranslation());

        word.setContext("Test context");
        assertEquals("Test context", word.getContext());

        word.setNotes("Test notes");
        assertEquals("Test notes", word.getNotes());

        word.setLastReviewed(1234567890L);
        assertEquals(1234567890L, word.getLastReviewed());

        word.setReviewCount(5);
        assertEquals(5, word.getReviewCount());
    }
} 