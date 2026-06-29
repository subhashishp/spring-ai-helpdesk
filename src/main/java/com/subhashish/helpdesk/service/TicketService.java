package com.subhashish.helpdesk.service;


import com.subhashish.helpdesk.dto.UserTicketsDTO;
import com.subhashish.helpdesk.entity.Status;
import com.subhashish.helpdesk.entity.Ticket;
import com.subhashish.helpdesk.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TicketService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket createTicket(Ticket ticket) {
        LOGGER.info("Creating a ticket for user {}", ticket.getUsername());

        ticket.setId(null);
        ticket.setStatus(Status.OPEN);
        LOGGER.info("Priority Set to --------------------------------- {} ",ticket.getPriority());
        return ticketRepository.save(ticket);
    }

    public Ticket getTicket(Long ticketId) {
        LOGGER.info("Finding ticket details using ticketId {}",ticketId);

        return ticketRepository.findById(ticketId).orElse(null);
    }

    public String findUserNameByEmail(String email) {
        LOGGER.info("Finding username details using emailId {}",email);

        return ticketRepository.findUsernameByEmail(email).orElse(null);
    }

    public Ticket getTicketByUserName(String username) {
        LOGGER.info("Finding ticket using username {}", username);

        return ticketRepository.findByUsername(username).orElse(null);
    }

    public Ticket getTicketByEmail(String email) {
        LOGGER.info("Finding ticket using email {}", email);

        return ticketRepository.findByEmail(email).stream().findFirst().orElse(null);
    }

    public List<Ticket> getListOfTicketsByEmail(String email) {
        LOGGER.info("Finding all ticket of email {}", email);

        return ticketRepository.findByEmail(email);
    }

    public Ticket updateTicket(Ticket ticket) {
        LOGGER.info("Updating the current ticket having ticket id {}", ticket.getId());
        return ticketRepository.save(ticket);
    }

    public String findUsernameFromEmail(String email) {
        LOGGER.info("Finding for username having email id {}", email);

        return ticketRepository.findUsernameByEmail(email).orElse(null);
    }

    public List<UserTicketsDTO> findAllOpenTicketsOfUser(String identifier) {
        String searchOn = "email";
        if(identifier.trim().contains("@"))
            return ticketRepository.findSummaryAndDescriptionByEmail(identifier);
        else
            return ticketRepository.findSummaryAndDescriptionByUsername(identifier);
    }

}
