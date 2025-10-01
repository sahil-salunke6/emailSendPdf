package com.ss.emailSendPdf.service;

import com.ss.emailSendPdf.model.enums.TemplateType;
import com.ss.emailSendPdf.pdfGeneration.EmailGenerationRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


@Service
public class OpenHtmlPdfService {

    private static final String OUTPUT_DIR = "E:/WorkPlace/Java/Sample Projects/emailSendPdf/pdf/";

    public byte[] generatePdf(EmailGenerationRequest request, String emailTemplate) throws Exception {
        // Load template HTML
        ClassPathResource resource = new ClassPathResource("templates/" + emailTemplate +
                ".html");
        InputStream inputStream = resource.getInputStream();
        String htmlContent = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

        // prepare placeholders
        Map<String, String> values = new HashMap<>();
        values.put("makerName",
                request.getRequest().getUser().getFirstName() + " " + request.getRequest().getUser().getLastName());
        values.put("checkerName",
                request.getRequest().getUser().getFirstName() + " " + request.getRequest().getUser().getLastName());
        values.put("programName", request.getRequest().getDsfSetup().getProgramName());
        values.put("rejectionReason", request.getRequest().getDsfSetup().getRejectionReason());
        values.put("dsfModuleLink", request.getRequest().getDsfSetup().getDsfModuleLink());


        // Replace placeholders
        for (Map.Entry<String, String> entry : values.entrySet()) {
            htmlContent = htmlContent.replace("{{" + entry.getKey() + "}}",
                    entry.getValue() != null ? entry.getValue() : "");
        }

        // Fix self-closing tags for XHTML compliance
        htmlContent = htmlContent
                .replaceAll("<meta([^>]*)>", "<meta$1/>")
                .replaceAll("<link([^>]*)>", "<link$1/>")
                .replaceAll("<br>", "<br/>")
                .replaceAll("<hr>", "<hr/>")
                .replaceAll("<img([^>]*)>", "<img$1/>");


        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) dir.mkdirs();

        // Output file
        File pdfFile = new File(dir, "OpenPdf-" + emailTemplate + System.currentTimeMillis() + ".pdf");

        try (FileOutputStream os = new FileOutputStream(pdfFile)) {
            // Generate PDF
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(os);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error generating PDF: " + e.getMessage());
        }
    }


}