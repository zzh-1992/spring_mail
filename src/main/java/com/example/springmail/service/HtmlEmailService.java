package com.example.springmail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HtmlEmailService {

    private static final Pattern CID_PATTERN = Pattern.compile("cid:([\\w.-]+)");
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([\\w.-]+)}}");

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public HtmlEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public void sendHtmlEmail(String to, String subject, String templateClasspath, Map<String, String> variables)
            throws MessagingException, IOException {
        String htmlBody = loadAndRenderTemplate(templateClasspath, variables);
        Map<String, Resource> inlineImages = resolveInlineImages(htmlBody);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);

        for (Map.Entry<String, Resource> image : inlineImages.entrySet()) {
            helper.addInline(image.getKey(), image.getValue());
        }

        mailSender.send(message);
    }

    String loadAndRenderTemplate(String templateClasspath, Map<String, String> variables) throws IOException {
        String normalizedPath = normalizeClasspath(templateClasspath);
        Resource templateResource = new ClassPathResource(normalizedPath);
        if (!templateResource.exists()) {
            throw new IllegalArgumentException("HTML template not found on classpath: " + normalizedPath);
        }

        String html;
        try (InputStream inputStream = templateResource.getInputStream()) {
            html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }

        if (variables == null || variables.isEmpty()) {
            return html;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(html);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = variables.getOrDefault(key, "");
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    Map<String, Resource> resolveInlineImages(String htmlBody) {
        Map<String, Resource> images = new LinkedHashMap<>();
        Matcher matcher = CID_PATTERN.matcher(htmlBody);

        while (matcher.find()) {
            String contentId = matcher.group(1);
            if (images.containsKey(contentId)) {
                continue;
            }

            Resource imageResource = findImageResource(contentId);
            if (!imageResource.exists()) {
                throw new IllegalArgumentException(
                        "Inline image not found for cid:" + contentId + " (expected under classpath:images/)");
            }
            images.put(contentId, imageResource);
        }

        return images;
    }

    private Resource findImageResource(String contentId) {
        String[] extensions = {".png", ".jpg", ".jpeg", ".gif", ".webp"};
        for (String extension : extensions) {
            Resource resource = new ClassPathResource("images/" + contentId + extension);
            if (resource.exists()) {
                return resource;
            }
        }
        return new ClassPathResource("images/" + contentId);
    }

    private String normalizeClasspath(String templateClasspath) {
        String path = templateClasspath.trim();
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException("Template path must not be blank");
        }
        return path;
    }
}
