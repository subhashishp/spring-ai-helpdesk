package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.tool.TicketDatabaseTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

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

    public String getResponseFromAssistant(String query) {

        return this.chatClient
                .prompt()
                .user(query)
                .system(systemPrompt)
                .tools(ticketDatabaseTool)
                .call()
                .content();
    }
}
