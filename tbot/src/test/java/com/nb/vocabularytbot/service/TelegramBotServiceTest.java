package com.nb.vocabularytbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nb.vocabularytbot.config.ApplicationConfig;
import com.nb.vocabularytbot.model.VocabularyWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    @Mock
    private ApplicationConfig config;

    @Mock
    private VocabularyService vocabularyService;

    @Mock
    private ObjectMapper objectMapper;

    private TelegramBotService telegramBotService;

    @BeforeEach
    void setUp() throws TelegramApiException {
        telegramBotService = spy(new TelegramBotService(config, vocabularyService, objectMapper));
        doReturn(mock(Message.class)).when(telegramBotService).execute(any(SendMessage.class));
    }

    @Test
    void testProcessUpdate() throws Exception {
        // Prepare test data
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(123456789L);
        message.setChat(chat);
        message.setText("/words");
        update.setMessage(message);

        List<VocabularyWord> words = Arrays.asList(
            createVocabularyWord("test1", "тест1"),
            createVocabularyWord("test2", "тест2")
        );

        // Configure mocks
        when(objectMapper.readValue(any(String.class), eq(Update.class))).thenReturn(update);
        when(vocabularyService.getTodayWords(anyLong())).thenReturn(words);

        // Execute
        telegramBotService.processUpdate("{}");

        // Verify
        verify(vocabularyService).getTodayWords(123456789L);
        verify(telegramBotService).execute(any(SendMessage.class));
    }

    @Test
    void testHandleUpdateWithHelpCommand() throws Exception {
        // Prepare test data
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(123456789L);
        message.setChat(chat);
        message.setText("/help");
        update.setMessage(message);

        // Configure mocks
        when(objectMapper.readValue(any(String.class), eq(Update.class))).thenReturn(update);

        // Execute
        telegramBotService.processUpdate("{}");

        // Verify
        verify(vocabularyService, never()).getTodayWords(anyLong());
        verify(telegramBotService).execute(any(SendMessage.class));
    }

    private VocabularyWord createVocabularyWord(String word, String translation) {
        VocabularyWord vocabularyWord = new VocabularyWord();
        vocabularyWord.setWord(word);
        vocabularyWord.setTranslation(translation);
        return vocabularyWord;
    }
} 