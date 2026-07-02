package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.entity.Intent;
import com.subhashish.helpdesk.tool.TicketDatabaseTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
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

    private static final String RAG_SYSTEM_MESSAGE = """
    You are Liza, a polite, professional, and efficient Help Desk Assistant at Substring Technologies Company.
    Your goal is to help users resolve technical issues using ONLY the provided historical data.

    HISTORICAL DATA:
    {context}
    
    INSTRUCTIONS:
    1. Analyze the user's issue and check if the HISTORICAL DATA contains a related solution.
    2. If a relevant solution is found, explain it clearly and politely to the user.
    3. If the HISTORICAL DATA is NOT related to the user's issue, or does not contain a fix, DO NOT guess, DO NOT use outside knowledge, and DO NOT invent a solution.
    4. If no solution is found, apologize politely, state that you cannot find a historical fix, and ask the user: "Would you like me to open a new support ticket for this?"
    """;

    private static final String systemMessageSimpleModel = """
        You are an expert IT support triage router. Your only job is to analyze the user's message and classify their intent into one of two categories:
        
        1. TROUBLESHOOTING: The user is describing a technical problem, error, or bug that needs to be fixed. 
        2. TICKET_MANAGEMENT: The user explicitly wants to open a new ticket, check the status of an existing ticket, or update a ticket.
    
        Respond ONLY with the classification enum.
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
            String llmResponse =  this.chatClient
                    .prompt(query)
                    .system(systemMessageSimpleModel)
                    .call()
                    .content();
            try {
                assert llmResponse != null;
                return Intent.valueOf(llmResponse.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("LLM returned unexpected intent: {}", llmResponse);
                // Handle the fallback (e.g., return a default Intent or throw a custom exception)
                return Intent.TICKET_MANAGEMENT;
            }
        } catch (RestClientException e) {
            log.error("AI service is unavailable: {}", e.getMessage());
            return Intent.TICKET_MANAGEMENT;
        }
    }

    public String getAssistantWithRags(String query, String conversationId) {
        try {
            String historicalData = ticketVectorService.searchResolutionInVectorDB(query);

            log.info("Historical data received - {}", historicalData);
            return this.chatClient
                    .prompt(query)
                    .system(promptSystemSpec -> promptSystemSpec
                            .text(RAG_SYSTEM_MESSAGE)
                            .param("context", historicalData))
                    .advisors(advisorSpec -> advisorSpec
                            .advisors(new SimpleLoggerAdvisor())
                            .param(ChatMemory.CONVERSATION_ID, conversationId))
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
