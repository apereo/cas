package org.apereo.cas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@Slf4j
public class PullRequestWebhookController {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${casbot.github.repository.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/webhooks/pullrequests")
    public ResponseEntity<?> handlePullRequestWebhook(HttpServletRequest request) throws Exception {
        var rawBody = StreamUtils.copyToByteArray(request.getInputStream());
        var event = request.getHeader("X-GitHub-Event");
        var deliveryId = request.getHeader("X-GitHub-Delivery");
        var signature = request.getHeader("X-Hub-Signature-256");

        if ("ping".equals(event)) {
            return ResponseEntity.ok("pong");
        }

        if (!"pull_request".equals(event)) {
            return ResponseEntity.accepted().body("Ignored event: " + event);
        }

        if (!isValidSignature(rawBody, signature)) {
            return ResponseEntity.status(401).body("Invalid GitHub signature");
        }

        var payload = objectMapper.readTree(rawBody);
        var action = payload.path("action").asText();
        var pullRequest = payload.path("pull_request");
        var repository = payload.path("repository");
        var prEvent = new PullRequestEvent(
                deliveryId,
                action,
                repository.path("full_name").asText(),
                pullRequest.path("number").asInt(),
                pullRequest.path("title").asText(),
                pullRequest.path("html_url").asText(),
                pullRequest.path("state").asText(),
                pullRequest.path("merged").asBoolean(false),
                pullRequest.path("draft").asBoolean(false),
                pullRequest.path("user").path("login").asText(),
                pullRequest.path("base").path("ref").asText(),
                pullRequest.path("head").path("ref").asText(),
                pullRequest.path("head").path("sha").asText()
        );

        handlePullRequestEvent(prEvent);

        return ResponseEntity.ok("Processed pull request event");
    }

    private static void handlePullRequestEvent(PullRequestEvent event) {
        switch (event.action()) {
            case "opened", "reopened" -> handleOpened(event);
        }
    }

    private static void handleOpened(PullRequestEvent event) {
        System.out.println("PR opened: " + event);
    }

    private boolean isValidSignature(byte[] rawBody, String signatureHeader) throws Exception {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) {
            return false;
        }
        var expectedSignature = "sha256=" + hmacSha256Hex(rawBody, webhookSecret);
        var expectedBytes = expectedSignature.getBytes(StandardCharsets.UTF_8);
        var actualBytes = signatureHeader.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expectedBytes, actualBytes);
    }

    private static String hmacSha256Hex(byte[] body, String secret) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        var secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        mac.init(secretKey);
        var digest = mac.doFinal(body);
        return HexFormat.of().formatHex(digest);

    }

    public record PullRequestEvent(
            String deliveryId,
            String action,
            String repository,
            int pullRequestNumber,
            String title,
            String url,
            String state,
            boolean merged,
            boolean draft,
            String author,
            String baseBranch,
            String headBranch,
            String headSha
    ) {
    }
    
}
