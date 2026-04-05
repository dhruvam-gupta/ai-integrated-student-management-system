package com.example.studentmanagement.configuration;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;

@Configuration
public class Configuartions {

    @Value("${spring.ai.openai.chat.base-url}")
    private String openAiBaseUrl;

    @Value("${spring.ai.openai.chat.completions-path}")
    private String openAiCompletionsPath;

    @Bean
    public OpenAiApi openAiApi(@Value("${spring.ai.openai.api-key}") String apiKey) {
        return OpenAiApi.builder()
                .baseUrl(openAiBaseUrl)
                .apiKey(apiKey)
                .completionsPath(openAiCompletionsPath)
                .embeddingsPath("/embeddings")
                .build();
    }

    @Bean
    public ChatMemoryRepository chatMemoryRepository() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10) // to keep last 10 messages
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel(OpenAiApi openAiApi) {
        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, 
                OpenAiEmbeddingOptions.builder().model("gemini-embedding-2-preview").build());
    }

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader) throws IOException {
        VectorStore store = SimpleVectorStore.builder(embeddingModel).build();
        Resource resource = resourceLoader.getResource("classpath:university-policy.txt");
        String content = new String(resource.getInputStream().readAllBytes());

        // Split into chunks and load into vector store
        List<Document> documents = List.of(new Document(content));
        List<Document> chunks = new TokenTextSplitter().split(documents);
        store.add(chunks);
        return store;
    }

    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(5)                        // top 5 relevant chunks
                        .similarityThreshold(0.5)       // minimum similarity score
                        .build())
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory, QuestionAnswerAdvisor questionAnswerAdvisor) {
        return ChatClient.builder(chatModel)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), questionAnswerAdvisor)
            .build();
    }
}

