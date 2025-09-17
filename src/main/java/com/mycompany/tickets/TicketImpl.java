package com.mycompany.tickets;

import java.util.Date;

public class TicketImpl {
    private int ticketID;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Date creationDate;
    private Date updateDate;
  public TicketImpl(){

  };
    public TicketImpl(int ticketID, String title, String description, String status, String priority, Date creationDate, Date updateDate) {
        this.ticketID = ticketID;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    public int getTicketID() {
        return ticketID;
    }

    public void setTicketID(int ticketID) {
        this.ticketID = ticketID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

}
