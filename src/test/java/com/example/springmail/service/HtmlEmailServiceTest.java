package com.example.springmail.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HtmlEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private HtmlEmailService htmlEmailService;

    @BeforeEach
    void setUp() {
        htmlEmailService = new HtmlEmailService(mailSender, "sender@example.com");
    }

    @Test
    void loadAndRenderTemplate_replacesPlaceholders() throws Exception {
        String rendered = htmlEmailService.loadAndRenderTemplate(
                "templates/welcome.html",
                Map.of("name", "Alice"));

        assertThat(rendered).contains("Hello, Alice!");
        assertThat(rendered).contains("cid:logo");
        assertThat(rendered).doesNotContain("{{name}}");
    }

    @Test
    void loadAccessReviewReminderTemplate_replacesAllFields() throws Exception {
        String rendered = htmlEmailService.loadAndRenderTemplate(
                "templates/access-review-reminder.html",
                Map.of(
                        "username", "jdoe",
                        "expireDate", "2026-06-30",
                        "persona", "Data Analyst",
                        "reviewUrl", "https://portal.example.com/access-review"));

        assertThat(rendered).contains("jdoe");
        assertThat(rendered).contains("2026-06-30");
        assertThat(rendered).contains("Data Analyst");
        assertThat(rendered).contains("href=\"https://portal.example.com/access-review\"");
        assertThat(rendered).contains("Review Access Now");
        assertThat(rendered).contains("arcsize=\"60%\"");
        assertThat(rendered).contains("stroke=\"f\"");
        assertThat(rendered).contains("cid:java");
        assertThat(rendered).contains("cid:user-access-review-slide3");
        assertThat(rendered).doesNotContain("{{username}}", "{{expireDate}}", "{{persona}}", "{{reviewUrl}}");
    }

    @Test
    void resolveInlineImages_findsAccessReviewSlideImage() {
        var images = htmlEmailService.resolveInlineImages(
                "<img src=\"cid:user-access-review-slide3\" />");

        assertThat(images).containsKey("user-access-review-slide3");
        assertThat(images.get("user-access-review-slide3").exists()).isTrue();
    }

    @Test
    void resolveInlineImages_findsLogoAndJavaFromClasspath() {
        var images = htmlEmailService.resolveInlineImages(
                "<img src=\"cid:logo\" /><img src=\"cid:java\" />");

        assertThat(images).containsKeys("logo", "java");
        assertThat(images.get("logo").exists()).isTrue();
        assertThat(images.get("java").exists()).isTrue();
    }

    @Test
    void sendHtmlEmail_buildsMimeMessage() throws Exception {
        when(mailSender.createMimeMessage())
                .thenAnswer(invocation -> new MimeMessage(Session.getDefaultInstance(new Properties())));

        htmlEmailService.sendHtmlEmail(
                "recipient@example.com",
                "Welcome",
                "templates/welcome.html",
                Map.of("name", "Bob"));

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).contains("recipient@example.com");
        assertThat(message.getSubject()).isEqualTo("Welcome");
    }
}
