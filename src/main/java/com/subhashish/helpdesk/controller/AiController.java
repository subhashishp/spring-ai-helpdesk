package com.subhashish.helpdesk.controller;

import com.subhashish.helpdesk.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/block/chat")
    public ResponseEntity<String> getResponse(@RequestBody String query, @RequestHeader("ConversationId") String conversationid){
        return ResponseEntity.ok(aiService.getResponseFromAssistant(query, conversationid));
    }

    @PostMapping("/stream/chat")
    public ResponseEntity<Flux<String>> getStreamResponse(@RequestBody String query, @RequestHeader("ConversationId") String conversationid){
        return ResponseEntity.ok(this.aiService.getResponseFromAssistantStream(query, conversationid));
    }
}
