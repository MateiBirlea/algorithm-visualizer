package com.example.explanationengine.service;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.explanationengine.config.OllamaProperties;

@Component
public class OllamaClient {

    private final RestClient client;
    private final OllamaProperties properties;

    public OllamaClient(OllamaProperties properties) {
        this.properties = properties;
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .build();
    }

    public String chat(String userPrompt) {
        ChatRequest request = ChatRequest.fromPrompts(properties.model(), properties.systemPrompt(), userPrompt);
        ChatResponse response = client.post()
                .uri("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatResponse.class);

        if (response != null && response.message() != null && response.message().content() != null) {
            return response.message().content().trim();
        }
        return "Nu am putut genera explicatia in acest moment.";
    }

    private record ChatRequest(String model, List<Message> messages, boolean stream) {
        static ChatRequest fromPrompts(String model, String system, String user) {
            return new ChatRequest(model,
                    List.of(new Message("system", system), new Message("user", user)),
                    false);
        }
    }

    private record ChatResponse(Message message) {
    }

    private record Message(String role, String content) {
    }
}
