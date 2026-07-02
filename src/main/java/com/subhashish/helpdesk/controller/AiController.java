package com.subhashish.helpdesk.controller;

import com.subhashish.helpdesk.entity.Intent;
import com.subhashish.helpdesk.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);
    private final AiService aiService;


    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/block/chat")
    public ResponseEntity<String> getResponse(@RequestBody String query,
                                              @RequestHeader("ConversationId") String conversationid){
        Intent intent = aiService.simpleChatAssistant(query);
        log.info("LLM Router classified query as: {}", intent);
        if(Intent.TROUBLESHOOTING.equals(intent)) {
            return ResponseEntity.ok(aiService.getAssistantWithRags(query, conversationid));
        }
        else {
            return ResponseEntity.ok(aiService.getResponseFromAssistant(query, conversationid));
        }
    }

    @PostMapping("/stream/chat")
    public ResponseEntity<Flux<String>> getStreamResponse(@RequestBody String query, @RequestHeader("ConversationId") String conversationid){
        return ResponseEntity.ok(this.aiService.getResponseFromAssistantStream(query, conversationid));
    }
}
