package uk.co.motors;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;

public class BesedoServiceCheck {

    private static final String HEALTH_CHECK_URL = "https://api.implio.com/v1/health";
    private static final String EXPECTED_RESPONSE = "{\"status\":\"alive\"}";
    private static final int EXPECTED_STATUS_CODE = 200;

    private final String emailUsername;
    private final String emailPassword;
    private final String recipientEmail;

    public BesedoServiceCheck(String emailUsername, String emailPassword, String recipientEmail) {
        this.emailUsername = emailUsername;
        this.emailPassword = emailPassword;
        this.recipientEmail = recipientEmail;
    }

    public void checkHealthAndAlert() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HEALTH_CHECK_URL))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == EXPECTED_STATUS_CODE) {
                String responseBody = response.body().trim();
                if (responseBody.equals(EXPECTED_RESPONSE)) {
                    System.out.println("Besedo Health Checksuccessful.");
                    return; // No need to send email
                } else {
                    System.err.println("Besedo Health Checkfailed - Unexpected response body: " + responseBody);
                    sendAlertEmail("Besedo Health CheckFailed - Unexpected Response Body",
                            "Expected: " + EXPECTED_RESPONSE + "\nReceived: " + responseBody);
                }
            } else {
                System.err.println("Besedo Health Checkfailed - HTTP status code: " + response.statusCode());
                sendAlertEmail("Besedo Health CheckFailed - HTTP Status Code Error",
                        "Received status code: " + response.statusCode());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error during health check: " + e.getMessage());
            sendAlertEmail("Besedo Health CheckError", "An error occurred while performing the health check: " + e.getMessage());
        }
    }

    private void sendAlertEmail(String subject, String body) {
        // Configure email properties
        Properties props = new Properties();
        props.put("mail.smtp.host", System.getenv("BSC_SEND_SERVER"));
        props.put("mail.smtp.port", System.getenv("BSC_SEND_SERVER_PORT")); 
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Use TLS

        // Create a Session object with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUsername, emailPassword);
            }
        });

        try {
            // Create a new email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);

            // Send the email
            Transport.send(message);

            System.out.println("Alert email sent successfully to: " + recipientEmail);

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String emailUsername = System.getenv("BSC_SEND_FROM");
        String emailPassword = System.getenv("BSC_SEND_PASSWORD");
        String recipientEmail = System.getenv("BSC_SEND_TO");

        BesedoServiceCheck service = new BesedoServiceCheck(emailUsername, emailPassword, recipientEmail);

        // Run the Besedo Health Checkperiodically (e.g., every minute)
        while (true) {
            service.checkHealthAndAlert();
            try {
                Thread.sleep(60000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
