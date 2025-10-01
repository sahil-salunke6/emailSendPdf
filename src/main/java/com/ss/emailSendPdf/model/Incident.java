package com.ss.emailSendPdf.model;

public class Incident {
private String ticketNumber;
private String ticketTitel;
private String ticketDescription;


// getters & setters
public String getTicketNumber() { return ticketNumber; }
public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
public String getTicketTitel() { return ticketTitel; }
public void setTicketTitel(String ticketTitel) { this.ticketTitel = ticketTitel; }
public String getTicketDescription() { return ticketDescription; }
public void setTicketDescription(String ticketDescription) { this.ticketDescription = ticketDescription; }
}