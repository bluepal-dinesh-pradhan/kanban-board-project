package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
@Slf4j
public class EmailService {

//    @Autowired(required = false)
//    private JavaMailSender mailSender;
		private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@kanbanboard.com}")
    private String fromEmail;

    @Value("${app.mail.frontend-url:http://localhost:5173}")
    private String frontendUrl;
    
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendBoardInvitation(String toEmail, String inviterName, String boardTitle, String role, boolean userExists) {
        log.info("Sending board invitation email to {}", toEmail);
        log.debug("Email enabled={}, mailSenderPresent={}", mailEnabled, mailSender != null);
        if (!mailEnabled || mailSender == null) {
            log.warn("Email disabled. Invitation for {} saved to DB only.", toEmail);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            if (userExists) {
                helper.setSubject("You've been added to \"" + boardTitle + "\" on Kanban Board");
                helper.setText(buildMemberAddedHtml(toEmail, inviterName, boardTitle, role), true);
            } else {
                helper.setSubject("You've been invited to join \"" + boardTitle + "\" on Kanban Board");
                helper.setText(buildInvitationHtml(inviterName, boardTitle, role, toEmail), true);
            }
            
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    public boolean sendDueDateReminder(String toEmail, String userName, String cardTitle, String boardTitle, LocalDate dueDate) {
        log.info("Sending due date reminder email to {}", toEmail);
        log.debug("Email enabled={}, mailSenderPresent={}", mailEnabled, mailSender != null);
        if (!mailEnabled || mailSender == null) {
            log.warn("Email disabled. Due date reminder for {} not sent.", toEmail);
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Due Date Reminder: " + cardTitle);
            helper.setText(buildDueDateReminderHtml(userName, cardTitle, boardTitle, dueDate), true);
            
            mailSender.send(message);
            log.info("Due date reminder email sent successfully to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send due date reminder email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private String buildInvitationHtml(String inviterName, String boardTitle, String role, String toEmail) {
        String encodedEmail = URLEncoder.encode(toEmail, StandardCharsets.UTF_8);
        String registerUrl = frontendUrl + "/register?email=" + encodedEmail + "&invited=true";
        String loginUrl = frontendUrl + "/login?invited=true";
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Board Invitation</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8fafc;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white;">
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #3b82f6 0%%, #1d4ed8 100%%); padding: 40px 20px; text-align: center;">
                        <div style="background-color: white; width: 60px; height: 60px; border-radius: 12px; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center;">
                            <div style="width: 32px; height: 32px; background: linear-gradient(135deg, #3b82f6 0%%, #1d4ed8 100%%); border-radius: 6px;"></div>
                        </div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">Kanban Board</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 16px;">Organize your work, your way</p>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 40px 20px;">
                        <h2 style="color: #1f2937; margin: 0 0 20px; font-size: 24px; font-weight: 600;">You've been invited!</h2>
                        
                        <p style="color: #4b5563; margin: 0 0 20px; font-size: 16px; line-height: 1.6;">
                            <strong>%s</strong> has invited you to collaborate on "<strong>%s</strong>" as a <strong>%s</strong>.
                        </p>
                        
                        <p style="color: #6b7280; margin: 0 0 30px; font-size: 14px; line-height: 1.5;">
                            Join the board to start organizing tasks, tracking progress, and collaborating with your team.
                        </p>
                        
                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #3b82f6 0%%, #1d4ed8 100%%); color: white; text-decoration: none; padding: 16px 32px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);">
                                Accept Invitation
                            </a>
                        </div>
                        
                        <p style="color: #9ca3af; margin: 30px 0 0; font-size: 14px; text-align: center;">
                            Already have an account? 
                            <a href="%s" style="color: #3b82f6; text-decoration: underline;">Sign in</a>
                        </p>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f9fafb; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;">
                        <p style="color: #6b7280; margin: 0; font-size: 12px;">
                            This invitation was sent by %s via Kanban Board
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(inviterName, boardTitle, role.toLowerCase(), registerUrl, loginUrl, inviterName);
    }

    private String buildMemberAddedHtml(String toEmail, String inviterName, String boardTitle, String role) {
        String encodedEmail = URLEncoder.encode(toEmail, StandardCharsets.UTF_8);
        String loginUrl = frontendUrl + "/login?email=" + encodedEmail + "&invited=true";
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Added to Board</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8fafc;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white;">
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); padding: 40px 20px; text-align: center;">
                        <div style="background-color: white; width: 60px; height: 60px; border-radius: 12px; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center;">
                            <div style="width: 32px; height: 32px; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); border-radius: 6px;"></div>
                        </div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">Kanban Board</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 16px;">Organize your work, your way</p>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 40px 20px;">
                        <h2 style="color: #1f2937; margin: 0 0 20px; font-size: 24px; font-weight: 600;">You've been added to a board!</h2>
                        
                        <p style="color: #4b5563; margin: 0 0 20px; font-size: 16px; line-height: 1.6;">
                            <strong>%s</strong> has added you to "<strong>%s</strong>" as a <strong>%s</strong>.
                        </p>
                        
                        <p style="color: #6b7280; margin: 0 0 30px; font-size: 14px; line-height: 1.5;">
                            You can now access the board and start collaborating with your team.
                        </p>
                        
                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: white; text-decoration: none; padding: 16px 32px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);">
                                View Board
                            </a>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f9fafb; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;">
                        <p style="color: #6b7280; margin: 0; font-size: 12px;">
                            You were added by %s via Kanban Board
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(inviterName, boardTitle, role.toLowerCase(), loginUrl, inviterName);
    }

    private String buildDueDateReminderHtml(String userName, String cardTitle, String boardTitle, LocalDate dueDate) {
        String boardUrl = frontendUrl + "/boards";
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Due Date Reminder</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8fafc;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white;">
                    <!-- Header -->
                    <div style="background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); padding: 40px 20px; text-align: center;">
                        <div style="background-color: white; width: 60px; height: 60px; border-radius: 12px; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center;">
                            <div style="width: 32px; height: 32px; background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); border-radius: 6px;"></div>
                        </div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">Due Date Reminder</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 16px;">Don't miss your deadline</p>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding: 40px 20px;">
                        <h2 style="color: #1f2937; margin: 0 0 20px; font-size: 24px; font-weight: 600;">Hi %s,</h2>
                        
                        <p style="color: #4b5563; margin: 0 0 20px; font-size: 16px; line-height: 1.6;">
                            This is a reminder that your card "<strong>%s</strong>" in board "<strong>%s</strong>" is due on <strong>%s</strong>.
                        </p>
                        
                        <p style="color: #6b7280; margin: 0 0 30px; font-size: 14px; line-height: 1.5;">
                            Click the button below to view your board and update the card status.
                        </p>
                        
                        <!-- CTA Button -->
                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%); color: white; text-decoration: none; padding: 16px 32px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);">
                                View Board
                            </a>
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div style="background-color: #f9fafb; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;">
                        <p style="color: #6b7280; margin: 0; font-size: 12px;">
                            This reminder was sent by Kanban Board
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, cardTitle, boardTitle, dueDate.toString(), boardUrl);
    }
    public boolean sendPasswordResetEmail(String toEmail, String token) {
        log.info("Sending password reset email to {}", toEmail);
        if (!mailEnabled || mailSender == null) {
            log.warn("Email disabled. Password reset for {} not sent. Token: {}", toEmail, token);
            return false;
        }

        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset Your Password - Kanban Board");
            helper.setText(buildPasswordResetHtml(resetUrl), true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private String buildPasswordResetHtml(String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Reset Password</title>
            </head>
            <body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #f8fafc;">
                <div style="max-width: 600px; margin: 0 auto; background-color: white;">
                    <div style="background: linear-gradient(135deg, #3b82f6 0%%, #1d4ed8 100%%); padding: 40px 20px; text-align: center;">
                        <div style="background-color: white; width: 60px; height: 60px; border-radius: 12px; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center;">
                            <div style="width: 32px; height: 32px; background: linear-gradient(135deg, #3b82f6 0%%, #1d4ed8 100%%); border-radius: 6px;"></div>
                        </div>
                        <h1 style="color: white; margin: 0; font-size: 28px; font-weight: 700;">Kanban Board</h1>
                        <p style="color: rgba(255,255,255,0.9); margin: 8px 0 0; font-size: 16px;">Password Reset Request</p>
                    </div>

                    <div style="padding: 40px 20px;">
                        <h2 style="color: #1f2937; margin: 0 0 20px; font-size: 24px; font-weight: 600;">Reset Your Password</h2>

                        <p style="color: #4b5563; margin: 0 0 20px; font-size: 16px; line-height: 1.6;">
                            We received a request to reset your password. Click the button below to set a new password.
                        </p>

                        <p style="color: #6b7280; margin: 0 0 30px; font-size: 14px; line-height: 1.5;">
                            This link will expire in <strong>1 hour</strong>. If you didn't request a password reset, you can safely ignore this email.
                        </p>

                        <div style="text-align: center; margin: 30px 0;">
                            <a href="%s" style="display: inline-block; background: linear-gradient(135deg, #3b82f6 0%%, #1d4ed8 100%%); color: white; text-decoration: none; padding: 16px 32px; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);">
                                Reset Password
                            </a>
                        </div>

                        <p style="color: #9ca3af; margin: 30px 0 0; font-size: 12px; text-align: center;">
                            If the button doesn't work, copy and paste this link into your browser:<br>
                            <a href="%s" style="color: #3b82f6; word-break: break-all;">%s</a>
                        </p>
                    </div>

                    <div style="background-color: #f9fafb; padding: 20px; text-align: center; border-top: 1px solid #e5e7eb;">
                        <p style="color: #6b7280; margin: 0; font-size: 12px;">
                            This email was sent by Kanban Board. If you didn't request this, please ignore it.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetUrl, resetUrl, resetUrl);
    }

}
