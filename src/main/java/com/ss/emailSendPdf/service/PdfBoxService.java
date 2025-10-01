package com.ss.emailSendPdf.service;

import com.ss.emailSendPdf.model.enums.TemplateType;
import com.ss.emailSendPdf.pdfGeneration.*;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.type.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfBoxService {

    private static final String OUTPUT_DIR = "E:/WorkPlace/Java/Sample Projects/emailSendPdf/pdf/";
    private static final int PAGE_WIDTH = 612; // Letter size
    private static final int PAGE_HEIGHT = 792;
    private static final int MARGIN = 50;
    private static final int LINE_HEIGHT = 20;
    private static final int MAX_WIDTH = PAGE_WIDTH - (2 * MARGIN);


    /**
     * Generic method to generate PDF for email templates
     *
     * @param templateType           Type of template (REJECTED_DSF_SETUP or PENDING_APPROVAL)
     * @param emailGenerationRequest Request object containing all template data
     * @throws IOException If an error occurs during PDF generation
     */
    public File generateEmailTemplatePDF(TemplateType templateType, EmailGenerationRequest emailGenerationRequest)
            throws IOException, COSVisitorException {

        validateRequest(templateType, emailGenerationRequest);

        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            int yPosition = PAGE_HEIGHT - MARGIN;

            // Generate content based on template type
            if (templateType == TemplateType.DSF_FEE_REJECTED) {
                yPosition = generateRejectedDSFContent(contentStream, yPosition, emailGenerationRequest.getRequest());
            } else if (templateType == TemplateType.DSF_FEE_PENDING_FOR_APPROVAL) {
                yPosition = generatePendingApprovalContent(contentStream, yPosition,
                        emailGenerationRequest.getRequest());
            }

            contentStream.close();

            // Save PDF
            String outFile = OUTPUT_DIR + "pdfBox-" + templateType.name() + System.currentTimeMillis() + ".pdf";
            document.save(outFile);
            return new File(outFile);
        } finally {
            document.close();
        }
    }

    /**
     * Validate required fields in request
     */
    private void validateRequest(TemplateType templateType, EmailGenerationRequest pdfRequest) {
        if (pdfRequest == null || pdfRequest.getRequest() == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        EmailTemplateRequest request = pdfRequest.getRequest();

        if (request.getUser() == null) {
            throw new IllegalArgumentException("User information is required");
        }

        if (request.getUser().getFirstName() == null || request.getUser().getFirstName().isEmpty()) {
            throw new IllegalArgumentException("User first name is required");
        }

        if (request.getUser().getLastName() == null || request.getUser().getLastName().isEmpty()) {
            throw new IllegalArgumentException("User last name is required");
        }

        DSFSetup dsfSetup = request.getDsfSetup();

        if (dsfSetup.getProgramName() == null || dsfSetup.getProgramName().isEmpty()) {
            throw new IllegalArgumentException("Program name is required");
        }

        if (dsfSetup.getDsfModuleLink() == null || dsfSetup.getDsfModuleLink().isEmpty()) {
            throw new IllegalArgumentException("DSF module link is required");
        }

        if (templateType == TemplateType.DSF_FEE_REJECTED) {
            if (dsfSetup.getRejectionReason() == null || dsfSetup.getRejectionReason().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required for rejected template");
            }
        }
    }

    /**
     * Generate content for Rejected DSF Setup template
     */
    private int generateRejectedDSFContent(PDPageContentStream contentStream, int yPosition,
                                           EmailTemplateRequest request) throws IOException {
        User user = request.getUser();
        DSFSetup dsfSetup = request.getDsfSetup();
        String salutation = request.getSalutation() != null ? request.getSalutation() : "Hello";

        // Title
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString("Rejected DSF Setup");
        contentStream.endText();
        yPosition -= 40;

        // Greeting with user name
        yPosition = drawGreeting(contentStream, yPosition, salutation, user.getFullName());

        // Main message paragraph
        String message = "Please be advised that one or more DSF Set up(s) have been rejected for ";
        yPosition = drawMessageWithProgramName(contentStream, yPosition, message, dsfSetup.getProgramName());

        // Second part of message
        String message2 = "Please review and action accordingly.";
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString(message2);
        contentStream.endText();
        yPosition -= 30;

        // Rejection Reason
        yPosition = drawRejectionReason(contentStream, yPosition, dsfSetup.getRejectionReason());

        // Link
        yPosition = drawLink(contentStream, yPosition, dsfSetup.getDsfModuleLink());

        return yPosition;
    }

    /**
     * Generate content for Pending Approval template
     */
    private int generatePendingApprovalContent(PDPageContentStream contentStream, int yPosition,
                                               EmailTemplateRequest request) throws IOException {
        User user = request.getUser();
        DSFSetup dsfSetup = request.getDsfSetup();
        String salutation = request.getSalutation() != null ? request.getSalutation() : "Hello";

        // Title
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString("Pending Approval");
        contentStream.endText();
        yPosition -= 40;

        // Greeting with username
        yPosition = drawGreeting(contentStream, yPosition, salutation, user.getFullName());

        // Main message paragraph
        String message = "Please be advised that one or more DSF Set up(s) are awaiting approval for ";
        yPosition = drawMessageWithProgramName(contentStream, yPosition, message, dsfSetup.getProgramName());

        // Second part of message
        String message2 = "Please review and action accordingly.";
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString(message2);
        contentStream.endText();
        yPosition -= 30;

        // Link
        yPosition = drawLink(contentStream, yPosition, dsfSetup.getDsfModuleLink());

        return yPosition;
    }

    /**
     * Draw greeting line with name
     */
    private int drawGreeting(PDPageContentStream contentStream, int yPosition,
                             String salutation, String name) throws IOException {
        String greeting = salutation + " ";

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString(greeting);
        contentStream.endText();

        float greetingWidth = PDType1Font.HELVETICA.getStringWidth(greeting) / 1000 * 12;
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.moveTextPositionByAmount(MARGIN + greetingWidth, yPosition);
        contentStream.drawString(name + ",");
        contentStream.endText();

        return yPosition - 30;
    }

    /**
     * Draw message with program name in bold
     */
    private int drawMessageWithProgramName(PDPageContentStream contentStream, int yPosition,
                                           String message, String programName) throws IOException {
        List<String> wrappedMessage = wrapText(message, PDType1Font.HELVETICA, 12, MAX_WIDTH);

        for (int i = 0; i < wrappedMessage.size(); i++) {
            String line = wrappedMessage.get(i);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.moveTextPositionByAmount(MARGIN, yPosition);
            contentStream.drawString(line);
            contentStream.endText();

            if (i == wrappedMessage.size() - 1) {
                float lineWidth = PDType1Font.HELVETICA.getStringWidth(line) / 1000 * 12;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.moveTextPositionByAmount(MARGIN + lineWidth, yPosition);
                contentStream.drawString(programName + ".");
                contentStream.endText();
            }
            yPosition -= LINE_HEIGHT;
        }

        return yPosition;
    }

    /**
     * Draw rejection reason with label
     */
    private int drawRejectionReason(PDPageContentStream contentStream, int yPosition,
                                    String rejectionReason) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString("Rejection Reason: ");
        contentStream.endText();

        float reasonLabelWidth = PDType1Font.HELVETICA_BOLD.getStringWidth("Rejection Reason: ") / 1000 * 12;

        List<String> wrappedReason = wrapText(rejectionReason, PDType1Font.HELVETICA, 12,
                MAX_WIDTH - (int) reasonLabelWidth);

        for (int i = 0; i < wrappedReason.size(); i++) {
            String line = wrappedReason.get(i);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            if (i == 0) {
                contentStream.moveTextPositionByAmount(MARGIN + reasonLabelWidth, yPosition);
            } else {
                contentStream.moveTextPositionByAmount(MARGIN, yPosition);
            }
            contentStream.drawString(line);
            contentStream.endText();
            yPosition -= LINE_HEIGHT;
        }
        yPosition -= 10;

        return yPosition;
    }

    /**
     * Draw link with underline
     */
    private int drawLink(PDPageContentStream contentStream, int yPosition,
                         String dsfModuleLink) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString("Login to the DSF module");
        contentStream.endText();

        // Draw underline for link
        float linkWidth = PDType1Font.HELVETICA.getStringWidth("Login to the DSF module") / 1000 * 12;
        contentStream.setStrokingColor(0, 0, 255); // Blue color
        contentStream.drawLine(MARGIN, yPosition - 2, MARGIN + linkWidth, yPosition - 2);

        yPosition -= 20;

        // Add link URL in smaller text
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 9);
        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
        contentStream.drawString("(" + dsfModuleLink + ")");
        contentStream.endText();

        return yPosition - 20;
    }

//    private int drawLink(PDPageContentStream contentStream, int yPosition,
//                         String dsfModuleLink) throws IOException {
//        String linkText = "Login to the DSF module";
//        float fontSize = 12;
//
//        // Draw blue underlined text
//        contentStream.beginText();
//        contentStream.setNonStrokingColor(0, 0, 255); // Blue color
//        contentStream.setFont(PDType1Font.HELVETICA, fontSize);
//        contentStream.moveTextPositionByAmount(MARGIN, yPosition);
//        contentStream.drawString(linkText);
//        contentStream.endText();
//
//        // Draw underline
//        float linkWidth = PDType1Font.HELVETICA.getStringWidth(linkText) / 1000 * fontSize;
//        contentStream.setStrokingColor(0, 0, 255); // Blue color
//        contentStream.setLineWidth(1);
//        contentStream.moveTo(MARGIN, yPosition - 2);
//        contentStream.lineTo(MARGIN + linkWidth, yPosition - 2);
//        contentStream.stroke();
//
//        // Create clickable link annotation
//        PDAnnotationLink link = new PDAnnotationLink();
//        PDRectangle position = new PDRectangle();
//        position.setLowerLeftX(MARGIN);
//        position.setLowerLeftY(yPosition - 2);
//        position.setUpperRightX(MARGIN + linkWidth);
//        position.setUpperRightY(yPosition + fontSize);
//        link.setRectangle(position);
//
//        // Set link action
//        PDActionURI action = new PDActionURI();
//        action.setURI(dsfModuleLink);
//        link.setAction(action);
//
//        // Add border style (optional - makes link visible on hover)
//        PDBorderStyleDictionary borderStyle = new PDBorderStyleDictionary();
//        borderStyle.setWidth(0); // No border
//        link.setBorderStyle(borderStyle);
//
//        // Add annotation to page
////        page.getAnnotations().add(link);
//
//        // Reset color to black for subsequent text
//        contentStream.setNonStrokingColor(0, 0, 0);
//
//        return yPosition - 40;
//    }

    /**
     * Utility method to wrap text to fit within specified width
     */
    private List<String> wrapText(String text, PDFont font, int fontSize, int maxWidth) throws IOException {
        List<String> lines = new ArrayList<String>();

        if (text == null || text.isEmpty()) {
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float textWidth = font.getStringWidth(testLine) / 1000 * fontSize;

            if (textWidth > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

}
