package com.gmail.etpr99.jose.coviddailytable.notifications;

import com.gmail.etpr99.jose.coviddailytable.callbacks.TodayReportAvailableCallback;
import com.google.common.base.Supplier;
import com.pusher.rest.Pusher;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.areDaysEqual;

@Component
public class DgsTwitterStatusListener implements StatusListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DgsTwitterStatusListener.class);
    private static final String REPORT_AVAILABLE_TEXT = "Já se encontra disponível o Relatório de Situação de hoje";

    private static final String API_KEY = "a83yLd2b4bFEjpXzX5TTT9Yq9";
    private static final String API_KEY_SECRET = "ZPsvnkrakS8wMi7pWgZX5JvbTRDthAoNqIwBzQm5Rog1FSOrxs";
    private static final String ACCESS_TOKEN = "4171852684-NrHZIMQeaZehFnHFJyITMXQK2Xh7xo6T4LK1Bnl";
    private static final String ACCESS_TOKEN_SECRET = "T4Z0l3UvkJoFCbUx6q6VkVerz3eEPmdi6N6uGT2WbCG0O";
    private static final int DG_SAUDE_USER_ID = 599399505;

    private final Supplier<Document> dgsPageSupplier;
    private final Pusher pusher;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final TodayReportAvailableCallback todayReportAvailableCallback = () -> {
        LOGGER.info("Shutting down the scheduled executor service...");
        new Thread(executor::shutdownNow).start();
    };

    @Autowired
    public DgsTwitterStatusListener(@Qualifier("dgsPage") Supplier<Document> dgsPageSupplier, Pusher pusher) {
        this.dgsPageSupplier = dgsPageSupplier;
        this.pusher = pusher;
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(API_KEY)
            .setOAuthConsumerSecret(API_KEY_SECRET)
            .setOAuthAccessToken(ACCESS_TOKEN)
            .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(this);

        FilterQuery query = new FilterQuery();
        query.follow(DG_SAUDE_USER_ID);
        twitterStream.filter(query);
    }

    @Override
    public void onStatus(Status status) {
        LOGGER.info("Received status update with text: " + status.getText());
        boolean doesStatusContainReportAvailableText = status.getText().toLowerCase().contains(REPORT_AVAILABLE_TEXT.toLowerCase());
        boolean isStatusFromToday = areDaysEqual(status.getCreatedAt(), new Date());
        if (!status.isRetweet() && !status.isRetweeted() && doesStatusContainReportAvailableText && isStatusFromToday) {
            LOGGER.info("Today's report is out according to DGS Twitter! Aggressively checking for changes in the DGS homepage structure.");
            try {
                executor.scheduleAtFixedRate(() -> new ListenForChangesInDgsPage(todayReportAvailableCallback, dgsPageSupplier, pusher).run(),
                    extractExpirationTimeFromSupplier(), TimeUnit.MINUTES.toNanos(5), TimeUnit.NANOSECONDS).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) { }

    @Override
    public void onTrackLimitationNotice(int i) { }

    @Override
    public void onScrubGeo(long l, long l1) { }

    @Override
    public void onStallWarning(StallWarning stallWarning) { }

    @Override
    public void onException(Exception e) {
        LOGGER.warn("Exception occurred while listening for Twitter statuses", e);
    }

    private long extractExpirationTimeFromSupplier() {
        try {
            Field expirationNanosField = dgsPageSupplier.getClass().getDeclaredField("expirationNanos");
            expirationNanosField.setAccessible(true);
            long diffBetweenNowAndSupplierExpirationTime = expirationNanosField.getLong(dgsPageSupplier) - System.nanoTime();
            return Math.max(diffBetweenNowAndSupplierExpirationTime, 0L);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
