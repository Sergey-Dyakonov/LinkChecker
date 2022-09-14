package com.knubisoft;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    private static final int STATUS_OK = 200;
    static Set<String> alreadyCheckedLinks = ConcurrentHashMap.newKeySet();

    @SneakyThrows
    public static void main(String[] args) {
        new LinkChecker("https://karazin.ua/").start();
    }

    static class LinkChecker extends Thread {
        private final String inputLink;
        private static Logger log = LogManager.getRootLogger();

        public LinkChecker(String inputLink) {
            this.inputLink = inputLink;
        }

        @Override
        @SneakyThrows
        public void run() {
            Connection con = Jsoup.connect(inputLink);
            Document doc = con.get();
            for (Element linkTag : doc.select("a[href]")) {
                String link = linkTag.attr("href");
                if (!isAbsolute(link)) {
                    link = inputLink + link.substring(1);
                }
                Connection connect = Jsoup.connect(link);
                Document document = connect.get();
                Connection connection = document.connection();
                int statusCode = connection.response().statusCode();
                if (statusCode != STATUS_OK) {
//                        log.warn(link + ": status code = [" + statusCode + "]");
                    System.out.println(link + ": status code = [" + statusCode + "]");
                } else if (!alreadyCheckedLinks.contains(link)) {
                    alreadyCheckedLinks.add(link);
//                        log.info();
                    System.out.println(link + ": status code = [" + statusCode + "]");
                    new LinkChecker(link).start();
                }
                /*} else {
                    System.out.println(link + ": is malformed. Should be an absolute URL, and starts with 'http://' or 'https://'");
//                    log.warn(link + ": is malformed. Should be an absolute URL, and starts with 'http://' or 'https://'");
                }*/
            }
        }

        private boolean isAbsolute(String link) {
            return link.matches("^(http|https).+") && !link.contains(inputLink);
        }
    }
}

