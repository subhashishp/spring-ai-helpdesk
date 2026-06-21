package com.subhashish.helpdesk.tool;

import com.subhashish.helpdesk.entity.Ticket;
import com.subhashish.helpdesk.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TicketDatabaseTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketDatabaseTool.class);

    private final TicketService ticketService;

    public TicketDatabaseTool(TicketService ticketService) {
        this.ticketService = ticketService;
    }

//    @Tool(description = "This tool helps to create a new ticket.")
//    public Ticket createTicketTool(@ToolParam(description = "Ticket Details required to create a new ticket.") Ticket ticket) {
//
//        try{
//            LOGGER.info("Creating a new ticket : {} ", ticket);
//            return ticketService.createTicket(ticket);
//        } catch (Exception e) {
//            LOGGER.error("Ticket creation failed", e);
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }

    @Tool(description = "Creates a new support ticket in the database. Call this tool ONLY after you have collected summary, " +
            "description, priority, username, and email from the user. " +
            "This is the only way a ticket is actually created — never tell the user a ticket was created unless this " +
            "tool has been called and returned a result with a ticket ID.")
    public Ticket createTicketTool(
            @ToolParam(description = "Ticket details. Required fields: summary (short title), description (detailed issue), " +
                    "priority (LOW, MEDIUM, HIGH, or URGENT), " +
                    "username, email. Do not set status, id, " +
                    "createdOn, " +
                    "or updatedOn — these are handled by the system.")
            Ticket ticket) {

        LOGGER.info("Tool invoked: createTicketTool with for user : {}", ticket.getEmail());

        if (ticket.getEmail() == null || ticket.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required to create a ticket.");
        }
        if (ticket.getSummary() == null || ticket.getSummary().isBlank()) {
            throw new IllegalArgumentException("Summary is required to create a ticket.");
        }
        if (ticket.getPriority() == null) {
            throw new IllegalArgumentException("Priority is required to create a ticket.");
        }
        if (ticket.getDescription() == null || ticket.getDescription().isBlank()) {
            throw new IllegalArgumentException("Missing required field: description. Ask the user for more detail, then retry.");
        }
        // Resolve username automatically if not provided
        if (ticket.getUsername() == null || ticket.getUsername().isBlank()) {
            String username = ticketService.findUsernameFromEmail(ticket.getEmail());
            if (username != null) {
                LOGGER.info("Resolved username '{}' from existing ticket for email {}",username , ticket.getEmail());
                ticket.setUsername(username);
            } else {
                throw new IllegalArgumentException(
                        "Missing required field: username. No existing user found for this email. " +
                                "Suggest 2-3 usernames based on the email's local part (e.g. for 'paul2@gmail.com' suggest 'paul2', 'paul2_subh', 'paul2tech') " +
                                "and ask the user to confirm or provide their own, then retry."
                );
            }
        }

        try {
            Ticket created = ticketService.createTicket(ticket);
            LOGGER.info("Ticket created successfully: {}", created.getId());
            return created;
        } catch (Exception e) {
            LOGGER.error("Ticket creation failed for payload: {}", ticket, e);
            throw new RuntimeException("Failed to create ticket: " + e.getMessage(), e);
        }
    }

    @Tool(description = "This tool helps to get ticket of the user using the provided username.")
    public Ticket getTicketByUserNameTool(@ToolParam(description = "Username of the ticket owner") String username) {
        return ticketService.getTicketByUserName(username);
    }

    @Tool(description = "This tool helps to get ticket of the user using the provided emailId.")
    public Ticket getTicketByEmailTool(@ToolParam(description = "Email id of the ticket owner") String email) {
        return ticketService.getTicketByEmail(email);
    }


    @Tool(description = "This tool helps to update the ticket for the particular user.")
    public Ticket updateTicketTool(@ToolParam(description = "Ticket Details with ticket id.") Ticket ticket) {
        return ticketService.updateTicket(ticket);
    }

//
//
//    @Tool(description = "This tool helps to get username for a given email id.")
//    public String getUsernameUsingEmail(@ToolParam(description = "Email id of the user whose username needs to be searched") String email) {
//        return ticketService.findUsernameForEmail(email);
//    }
//
//    public record TicketLookupResult(boolean success, String message, Ticket ticket) {}
//
//    @Tool(description = """
//    Retrieves a ticket using the provided username and/or email.
//    Tries the username first. If no match is found and an email is available
//    (from this call or conversation history), tries the email next.
//    If both lookups fail, ask the user to confirm or provide a different username,
//    then call this tool again with the corrected value.
//    """)
//    public TicketLookupResult getTicketByUserNameTool(
//            @ToolParam(description = "Username of the ticket owner, if known", required = false) String username,
//            @ToolParam(description = "Email of the ticket owner, used as a fallback lookup if username doesn't match", required = false) String email) {
//
//        if ((username == null || username.isBlank()) && (email == null || email.isBlank())) {
//            return new TicketLookupResult(false,
//                    "Both username and email are missing. Check the conversation history for either, "
//                            + "or ask the user to provide their username or email.", null);
//        }
//
//        Ticket ticket = null;
//
//        if (username != null && !username.isBlank()) {
//            ticket = ticketService.getTicketByUserName(username);
//        }
//
//        if (ticket == null && email != null && !email.isBlank()) {
//            ticket = ticketService.getTicketByEmail(email);
//        }
//
//        if (ticket == null) {
//            return new TicketLookupResult(false,
//                    "No ticket found for username '" + username + "' or email '" + email + "'. "
//                            + "Ask the user to double-check the username, or provide a different one — "
//                            + "then call this tool again with the corrected value.", null);
//        }
//
//        List<String> missing = new ArrayList<>();
//        if (ticket.getEmail() == null || ticket.getEmail().isBlank()) missing.add("email");
//        if (ticket.getDescription() == null || ticket.getDescription().isBlank()) missing.add("description");
//
//        if (!missing.isEmpty()) {
//            return new TicketLookupResult(true,
//                    "Ticket found, but the following fields are empty in the record: "
//                            + String.join(", ", missing)
//                            + ". Mention this to the user when presenting the ticket details — "
//                            + "do not omit it silently.", ticket);
//        }
//
//        return new TicketLookupResult(true, "Ticket retrieved successfully.", ticket);
//    }

//    @Tool(description = """
//    Creates a new support ticket. Required fields: summary, description, username, email, priority.
//    Before calling this tool, make sure you have all required fields — check the conversation
//    history first, and if any are still missing, ask the user for them.
//    If the result has success=false, do NOT tell the user the ticket was created —
//    follow the message instructions (ask the user for the missing info, then retry this tool).
//    """)
//    public TicketCreationResult createTicketTool(
//            @ToolParam(description = "Ticket details required to create a new ticket.") Ticket ticket) {
//
//        if (ticket == null) {
//            return new TicketCreationResult(false,
//                    "No ticket details were provided. Ask the user for a summary, description, "
//                            + "username, email, and priority before retrying.", null);
//        }
//
//        List<String> missing = new ArrayList<>();
//        if (ticket.getSummary() == null || ticket.getSummary().isBlank()) missing.add("summary");
//        if (ticket.getDescription() == null || ticket.getDescription().isBlank()) missing.add("description");
//        if (ticket.getUsername() == null || ticket.getUsername().isBlank()) missing.add("username");
//        if (ticket.getEmail() == null || ticket.getEmail().isBlank()) missing.add("email");
//        if (ticket.getPriority() == null) missing.add("priority");
//
//        if (!missing.isEmpty()) {
//            return new TicketCreationResult(false,
//                    "Cannot create the ticket — the following required fields are missing: "
//                            + String.join(", ", missing)
//                            + ". Check the conversation history for these values, and if still unavailable, "
//                            + "ask the user for them. Then call this tool again with the complete details.", null);
//        }
//
//        try {
//            LOGGER.info("Creating a new ticket : {} ", ticket);
//            Ticket saved = ticketService.createTicket(ticket);
//            return new TicketCreationResult(true, "Ticket created successfully.", saved);
//        } catch (Exception e) {
//            LOGGER.error("Failed to create ticket: {}", ticket, e);
//            return new TicketCreationResult(false,
//                    "An error occurred while creating the ticket. Inform the user that the ticket "
//                            + "could not be created and they may need to try again later.", null);
//        }
//    }
//
//    public record TicketCreationResult(boolean success, String message, Ticket ticket) {}
}


