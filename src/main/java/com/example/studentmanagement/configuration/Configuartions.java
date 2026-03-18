package com.example.studentmanagement.configuration;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Configuartions {

    @Value("${spring.ai.openai.chat.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.chat.completions-path}")
    private String openAiCompletionsPath;

    @Bean
    public OpenAiApi openAiApiBean(@Value("${spring.ai.openai.api-key}") String apiKey) {
        return OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(apiKey)
                .completionsPath(openAiCompletionsPath)
                .build();
    }
}
