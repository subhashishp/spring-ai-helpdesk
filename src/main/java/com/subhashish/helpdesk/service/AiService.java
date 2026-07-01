package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.entity.Intent;
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
    private final TicketVectorService ticketVectorService;

    private static final String systemMessageSimpleModel = """
        You are an expert IT support triage router. Your only job is to analyze the user's message and classify their intent into one of two categories:
        
        1. NEW_ISSUE: The user is describing a new problem or reporting a bug.
        2. EXISTING_TICKET: The user is asking for a status update, adding info, or referring to an already reported problem.
        """;

    @Value("classpath:/helpdesk-system.st")
    private Resource systemPrompt;

    public AiService(ChatClient chatClient, TicketDatabaseTool ticketDatabaseTool, TicketVectorService ticketVectorService) {
        this.chatClient = chatClient;
        this.ticketDatabaseTool = ticketDatabaseTool;
        this.ticketVectorService = ticketVectorService;
    }

    public Intent simpleChatAssistant(String query) {
        try {
            return this.chatClient
                    .prompt(query)
                    .system(systemMessageSimpleModel)
                    .call()
                    .entity(Intent.class);
        } catch (RestClientException e) {
            log.error("AI service is unavailable: {}", e.getMessage());
            return Intent.EXISTING_TICKET;
        }
    }

    public String getAssistantWithRags(String query, String conversationId) {
        try {
            String historicalData = ticketVectorService.searchResolutionInVectorDB(query);

            return this.chatClient
                    .prompt(query)
                    .system(historicalData)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

        } catch (RestClientException e) {
            log.error("AI service is unavailable: {}", e.getMessage());
            return "I'm having trouble connecting right now. Please try again in a moment.";
        }
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
