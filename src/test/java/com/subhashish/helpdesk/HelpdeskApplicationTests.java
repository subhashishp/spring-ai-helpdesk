package com.subhashish.helpdesk;

import com.subhashish.helpdesk.dto.UserTicketsDTO;
import com.subhashish.helpdesk.repository.TicketRepository;
import com.subhashish.helpdesk.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class HelpdeskApplicationTests {

	@Autowired
	private TicketService ticketService;

	@Autowired
	private TicketRepository ticketRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void findAllOpenTicketsOfUser_byEmail_returnsMatchingTickets() {

		List<UserTicketsDTO> result = ticketService.findAllOpenTicketsOfUser("paul2@gmail.com");

		System.out.println(result.getFirst().getTicketId() + ":" + result.getFirst().getDescription());

		List<UserTicketsDTO> result2 = ticketService.findAllOpenTicketsOfUser("brahulH2");

		System.out.println(result2.getFirst().getTicketId() + ":" + result2.getFirst().getDescription());
	}

}
