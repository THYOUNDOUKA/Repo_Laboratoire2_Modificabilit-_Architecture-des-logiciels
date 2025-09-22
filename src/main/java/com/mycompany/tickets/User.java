package com.mycompany.tickets;

public class User {
    private int userID;
    private String name;
    private String email;

    public User(){

    }

    public User(int userID, String name, String email, String role) {
        this.userID = userID;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    private String role;
    void createTicket(Ticket ticket){}
    void viewTicket(Ticket ticket){}
    void updateTicket(Ticket ticket){}

}
