package com.subhashish.helpdesk.repository;

import com.subhashish.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {

    Optional<Ticket> findByUsername(String username);
    Optional<Ticket> findByEmail(String username);
    Optional<String> findUsernameByEmail(String email);
}
