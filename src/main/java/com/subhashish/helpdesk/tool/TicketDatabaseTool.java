package com.subhashish.helpdesk.tool;

import com.subhashish.helpdesk.entity.Ticket;
import com.subhashish.helpdesk.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class TicketDatabaseTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketDatabaseTool.class);

    private final TicketService ticketService;

    public TicketDatabaseTool(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Tool(description = "This tool helps to create a new ticket.")
    public Ticket createTicketTool(@ToolParam(description = "Ticket Details required to create a new ticket.") Ticket ticket) {

        try{
            LOGGER.info("Creating a new ticket : {} ", ticket);
            return ticketService.createTicket(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Tool(description = "This tool helps to get ticket of the user using the username.")
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

}
