package com.subhashish.helpdesk.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Status {
    OPEN, CLOSED, RESOLVED;

    @JsonCreator
    public static Status fromString(String value) {
        if (value == null) return null;

        String cleaned = value.trim().toUpperCase().replace("-", "_");

        try {
            return Status.valueOf(cleaned);
        } catch (IllegalArgumentException e) {
            // If the LLM hallucinates a bad status, return null or a default
            // Returning null tells the update logic to skip updating this field
            return null;
        }
    }
}
