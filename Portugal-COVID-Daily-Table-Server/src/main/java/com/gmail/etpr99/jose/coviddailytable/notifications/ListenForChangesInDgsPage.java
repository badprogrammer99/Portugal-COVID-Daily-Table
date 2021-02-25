package com.gmail.etpr99.jose.coviddailytable.notifications;

import com.gmail.etpr99.jose.coviddailytable.callbacks.TodayReportAvailableCallback;
import com.google.common.base.Supplier;
import com.pusher.rest.Pusher;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class ListenForChangesInDgsPage implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenForChangesInDgsPage.class);

    private static final String PUSHER_CHANNEL_NAME = "energized-atoll-189";
    private static final String PUSHER_EVENT_NAME = "covid-data-available";

    private final TodayReportAvailableCallback todayReportAvailableCallback;
    private final Document dgsPage;
    private final Pusher pusher;

    public ListenForChangesInDgsPage(TodayReportAvailableCallback todayReportAvailableCallback,
                                     Supplier<Document> dgsPage, Pusher pusher) {
        this.todayReportAvailableCallback = todayReportAvailableCallback;
        this.dgsPage = dgsPage.get();
        this.pusher = pusher;
    }

    @Override
    public void run() {
        Element mostRecentReportAnchor = dgsPage.getElementsByClass("single_content").select("a[href]").get(0);
        String formattedTodayDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

        if (mostRecentReportAnchor.html().contains(formattedTodayDate)) {
            LOGGER.info("Today's report is available for download! Propagating the message throughout all connected clients.");
            pusher.trigger(PUSHER_CHANNEL_NAME, PUSHER_EVENT_NAME, Collections.singletonMap("message", "Today's COVID report data is available!"));
            todayReportAvailableCallback.onTodayReportAvailable();
        }
    }
}
