package com.gmail.etpr99.jose.coviddailytable;

import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSource;
import com.gmail.etpr99.jose.coviddailytable.tablebuilders.CovidDataTableBuilder;
import com.gmail.etpr99.jose.coviddailytable.tablebuilders.HTMLCovidDataTableBuilder;
import com.gmail.etpr99.jose.coviddailytable.tablebuilders.RedditCovidDataTableBuilder;
import com.gmail.etpr99.jose.coviddailytable.utils.DgsPageSupplier;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.pusher.rest.Pusher;
import de.invesdwin.instrument.DynamicInstrumentationLoader;
import org.jsoup.nodes.Document;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableLoadTimeWeaving;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableLoadTimeWeaving
public class CovidDailyTableApplication {
    private static final String PUSHER_APP_ID = "1161144";
    private static final String PUSHER_KEY = "98a6a8bb2fc2375f46b1";
    private static final String PUSHER_SECRET = "aebf1f859956ceb6515f";
    private static final String PUSHER_CLUSTER = "eu";

    public static void main(String[] args) {
        System.setProperty("user.timezone", "Europe/Lisbon");
        DynamicInstrumentationLoader.waitForInitialized();
        DynamicInstrumentationLoader.initLoadTimeWeavingContext();
        SpringApplication.run(CovidDailyTableApplication.class, args);
    }

    @Bean("covidDataTableBuilderSet")
    public Set<CovidDataTableBuilder> provideCovidDataTableBuilderSet(CovidDataSource covidDataSource) {
        return new HashSet<>() {{
           add(new HTMLCovidDataTableBuilder(covidDataSource));
           add(new RedditCovidDataTableBuilder(covidDataSource));
        }};
    }

    @Bean("dgsPage")
    public Supplier<Document> provideDgsPage() {
        return Suppliers.memoizeWithExpiration(new DgsPageSupplier(), 5, TimeUnit.MINUTES);
    }

    @Bean
    public Pusher providePusherInstance() {
        Pusher pusher = new Pusher(PUSHER_APP_ID, PUSHER_KEY, PUSHER_SECRET);
        pusher.setCluster(PUSHER_CLUSTER);
        return pusher;
    }
}
