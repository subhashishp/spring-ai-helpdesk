package com.subhashish.helpdesk.tool;

import com.subhashish.helpdesk.entity.Priority;
import com.subhashish.helpdesk.entity.Status;
import com.subhashish.helpdesk.entity.Ticket;
import com.subhashish.helpdesk.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class TicketDatabaseTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketDatabaseTool.class);

    private final TicketService ticketService;

    private static final String DELIMITER = "\u001E";

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
                    "priority (LOW, MEDIUM, HIGH, URGENT) -- STRICT rule - use the exact among these mentioned priorities only, " +
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

    // On the username tool
    @Tool(description = """
    ALWAYS call this tool to fetch ticket details when the user provides or mentions their username. \
    Or user ask's for the update on his/her ticket using username \
    Triggered by phrases like 'my ticket', 'check my ticket', 'ticket status', 'show my issue' \
    when a username is available. Never answer ticket details from memory. \
    Call this before responding to any ticket lookup request involving a username.
    """)
    public Ticket getTicketByUserNameTool(@ToolParam(description = "The exact username of the ticket owner, " +
            "e.g. 'john_doe' or 'subhashish123'. Ask the user if not provided.") String username) {

        LOGGER.info("Tool invoked: getTicketByUserNameTool with for user : {}", username);

        return ticketService.getTicketByUserName(username);
    }

    // On the email tool
    @Tool(description = """
    ALWAYS call this tool to fetch ticket details when the user provides or mentions their email address. \
    Or user ask's for the update on his/her ticket using emailId \
    Triggered by phrases like 'my ticket', 'check my ticket', 'ticket status', 'show my issue' \
    when an email is available. Never answer ticket details from memory. \
    Call this before responding to any ticket lookup request involving an email.
    """)
    public Ticket getTicketByEmailTool(@ToolParam(description = "The exact emailId of the ticket owner, " +
            "e.g. 'john@outlook.com' or 'subh@gmail.com'. Ask the user if not provided.") String email) {

        LOGGER.info("Tool invoked: getTicketByEmailTool with for user : {}", email);

        return ticketService.getTicketByEmail(email);
    }


    @Tool(name = "findAllTicketByEmail",
    description = """ 
            ALWAYS call this tool to fetch all tickets belonging to a user when their email address is available. \\
                Triggered by phrases like 'show my tickets', 'list my tickets', 'what are my tickets', \\
                'all my issues', 'fetch my tickets' when an email is provided. \\
                Also call this when the user refers to a ticket without specifying a ticket ID or information about the ticket— \\
                fetch the full list first so the user can identify which ticket they mean. \\
                Use this tool ONLY when an email address is available. \\
                If only a username is provided, use the findAllTicketByUsername tool instead. \\
                Never answer ticket details from memory.
            """)
    public List<Ticket> getListOfTicketsByEmailId(String emailId) {

        LOGGER.info("Tool invoked: getListOfTicketsByEmailId with for email : {}", emailId);

        return ticketService.getListOfTicketsByEmail(emailId);
    }

//    @Tool(
//            name = "updateTicket",
//            description = """
//    ALWAYS call this tool when the user wants to update, modify, change, or edit a ticket — \
//    including updating status, priority, description, or any other ticket field. \
//    Never update a ticket from memory. Must be called before confirming any ticket update.
//    """)
    public Ticket updateTicketTool(@ToolParam(description = "Ticket Details with ticket id.") Ticket ticket) {
        LOGGER.info("Tool invoked: updateTicketTool with for user : {}", ticket.getEmail());

        return ticketService.updateTicket(ticket);
    }


    @Tool(
            name = "updateTicket",
            description = """
    Updates an existing support ticket's status, priority, or description.
    
    CRITICAL RULES:
    1. DO NOT use this tool to resolve a ticket. If the user wants to resolve/close a ticket, you MUST use the 'resolveTicket' tool instead.
    2. You MUST have the exact numeric Ticket ID. If the user doesn't provide it, DO NOT guess. Ask the user for their email and use the 'findAllTicketByEmail' tool to find the ID first.
    3. If you are updating the 'status', you MUST also provide a 'description' explaining why the status is being changed. Ask the user for a reason if they didn't provide one.
    4. Only pass the fields the user explicitly wants to change. Pass null for all other fields.
    """
    )
    public Ticket updateTicketToolV2(
            @ToolParam(description = "The exact ID of the ticket. (REQUIRED)") Long ticketId,
            @ToolParam(description = "The new status. MUST be one of: [OPEN, CLOSED, RESOLVED].") String statusString,
            @ToolParam(description = "The new priority. MUST be one of: [LOW, MEDIUM, HIGH, URGENT]. Pass null if not updating.") String priorityString,
            @ToolParam(description = "New text to append to the description. Pass null if not updating.") String description
    ) {
        LOGGER.info("Took invoked: updateTicketTool with for ticketId : {}", ticketId);

        Ticket ticket = ticketService.getTicket(ticketId);
        if(ticket == null) {
            throw new IllegalArgumentException("Ticket not found for ID "+ ticketId);
        }

        if(statusString != null) {
            LOGGER.info("STATUS :: Ticket status changing to {}", statusString);
            if(description != null && !description.trim().isEmpty()) {
                LOGGER.info("STATUS AND DESCRIPTION :: Ticket status and description updating to {} , {}", statusString,
                        description);
                try {
                    Status status = Status.valueOf(statusString.trim().toUpperCase());
                    ticket.setStatus(status);
                } catch (IllegalArgumentException e) {
                    // Throw a descriptive error! Spring AI will catch this and send it back to the LLM.
                    throw new IllegalArgumentException("Error: '" + statusString + "' is not a valid status. You must use OPEN, CLOSED, or RESOLVED.");
                }

                String currentDesc = ticket.getDescription();
                ticket.setDescription(updateTicketDescription(currentDesc, description));

            } else {
                throw new IllegalArgumentException("Error: You must provide a description (reason) when updating the ticket status.");
            }
        } else if (description != null && !description.trim().isEmpty()) {
            LOGGER.info("DESCRIPTION :: Ticket description updating to {}", description);
            ticket.setDescription(updateTicketDescription(ticket.getDescription(), description));
        }

        if(priorityString != null && !priorityString.trim().isEmpty()) {
            LOGGER.info("PRIORITY :: Ticket priority updating to {} :", priorityString);
            try {
                Priority priority = Priority.valueOf(priorityString.trim().toUpperCase());
                ticket.setPriority(priority);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error: '" + priorityString + "' is not a valid priority. You must use [LOW, MEDIUM, HIGH]");
            }
        }

        LOGGER.info("UPDATING the ticket of user {} + ticket - {}", ticket.getUsername(), ticket.getSummary());
        return ticketService.updateTicket(ticket);
    }

    // this method is used to append the update in the description to create the new description
    public String updateTicketDescription(String currentDescription,String update){

        if(currentDescription == null)
            currentDescription = "";
        else
            currentDescription += DELIMITER;

        return currentDescription + update;
    }

    @Tool(name = "getExistingUsernameByEmail",
            description = """
    ALWAYS call this tool during ticket creation when the username is not provided or the user has forgotten it. \
    If the user provides an email address, call this tool silently to look up their username from the database \
    before proceeding with ticket creation. \
    Do NOT ask the user for their username if an email is available — look it up first using this tool. \
    If this tool returns null, inform the user: 'I could not find an account with that email. \
    Please provide your username to proceed.' \
    Never skip this tool when username is missing and email is present.
    """)
    public String getExistingUserNameUsingEmail(@ToolParam(description = "Email address of the user to look up their username. " +
            "Must be a valid email format e.g. user@example.com") String email) {

        return ticketService.findUserNameByEmail(email);
    }

    @Tool(
            name = "resolveTicket",
            description = """
    Resolves an existing support ticket.
    
    CRITICAL RULES:
    1. USE THIS TOOL EXCLUSIVELY when a user states a ticket is resolved, fixed, or completed.
    2. You MUST have the exact numeric Ticket ID. 
    3. You MUST provide the 'resolution' text explaining how the issue was fixed. If the user doesn't provide the solution steps, ask them for it before calling this tool.
    """
    )
    public Ticket resolveTicketTool(
            @ToolParam(description = "The exact ID of the ticket. (REQUIRED)") Long ticketId,
            @ToolParam(description = "The detailed explanation of how the issue was solved. (REQUIRED)") String resolution
    ) {
        LOGGER.info("Took invoked: resolving ticketId : {} , resolutionNote : {}", ticketId,resolution);

        Ticket ticket = ticketService.getTicket(ticketId);
        if(ticket == null) {
            throw new IllegalArgumentException("Ticket not found for ID "+ ticketId);
        }
        if(resolution.isEmpty())
            throw new IllegalArgumentException("Resolution note is required");

        return ticketService.resolveTicket(ticket,resolution);
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


