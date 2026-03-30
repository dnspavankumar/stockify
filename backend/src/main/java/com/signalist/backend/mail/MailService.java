package com.signalist.backend.mail;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.signalist.backend.config.AppProperties;
import com.signalist.backend.finnhub.FinnhubDtos;
import com.signalist.backend.user.UserDocument;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private static final DateTimeFormatter NEWS_DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu").withZone(ZoneOffset.UTC);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    public MailService(JavaMailSender mailSender, AppProperties appProperties) {
        this.mailSender = mailSender;
        this.appProperties = appProperties;
    }

    public void sendWelcomeEmail(UserDocument user) {
        if (!mailEnabled() || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        String intro = buildWelcomeIntro(user);
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <body style="margin:0;padding:32px;background:#050505;font-family:Segoe UI,Arial,sans-serif;color:#CCDADC;">
                  <div style="max-width:600px;margin:0 auto;background:#141414;border:1px solid #30333A;border-radius:12px;overflow:hidden;">
                    <div style="padding:32px 32px 12px 32px;">
                      <h1 style="margin:0 0 18px 0;color:#FDD458;font-size:28px;">Welcome aboard %s</h1>
                      <p style="margin:0 0 24px 0;font-size:16px;line-height:1.7;color:#CCDADC;">%s</p>
                      <p style="margin:0 0 12px 0;font-size:16px;font-weight:600;">Here's what you can do right now:</p>
                      <ul style="margin:0 0 24px 18px;padding:0;line-height:1.8;">
                        <li>Set up your watchlist to follow your favorite stocks</li>
                        <li>Create price alerts so you never miss a move</li>
                        <li>Explore the dashboard for trends and market news</li>
                      </ul>
                      <a href="%s" style="display:inline-block;background:#FDD458;color:#111;text-decoration:none;padding:14px 22px;border-radius:8px;font-weight:600;">Go to Dashboard</a>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(user.getName()),
                intro,
                HtmlUtils.htmlEscape(appProperties.getFrontendBaseUrl())
        );

        sendHtmlMail(user.getEmail(), "Welcome to Signalist - your stock market toolkit is ready!", html);
    }

    public void sendDailySummaryEmail(UserDocument user, List<FinnhubDtos.MarketNewsArticle> articles) {
        if (!mailEnabled() || user.getEmail() == null || user.getEmail().isBlank()) {
            return;
        }

        StringBuilder newsSections = new StringBuilder();
        if (articles == null || articles.isEmpty()) {
            newsSections.append("<p style=\"margin:0;font-size:16px;line-height:1.7;color:#CCDADC;\">No market news is available today. Please check back tomorrow.</p>");
        } else {
            for (FinnhubDtos.MarketNewsArticle article : articles) {
                newsSections.append(renderArticle(article));
            }
        }

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <body style="margin:0;padding:32px;background:#050505;font-family:Segoe UI,Arial,sans-serif;color:#CCDADC;">
                  <div style="max-width:680px;margin:0 auto;background:#141414;border:1px solid #30333A;border-radius:12px;overflow:hidden;">
                    <div style="padding:32px;">
                      <h1 style="margin:0 0 8px 0;color:#FDD458;font-size:28px;">Market News Summary Today</h1>
                      <p style="margin:0 0 28px 0;color:#9CA3AF;font-size:14px;">%s</p>
                      %s
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(NEWS_DATE_FORMAT.format(Instant.now()), newsSections);

        sendHtmlMail(user.getEmail(), "Market News Summary Today - " + NEWS_DATE_FORMAT.format(Instant.now()), html);
    }

    private boolean mailEnabled() {
        return appProperties.getMail().isEnabled();
    }

    private void sendHtmlMail(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_NO, StandardCharsets.UTF_8.name());
            helper.setFrom(appProperties.getMail().getFromAddress(), appProperties.getMail().getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
        } catch (Exception exception) {
            logger.warn("Failed to send mail to {}", to, exception);
        }
    }

    private String buildWelcomeIntro(UserDocument user) {
        String goals = escapeOrDefault(user.getInvestmentGoals(), "your investing goals");
        String risk = escapeOrDefault(user.getRiskTolerance(), "your risk level");
        String industry = escapeOrDefault(user.getPreferredIndustry(), "the market segments you care about");
        return "Thanks for joining Signalist. We'll help you stay focused on <strong>%s</strong> with a <strong>%s</strong> strategy while keeping a close eye on <strong>%s</strong> opportunities."
                .formatted(goals, risk, industry);
    }

    private String renderArticle(FinnhubDtos.MarketNewsArticle article) {
        String headline = HtmlUtils.htmlEscape(article.headline());
        String summary = HtmlUtils.htmlEscape(article.summary());
        String source = HtmlUtils.htmlEscape(article.source());
        String related = article.related() == null || article.related().isBlank() ? "" : "<p style=\"margin:0 0 12px 0;color:#FDD458;font-size:14px;font-weight:600;\">Related: " + HtmlUtils.htmlEscape(article.related()) + "</p>";
        String url = HtmlUtils.htmlEscape(article.url());

        return """
                <div style="background:#212328;border-radius:10px;padding:22px;margin:0 0 18px 0;">
                  %s
                  <h3 style="margin:0 0 10px 0;color:#FFFFFF;font-size:18px;line-height:1.4;">%s</h3>
                  <p style="margin:0 0 14px 0;color:#CCDADC;font-size:16px;line-height:1.6;">%s</p>
                  <p style="margin:0 0 16px 0;color:#9CA3AF;font-size:14px;">Source: %s</p>
                  <a href="%s" target="_blank" rel="noopener noreferrer" style="color:#FDD458;text-decoration:none;font-weight:600;">Read Full Story -></a>
                </div>
                """.formatted(related, headline, summary, source, url);
    }

    private String escapeOrDefault(String value, String fallback) {
        return HtmlUtils.htmlEscape(value == null || value.isBlank() ? fallback : value);
    }
}
