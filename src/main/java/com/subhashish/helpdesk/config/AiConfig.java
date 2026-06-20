package com.subhashish.helpdesk.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {


    @Bean
    ChatClient chatClient(OpenAiChatModel openAiChatModel, JdbcChatMemoryRepository chatMemoryRepository) {

        var chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(15)
                .build();

        MessageChatMemoryAdvisor chatContext = MessageChatMemoryAdvisor.builder(chatMemory).build();

        return ChatClient.builder(openAiChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor(), chatContext)
                .build();
    }
}
