package com.gmail.etpr99.jose.coviddailytable.utils;

import com.gmail.etpr99.jose.coviddailytable.annotations.Retryable;
import com.google.common.base.Supplier;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.gmail.etpr99.jose.coviddailytable.utils.MiscUtils.throwCheckedException;

@Component
public class DgsPageSupplier implements Supplier<Document> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DgsPageSupplier.class);

    private static final String COVID_SITUATION_REPORT_WEBSITE = "https://covid19.min-saude.pt/relatorio-de-situacao/";

    @Override
    @Retryable
    public Document get() {
        Document doc = null;

        LOGGER.info("Fetching a new instance of the DGS page...");

        try {
            doc = Jsoup.connect(COVID_SITUATION_REPORT_WEBSITE).get();
        } catch (IOException e) {
            throwCheckedException(e);
        }

        return doc;
    }
}
