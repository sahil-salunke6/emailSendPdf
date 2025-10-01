package com.ss.emailSendPdf.controller;

import com.ss.emailSendPdf.model.enums.TemplateType;
import com.ss.emailSendPdf.pdfGeneration.EmailGenerationRequest;
import com.ss.emailSendPdf.service.EmailService;
import com.ss.emailSendPdf.service.OpenHtmlPdfService;
import com.ss.emailSendPdf.service.PdfBoxService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/email")
public class EmailController {
    private final EmailService emailService;
    private final PdfBoxService pdfBoxService;
    private final OpenHtmlPdfService pdfService;

    public EmailController(EmailService emailService, PdfBoxService pdfBoxService, OpenHtmlPdfService pdfService) {
        this.emailService = emailService;
        this.pdfBoxService = pdfBoxService;
        this.pdfService = pdfService;
    }

    /**
     * POST /api/email
     * Body: { request: TestObject, action: "send" | "pdf" }
     */
    @PostMapping
    public ResponseEntity<?> handleEmail(@RequestBody EmailGenerationRequest emailGenerationRequest) {
        try {
            if ("send".equalsIgnoreCase(emailGenerationRequest.getAction())) {
                emailService.sendEmail(emailGenerationRequest);
                return ResponseEntity.ok("Email sent");
            } else if ("pdfBox".equalsIgnoreCase(emailGenerationRequest.getAction())) {
                pdfBoxService.generateEmailTemplatePDF(TemplateType.DSF_FEE_REJECTED,
                        emailGenerationRequest);
                return ResponseEntity.ok("PDF generated using PDFBox");
            } else if ("openPdf".equalsIgnoreCase(emailGenerationRequest.getAction())) {
                pdfService.generatePdf(emailGenerationRequest, TemplateType.DSF_FEE_REJECTED.name());
                return ResponseEntity.ok("PDF generated using OpenPDF");
            } else {
                return ResponseEntity.badRequest().body("action must be 'send' or 'pdf'");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("error: " + e.getMessage());
        }
    }
}