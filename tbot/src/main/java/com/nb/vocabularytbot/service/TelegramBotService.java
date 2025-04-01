package com.nb.vocabularytbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nb.vocabularytbot.config.ApplicationConfig;
import com.nb.vocabularytbot.model.VocabularyWord;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Singleton
public class TelegramBotService extends TelegramLongPollingBot {
    private final ApplicationConfig config;
    private final VocabularyService vocabularyService;
    private final ObjectMapper objectMapper;

    @Inject
    public TelegramBotService(ApplicationConfig config, VocabularyService vocabularyService, ObjectMapper objectMapper) {
        super(config.getTelegramBotToken());
        this.config = config;
        this.vocabularyService = vocabularyService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getBotUsername() {
        return "VocabularyTBot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        handleUpdate(update);
    }

    public void processUpdate(String updateJson) {
        try {
            Update update = objectMapper.readValue(updateJson, Update.class);
            handleUpdate(update);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpdate(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        switch (text) {
            case "/start":
                sendMessage(chatId, "Welcome to VocabularyTBot! Use /help to see available commands.");
                break;
            case "/help":
                sendMessage(chatId, "Available commands:\n" +
                        "/start - Start the bot\n" +
                        "/help - Show this help message\n" +
                        "/words - Show today's vocabulary words\n" +
                        "/settings - Configure your preferences");
                break;
            case "/words":
                List<VocabularyWord> words = vocabularyService.getTodayWords(chatId);
                if (words.isEmpty()) {
                    sendMessage(chatId, "No words for today. Check back later!");
                } else {
                    StringBuilder message = new StringBuilder("Today's vocabulary words:\n\n");
                    for (VocabularyWord word : words) {
                        message.append("📝 ").append(word.getWord())
                                .append(" - ").append(word.getTranslation())
                                .append("\n");
                        if (word.getContext() != null && !word.getContext().isEmpty()) {
                            message.append("Context: ").append(word.getContext()).append("\n");
                        }
                        message.append("\n");
                    }
                    sendMessage(chatId, message.toString());
                }
                break;
            case "/settings":
                sendMessage(chatId, "Settings:\n" +
                        "Words per day: " + config.getWordsPerDay() + "\n" +
                        "Schedule: " + config.getScheduleCron());
                break;
            default:
                sendMessage(chatId, "Unknown command. Use /help to see available commands.");
        }
    }

    private void sendMessage(long chatId, String text) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode("HTML")
                    .build();
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
} 