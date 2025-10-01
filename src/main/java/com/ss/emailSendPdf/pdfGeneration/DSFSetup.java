package com.ss.emailSendPdf.pdfGeneration;

/**
 * DSF Setup information - extends base incident
 */
public class DSFSetup extends Incident {
    private String programName;
    private String dsfModuleLink;
    private String rejectionReason; // Only for rejected templates

    public DSFSetup(String ticketNumber, String ticketTitle, String ticketDescription,
                    String programName, String dsfModuleLink, String rejectionReason) {
        super(ticketNumber, ticketTitle, ticketDescription);
        this.programName = programName;
        this.dsfModuleLink = dsfModuleLink;
        this.rejectionReason = rejectionReason;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getDsfModuleLink() {
        return dsfModuleLink;
    }

    public void setDsfModuleLink(String dsfModuleLink) {
        this.dsfModuleLink = dsfModuleLink;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}