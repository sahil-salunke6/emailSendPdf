package com.ss.emailSendPdf.pdfGeneration;

/**
 * Request wrapper
 */
public class EmailGenerationRequest {
    private EmailTemplateRequest request;

    // action: "send" or "pdf"
    private String action;

    public EmailGenerationRequest() {
    }

    public EmailGenerationRequest(EmailTemplateRequest request, String action) {
        this.request = request;
        this.action = action;
    }

    public EmailTemplateRequest getRequest() {
        return request;
    }

    public void setRequest(EmailTemplateRequest request) {
        this.request = request;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}