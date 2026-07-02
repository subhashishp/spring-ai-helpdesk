package com.subhashish.helpdesk.repository;

import com.subhashish.helpdesk.dto.UserTicketsDTO;
import com.subhashish.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {

    Optional<Ticket> findByUsername(String username);
    List<Ticket> findByEmail(String username);

    @Query("SELECT t.username FROM Ticket t WHERE t.email = :email")
    Optional<String> findUsernameByEmail(String email);

    @Query("SELECT new com.subhashish.helpdesk.dto.UserTicketsDTO(t.id, t.summary, t.description) FROM Ticket t " +
            "WHERE t.email = :email")
    List<UserTicketsDTO> findSummaryAndDescriptionByEmail(@Param("email") String email);

    @Query("""
            SELECT new com.subhashish.helpdesk.dto.UserTicketsDTO(t.id, t.summary, t.description) 
            FROM Ticket t
            WHERE t.username = :username
            """)
    List<UserTicketsDTO> findSummaryAndDescriptionByUsername(@Param("username") String username);

    @Query(value = "SELECT t.username FROM help_desk_tickets t where email=:email LIMIT 1", nativeQuery = true)
    Optional<String> findUsernameFromEmail(@Param("email") String email);


}
