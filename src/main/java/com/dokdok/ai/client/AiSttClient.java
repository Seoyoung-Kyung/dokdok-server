package com.dokdok.ai.client;

import com.dokdok.ai.dto.SttRequest;
import com.dokdok.ai.dto.SttResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class AiSttClient {

    private final WebClient aiWebClient;

    @Value("${ai.api.stt-path}")
    private String sttPath;

    public SttResponse requestStt(SttRequest request) {
        return aiWebClient.post()
                .uri(sttPath)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SttResponse.class)
                .block();
    }
}
