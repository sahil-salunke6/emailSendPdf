package com.ss.emailSendPdf.pdfGeneration;

/**
 * Base request class
 */
public class EmailTemplateRequest {
    private String saluation;
    private Incident incident;
    private User user;

    private DSFSetup dsfSetup;

    public EmailTemplateRequest() {
    }

    public EmailTemplateRequest(String salutation, Incident incident, User user, DSFSetup dsfSetup) {
        this.saluation = salutation;
        this.incident = incident;
        this.user = user;
        this.dsfSetup = dsfSetup;
    }

    public String getSalutation() {
        return saluation;
    }

    public void setSalutation(String salutation) {
        this.saluation = salutation;
    }

    public Incident getIncident() {
        return incident;
    }

    public void setIncident(Incident incident) {
        this.incident = incident;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DSFSetup getDsfSetup() {
        return dsfSetup;
    }

    public void setDsfSetup(DSFSetup dsfSetup) {
        this.dsfSetup = dsfSetup;
    }
}