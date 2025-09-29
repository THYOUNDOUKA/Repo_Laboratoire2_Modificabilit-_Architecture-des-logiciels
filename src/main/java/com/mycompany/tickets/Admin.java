package com.mycompany.tickets;

import java.util.List;

public class Admin {
    private int adminID;
    private String name;
    private String email;
public Admin() {

}
    public Admin(int adminID, String name, String email) {
        this.adminID = adminID;
        this.name = name;
        this.email = email;
    }

    public int getAdminID() {
        return adminID;
    }

    public void setAdminID(int adminID) {
        this.adminID = adminID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    void assignTicket(Ticket ticket, User user){

    }
    void closeTicket(Ticket ticket){

    }
    List<Ticket> viewAllTickets() {
        return null;

    };

}
