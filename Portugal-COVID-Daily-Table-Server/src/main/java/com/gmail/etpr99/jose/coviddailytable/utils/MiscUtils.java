package com.gmail.etpr99.jose.coviddailytable.utils;

import org.apache.pdfbox.text.PDFTextStripperByArea;

public class MiscUtils {
    public static int getStatForPdfRegion(PDFTextStripperByArea stripper, String regionName) {
        return Integer.parseInt(stripper.getTextForRegion(regionName)
                .replace(" ", "")
                .replace("\n", "")
                .replace("\r", "")
        );
    }

    public static void throwCheckedException(Exception e) {
        doThrow0(e);
    }

    @SuppressWarnings("unchecked")
    private static <E extends Exception> void doThrow0(Exception e) throws E {
        throw (E) e;
    }
}
