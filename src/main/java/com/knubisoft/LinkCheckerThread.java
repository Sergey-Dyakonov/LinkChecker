package com.knubisoft;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Setter
public class LinkCheckerThread extends Thread {
    interface COLOR_KEYS {
        String ANSI_CYAN = "\u001B[36m";
        String ANSI_RESET = "\u001B[0m";
        String ANSI_RED = "\u001B[31m";
        String ANSI_GREEN = "\u001B[32m";
        String ANSI_YELLOW = "\u001B[33m";
        String ANSI_BLUE = "\u001B[34m";
        String ANSI_PURPLE = "\u001B[35m";
        String ANSI_WHITE = "\u001B[37m";
    }

    private static final int STATUS_OK = 200;
    private static final Set<String> alreadyCheckedLinks = ConcurrentHashMap.newKeySet();
    private final String inputLink;
    private final String siteLink;
    private static final Logger log = LogManager.getLogger(LinkCheckerThread.class);

    @Override
    @SneakyThrows
    public void run() {
        Document doc = Jsoup.connect(inputLink).get();
        for (Element linkTag : doc.select("a[href]")) {
            String link = linkTag.absUrl("href");
            synchronized (alreadyCheckedLinks) {
                if (!alreadyCheckedLinks.contains(link)) {
                    checkLink(link);
                }
            }
        }
    }

    private void checkLink(String link) throws IOException {
        if (!isCommonLink(link)) {
            return;
        }
        alreadyCheckedLinks.add(link);
        int statusCode = getStatusCode(link);
        if (statusCode != STATUS_OK) {
            log.error(COLOR_KEYS.ANSI_RED + link + ": status code = [" + statusCode + "]" + COLOR_KEYS.ANSI_RESET);
        } else if (siteLink != null && !siteLink.isBlank() && !link.startsWith(siteLink)) {
            log.warn(COLOR_KEYS.ANSI_YELLOW + "Link " + link + " reaches to another site! To reach it specify it as root link" + COLOR_KEYS.ANSI_RESET);
        } else {
            log.info(COLOR_KEYS.ANSI_GREEN + link + ": status code = [" + statusCode + "]" + COLOR_KEYS.ANSI_RESET);
            checkChildren(link);
        }
    }

    private boolean isCommonLink(String link) {
        return link.matches("^(https|http).+");
    }

    private int getStatusCode(String link) throws IOException {
        return Jsoup.connect(link).get().connection().response().statusCode();
    }

    private void checkChildren(String link) {
        new LinkCheckerThread(link, siteLink).start();
    }
}