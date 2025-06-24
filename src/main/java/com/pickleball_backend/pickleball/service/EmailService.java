package com.pickleball_backend.pickleball.service;

import com.pickleball_backend.pickleball.entity.Booking;
import com.pickleball_backend.pickleball.entity.Court;
import com.pickleball_backend.pickleball.entity.Slot;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalTime;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // Fixed to actually send emails for booking confirmations
    public void sendBookingConfirmation(String email, Booking booking, Court court, Slot slot) {
        if (email == null || booking == null || court == null || slot == null) {
            log.error("Missing parameters for booking confirmation email");
            return;
        }

        String subject = "Court Booking Confirmation";
        double duration = Duration.between(
                slot.getStartTime() != null ? slot.getStartTime() : LocalTime.MIN,
                slot.getEndTime() != null ? slot.getEndTime() : LocalTime.MIN
        ).toMinutes() / 60.0;

        String content = String.format(
                "Your booking is confirmed!\n\n" +
                        "Court: %s\n" +
                        "Location: %s\n" +
                        "Date: %s\n" +
                        "Time: %s - %s\n" +
                        "Duration: %.1f hours\n" +
                        "Amount: $%.2f\n" +
                        "Purpose: %s\n" +
                        "Players: %d\n" +
                        "Booking ID: %d",
                court.getName() != null ? court.getName() : "N/A",
                court.getLocation() != null ? court.getLocation() : "N/A",
                slot.getDate() != null ? slot.getDate() : "N/A",
                slot.getStartTime() != null ? slot.getStartTime() : "N/A",
                slot.getEndTime() != null ? slot.getEndTime() : "N/A",
                duration,
                booking.getTotalAmount(),
                booking.getPurpose() != null ? booking.getPurpose() : "N/A",
                booking.getNumberOfPlayers() != null ? booking.getNumberOfPlayers() : 0,
                booking.getId()
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(content);

        try {
            javaMailSender.send(message);
            log.info("Booking confirmation email sent to: {}", email);
        } catch (MailException e) {
            log.error("Failed to send booking confirmation to {}: {}", email, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, click the link below:\n" + resetLink);

        try {
            javaMailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.");
        }
    }

    public void sendVoucherEmail(String toEmail, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);

        try {
            javaMailSender.send(message);
            log.info("Voucher email sent to: {}", toEmail);
        } catch (MailException e) {
            log.error("Failed to send voucher email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send voucher email");
        }
    }

    //Cancel Booking
    public void sendCancellationConfirmation(String email, Booking booking, Slot slot, Court court) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Booking Cancellation Request Received");

        String content = String.format(
                "Your cancellation request for booking #%d has been received.\n\n" +
                        "Court: %s\n" +
                        "Date: %s\n" +
                        "Time: %s - %s\n\n" +
                        "We'll process your request shortly.",
                booking.getId(),
                court.getName(), // Use court.getName() here
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime()
        );

        message.setText(content);

        try {
            javaMailSender.send(message);
            log.info("Cancellation confirmation sent to: {}", email);
        } catch (MailException e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

    public void sendCancellationDecision(String email, Booking booking, Slot slot, String courtName, boolean approved) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Cancellation Request " + (approved ? "Approved" : "Rejected"));

        String content = String.format(
                "Your cancellation request for booking #%d has been %s.\n\n" +
                        "Court: %s\nDate: %s\nTime: %s - %s\n\n" +
                        (approved ? "The slot has been freed up." : "Your booking remains confirmed."),
                booking.getId(),
                approved ? "APPROVED" : "REJECTED",
                courtName,
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime()
        );

        message.setText(content);

        try {
            javaMailSender.send(message);
            log.info("Cancellation decision sent to: {}", email);
        } catch (MailException e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

}