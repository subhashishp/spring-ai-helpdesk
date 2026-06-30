package com.subhashish.helpdesk.service;

import com.subhashish.helpdesk.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public String searchResolutionInVectorDB(String issue) {
        log.info("Searching problem for issue in vector DB{} ", issue);

        SearchRequest searchRequest = SearchRequest.builder().query(issue)
                .topK(3)
                .similarityThreshold(0.75)
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

        if (similarDocuments.isEmpty()) {
            log.info("No Similar historical ticket found for this issue");
            return "No historical information found related to this issue";
        }

        String previousInformation = "Earlier/Common resolution of this issue - ";

        previousInformation += similarDocuments.stream()
                .map(doc -> doc.getFormattedContent()) // Extracts the actual text
                .collect(Collectors.joining("\n\n---\n\n"));

        return previousInformation;
    }
}
