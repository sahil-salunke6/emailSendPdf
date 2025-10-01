package com.ss.emailSendPdf.service;

import com.ss.emailSendPdf.model.TestObject;
import com.ss.emailSendPdf.model.enums.TemplateType;
import com.ss.emailSendPdf.pdfGeneration.EmailGenerationRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfBoxService pdfBoxService;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, PdfBoxService pdfBoxService, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.pdfBoxService = pdfBoxService;
        this.templateEngine = templateEngine;
    }

    public void sendEmail(EmailGenerationRequest request) throws Exception {
        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("request", request);

        // Render HTML from template
        String htmlContent = templateEngine.process(TemplateType.DSF_FEE_PENDING_FOR_APPROVAL.name(), context);
        // email.html must be inside src/main/resources/templates

        // Create email message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("use.nothing@yahoo.com");
        helper.setTo(request.getRequest().getUser().getEmail()); // recipient from request object
        helper.setSubject("Ticket " + request.getRequest().getIncident().getTicketNumber() + " Closed");
        helper.setText(htmlContent, true); // true = HTML

        // Send email
        mailSender.send(message);
    }
}
