package com.example.springmail.controller;

import com.example.springmail.dto.AccessReviewReminderRequest;
import com.example.springmail.dto.SendEmailRequest;
import com.example.springmail.service.HtmlEmailService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final HtmlEmailService htmlEmailService;

    public EmailController(HtmlEmailService htmlEmailService) {
        this.htmlEmailService = htmlEmailService;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@Valid @RequestBody SendEmailRequest request)
            throws MessagingException, IOException {
        Map<String, String> variables = new HashMap<>();
        if (request.getRecipientName() != null) {
            variables.put("name", request.getRecipientName());
        }

        htmlEmailService.sendHtmlEmail(
                request.getTo(),
                request.getSubject(),
                request.getTemplatePath(),
                variables);

        return ResponseEntity.ok(Map.of("status", "sent"));
    }

    @PostMapping("/access-review-reminder")
    public ResponseEntity<Map<String, String>> sendAccessReviewReminder(
            @Valid @RequestBody AccessReviewReminderRequest request)
            throws MessagingException, IOException {
        Map<String, String> variables = Map.of(
                "username", request.getUsername(),
                "expireDate", request.getExpireDate(),
                "persona", request.getPersona());

        String subject = request.getSubject() != null
                ? request.getSubject()
                : "Access Review Reminder - action required by " + request.getExpireDate();

        htmlEmailService.sendHtmlEmail(
                request.getTo(),
                subject,
                "templates/access-review-reminder.html",
                variables);

        return ResponseEntity.ok(Map.of("status", "sent"));
    }
}
