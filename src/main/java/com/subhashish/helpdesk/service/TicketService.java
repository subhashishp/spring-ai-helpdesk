package com.subhashish.helpdesk.service;


import com.subhashish.helpdesk.entity.Ticket;
import com.subhashish.helpdesk.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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
        return ticketRepository.save(ticket);
    }

    public Ticket getTicket(Long ticketId) {
        LOGGER.info("Finding ticket details using ticketId {}",ticketId);

        return ticketRepository.findById(ticketId).orElse(null);
    }

    public Ticket getTicketByUserName(String username) {
        LOGGER.info("Finding ticket using username {}", username);

        return ticketRepository.findByUsername(username).orElse(null);
    }

    public Ticket getTicketByEmail(String email) {
        LOGGER.info("Finding ticket using email {}", email);

        return ticketRepository.findByEmail(email).orElse(null);
    }

    public Ticket updateTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

}
