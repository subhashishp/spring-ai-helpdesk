package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.tool.TicketDatabaseTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import reactor.core.publisher.Flux;

@Service
public class AiService {


    private static final Logger log = LoggerFactory.getLogger(AiService.class);
    private final ChatClient chatClient;
    private final TicketDatabaseTool ticketDatabaseTool;

    @Value("classpath:/helpdesk-system.st")
    private Resource systemPrompt;

    public AiService(ChatClient chatClient, TicketDatabaseTool ticketDatabaseTool) {
        this.chatClient = chatClient;
        this.ticketDatabaseTool = ticketDatabaseTool;
    }

    public String getResponseFromAssistant(String query, String conversationId) {

        try {
            return this.chatClient
                    .prompt()
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .user(query)
                    .system(systemPrompt)
                    .tools(ticketDatabaseTool)
                    .call()
                    .content();
        } catch (RestClientException e) {
            log.error("AI service is unavailable: {}", e.getMessage());
            return "I'm having trouble connecting right now. Please try again in a moment.";
        }
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
