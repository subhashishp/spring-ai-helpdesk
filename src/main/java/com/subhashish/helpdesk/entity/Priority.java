package com.subhashish.helpdesk.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Priority {
    LOW, MEDIUM, HIGH, URGENT;

    @JsonCreator
    public static Priority fromString(String value) {
        if (value == null) return null;

        // Clean up common LLM variations
        String cleaned = value.trim().toUpperCase().replace("-", "_");

        try {
            return Priority.valueOf(cleaned);
        } catch (IllegalArgumentException e) {
            // Fallback gracefully to a default instead of crashing your API
            return MEDIUM;
        }
    }
}
