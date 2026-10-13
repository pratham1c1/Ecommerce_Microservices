package com.pcProject.ecomProducts.Config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

    @Bean(name = "OllamaBean")
    public ChatClient getOllamaChatClient(OllamaChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder().maxMessages(4).chatMemoryRepository
                                        (new InMemoryChatMemoryRepository()).build()).build())
                .defaultOptions(ChatOptions.builder()
                        .model("qwen2.5:3b")
//                        .maxTokens(100)
                        .temperature(0.5)
                        .build())
                .build();
    }

}
