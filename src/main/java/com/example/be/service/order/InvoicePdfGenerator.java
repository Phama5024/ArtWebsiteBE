package com.example.be.service.order;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InvoicePdfGenerator {

    private final TemplateEngine templateEngine;

    private static final Logger log = LoggerFactory.getLogger(InvoicePdfGenerator.class);

    public byte[] generate(String templateName, Map<String, Object> model) {
        try {
            Context ctx = new Context();
            ctx.setVariables(model);

            String html = templateEngine.process(templateName, ctx);

            String xhtml = toWellFormedXhtml(html);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.useFastMode();

            String baseUri = new ClassPathResource("").getURL().toExternalForm();
            builder.withHtmlContent(xhtml, baseUri);

            addVietnameseFonts(builder);

            builder.toStream(out);
            builder.run();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Generate invoice pdf failed (root cause)", e);
            throw new RuntimeException("Generate invoice pdf failed", e);
        }
    }

    private String toWellFormedXhtml(String html) {
        if (html == null) return "";

        String s = html.replace("\uFEFF", "").trim();

        s = s.replaceAll("(?i)<meta([^>/]*?)>", "<meta$1/>");

        s = s.replaceAll("(?i)<br>", "<br/>");
        s = s.replaceAll("(?i)<hr>", "<hr/>");
        s = s.replaceAll("(?i)<img([^>/]*?)>", "<img$1/>");

        return s;
    }

    private void addVietnameseFonts(PdfRendererBuilder builder) {
        try {
            ClassPathResource regular = new ClassPathResource("fonts/NotoSans-Regular.ttf");
            if (regular.exists()) {
                builder.useFont(() -> openFreshStream(regular), "Noto Sans", 400,
                        PdfRendererBuilder.FontStyle.NORMAL, true);
            }

            ClassPathResource bold = new ClassPathResource("fonts/NotoSans-Bold.ttf");
            if (bold.exists()) {
                builder.useFont(() -> openFreshStream(bold), "Noto Sans", 700,
                        PdfRendererBuilder.FontStyle.NORMAL, true);
            }
        } catch (Exception e) {
            log.warn("Could not load Vietnamese fonts for invoice PDF: {}", e.getMessage());
        }
    }

    private InputStream openFreshStream(ClassPathResource res) {
        try {
            return res.getInputStream();
        } catch (Exception e) {
            throw new RuntimeException("Cannot open font stream: " + res.getPath(), e);
        }
    }
}
