package com.mycompany.tickets;

public interface Ticket {
    void assignTo(User user);
    void updateStatus(String status);
    void addComment(String comment);
}
