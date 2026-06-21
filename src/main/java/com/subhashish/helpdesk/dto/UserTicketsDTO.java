package com.subhashish.helpdesk.dto;

public class UserTicketsDTO {

    Long ticketId;
    String summary;
    String description;

    public UserTicketsDTO(Long ticketId, String summary, String description) {
        this.ticketId = ticketId;
        this.summary = summary;
        this.description = description;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
