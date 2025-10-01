package com.ss.emailSendPdf.pdfGeneration;

/**
 * Incident/Ticket information
 */
public class Incident {
    private String ticketNumber;
    private String ticketTitle;
    private String ticketDescription;

    public Incident() {
    }

    public Incident(String ticketNumber, String ticketTitle, String ticketDescription) {
        this.ticketNumber = ticketNumber;
        this.ticketTitle = ticketTitle;
        this.ticketDescription = ticketDescription;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTicketTitle() {
        return ticketTitle;
    }

    public void setTicketTitle(String ticketTitle) {
        this.ticketTitle = ticketTitle;
    }

    public String getTicketDescription() {
        return ticketDescription;
    }

    public void setTicketDescription(String ticketDescription) {
        this.ticketDescription = ticketDescription;
    }
}