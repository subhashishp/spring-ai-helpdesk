package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TicketVectorService {


    private static final Logger log = LoggerFactory.getLogger(TicketVectorService.class);
    private final VectorStore vectorStore;

    public TicketVectorService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void addTicketToVectorDB(Ticket ticket, String resolution) {
        log.info("Adding ticket {} with resolution {} to vector DB", ticket.getId(), resolution);

        StringBuilder content = new StringBuilder();
        content.append(String.format("Ticket Summary: %s\n", ticket.getSummary()));
        content.append(String.format("Resolution: %s\n", resolution));

        Map<String, Object> metadata = Map.of(
                "ticketId", ticket.getId(),
                "email", ticket.getEmail(),
                "priority", ticket.getPriority()
        );

        Document document = new Document(content.toString(), metadata);

        vectorStore.add(List.of(document));

        log.info("Ticket and resolution added to vectorDB");
    }
}
