package com.signalist.backend.mail;

import java.util.ArrayList;

import com.signalist.backend.config.AppProperties;
import com.signalist.backend.finnhub.FinnhubService;
import com.signalist.backend.user.UserDocument;
import com.signalist.backend.user.UserService;
import com.signalist.backend.watchlist.WatchlistService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyDigestScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DailyDigestScheduler.class);

    private final AppProperties appProperties;
    private final UserService userService;
    private final WatchlistService watchlistService;
    private final FinnhubService finnhubService;
    private final MailService mailService;

    public DailyDigestScheduler(
            AppProperties appProperties,
            UserService userService,
            WatchlistService watchlistService,
            FinnhubService finnhubService,
            MailService mailService
    ) {
        this.appProperties = appProperties;
        this.userService = userService;
        this.watchlistService = watchlistService;
        this.finnhubService = finnhubService;
        this.mailService = mailService;
    }

    @Scheduled(cron = "${app.jobs.daily-digest-cron:0 0 12 * * *}", zone = "UTC")
    public void sendDailyDigest() {
        if (!appProperties.getJobs().isDailyDigestEnabled()) {
            return;
        }

        logger.info("Starting scheduled daily digest run");
        for (UserDocument user : userService.getAllUsersWithEmail()) {
            try {
                var symbols = new ArrayList<>(watchlistService.getSymbolsForUser(user.getId()));
                var articles = finnhubService.getNews(symbols);
                mailService.sendDailySummaryEmail(user, articles);
            } catch (Exception exception) {
                logger.warn("Failed to send daily digest for {}", user.getEmail(), exception);
            }
        }
    }
}
