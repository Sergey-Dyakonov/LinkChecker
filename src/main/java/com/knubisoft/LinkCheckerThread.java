package com.knubisoft;

import lombok.AllArgsConstructor;
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
public class LinkCheckerThread extends Thread {
    private static final int STATUS_OK = 200;
    private static final Set<String> alreadyCheckedLinks = ConcurrentHashMap.newKeySet();
    private final String inputLink;
    private static Logger log = LogManager.getRootLogger();

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
        int statusCode = getStatusCode(link);
        if (statusCode != STATUS_OK) {
//                        log.warn(link + ": status code = [" + statusCode + "]");
            System.out.println(link + ": status code = [" + statusCode + "]");
        } else {
            alreadyCheckedLinks.add(link);
//                        log.info();
            System.out.println(this + " " + link + ": status code = [" + statusCode + "]");
            checkChildren(link);
        }
    }

    private int getStatusCode(String link) throws IOException {
        return Jsoup.connect(link).get().connection().response().statusCode();
    }

    private void checkChildren(String link) {
        new LinkCheckerThread(link).start();
    }
}