package com.example.explanationengine.service;

import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.explanationengine.config.OllamaProperties;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private static final String CONTROLLED_ERROR_MESSAGE = "Nu am putut genera explicatia in acest moment.";

    private final RestClient client;
    private final OllamaProperties properties;

    public OllamaClient(OllamaProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(120));
        requestFactory.setReadTimeout(Duration.ofSeconds(120));
        this.client = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public String chat(String userPrompt) {
        return chatInternal(userPrompt, false, 180);
    }

    public String chat(String userPrompt, int numPredict) {
        return chatInternal(userPrompt, false, numPredict);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpModel() {
        try {
            String response = chatInternal("Reply only with OK", true, 20);
            if (CONTROLLED_ERROR_MESSAGE.equals(response)) {
                log.warn("Ollama warm-up did not complete successfully.");
            }
        } catch (RuntimeException ex) {
            log.warn("Ollama warm-up failed: {}", ex.getMessage());
        }
    }

    private String chatInternal(String userPrompt, boolean warmUp, int numPredict) {
        ChatRequest request = ChatRequest.fromPrompt(properties.model(), userPrompt, numPredict);
        long start = System.currentTimeMillis();
        log.info("Starting Ollama {}request. model={}", warmUp ? "warm-up " : "", properties.model());
        try {
            ChatResponse response = client.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatResponse.class);

            long end = System.currentTimeMillis();
            log.info("Finished Ollama {}request. durationMs={}", warmUp ? "warm-up " : "", end - start);
            if (response != null && response.message() != null && response.message().content() != null) {
                return response.message().content().trim();
            }
            return CONTROLLED_ERROR_MESSAGE;
        } catch (RestClientException ex) {
            long end = System.currentTimeMillis();
            log.warn("Ollama {}request failed. durationMs={}, error={}", warmUp ? "warm-up " : "", end - start, ex.getMessage());
            return CONTROLLED_ERROR_MESSAGE;
        }
    }

    private record ChatRequest(
            String model,
            List<Message> messages,
            boolean stream,
            @JsonProperty("keep_alive") String keepAlive,
            Options options
    ) {
        static ChatRequest fromPrompt(String model, String user, int numPredict) {
            return new ChatRequest(model,
                    List.of(new Message("user", user)),
                    false,
                    "30m",
                    new Options(0.3, numPredict, 2048));
        }
    }

    private record Options(
            double temperature,
            @JsonProperty("num_predict") int numPredict,
            @JsonProperty("num_ctx") int numCtx
    ) {
    }

    private record ChatResponse(Message message) {
    }

    private record Message(String role, String content) {
    }
}
