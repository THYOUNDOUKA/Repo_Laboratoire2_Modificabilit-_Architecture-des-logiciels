package com.mycompany.tickets;

import java.util.List;

public interface Admin {
    void assignTicket(Ticket ticket, User user);
     void closeTicket(Ticket ticket);
     List<Ticket> viewAllTickets();


}
