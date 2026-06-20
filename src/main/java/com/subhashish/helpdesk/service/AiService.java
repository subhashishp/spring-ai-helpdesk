package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.tool.TicketDatabaseTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final TicketDatabaseTool ticketDatabaseTool;

    @Value("classpath:/helpdesk-system.st")
    private Resource systemPrompt;

    public AiService(ChatClient chatClient, TicketDatabaseTool ticketDatabaseTool) {
        this.chatClient = chatClient;
        this.ticketDatabaseTool = ticketDatabaseTool;
    }

    public String getResponseFromAssistant(String query, String conversationId) {

        return this.chatClient
                .prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query)
                .system(systemPrompt)
                .tools(ticketDatabaseTool)
                .call()
                .content();
    }

    public Flux<String> getResponseFromAssistantStream(String query, String conversationId) {
        return this.chatClient
                .prompt()
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(query)
                .system(systemPrompt)
                .tools(ticketDatabaseTool)
                .stream()
                .content();
    }
}
