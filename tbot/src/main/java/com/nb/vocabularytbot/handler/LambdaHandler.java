package com.nb.vocabularytbot.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nb.vocabularytbot.service.TelegramBotService;
import com.nb.vocabularytbot.service.VocabularyService;
import io.micronaut.context.ApplicationContext;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

public class LambdaHandler implements RequestHandler<Object, Object> {
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    @Inject
    public LambdaHandler(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @Override
    public Object handleRequest(Object input, Context context) {
        try {
            if (input instanceof ScheduledEvent) {
                return handleScheduledEvent((ScheduledEvent) input, context);
            } else if (input instanceof APIGatewayProxyRequestEvent) {
                return handleTelegramUpdate((APIGatewayProxyRequestEvent) input, context);
            }
            throw new IllegalArgumentException("Unsupported event type");
        } catch (Exception e) {
            context.getLogger().log("Error processing request: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private Object handleScheduledEvent(ScheduledEvent event, Context context) {
        VocabularyService vocabularyService = applicationContext.getBean(VocabularyService.class);
        vocabularyService.processScheduledVocabulary();
        return null;
    }

    private APIGatewayProxyResponseEvent handleTelegramUpdate(APIGatewayProxyRequestEvent input, Context context) {
        TelegramBotService botService = applicationContext.getBean(TelegramBotService.class);
        botService.processUpdate(input.getBody());
        
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setStatusCode(200);
        response.setBody("OK");
        return response;
    }
} 