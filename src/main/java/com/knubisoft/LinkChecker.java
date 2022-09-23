package com.knubisoft;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@NoArgsConstructor
@Setter
public class LinkChecker {
    private static final Set<String> alreadyCheckedLinks = Collections.synchronizedSet(new HashSet<>());
    private static final Logger log = LogManager.getLogger(LinkChecker.class);
    private static final int STATUS_OK = 200;
    private String siteLink;
    private ForkJoinPool pool;

    public Map<String, Integer> collectLinks(String inputLink, String siteLink) {
        this.siteLink = siteLink;
        pool = ForkJoinPool.commonPool();
        return pool.invoke(new CheckLinkTask(inputLink));
    }

    @AllArgsConstructor
    private class CheckLinkTask extends RecursiveTask<Map<String, Integer>> {
        private final String link;

        @Override
        @SneakyThrows
        protected Map<String, Integer> compute() {
            Map<String, Integer> urlCode = new LinkedHashMap<>();
            List<CheckLinkTask> taskList = new ArrayList<>();
            if (getStatusCode(link) == STATUS_OK) {
                try {
                    for (Element linkTag : Jsoup.connect(link).get().select("a[href]")) {
                        String childLink = linkTag.absUrl("href");
                        checkLink(urlCode, taskList, childLink);
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            }
            taskList.forEach(task -> urlCode.putAll(task.join()));
            return urlCode;
        }
    }

    private void checkLink(Map<String, Integer> urlCode, List<CheckLinkTask> taskList, String childLink) {
        if (!alreadyCheckedLinks.contains(childLink)) {
            alreadyCheckedLinks.add(childLink);
            urlCode.put(childLink, getStatusCode(childLink));
            checkChildren(childLink, taskList);
        }
    }

    private void checkChildren(String childLink, List<CheckLinkTask> taskList) {
        if (childLink.matches("^(" + siteLink + ").+")) {
            CheckLinkTask task = new CheckLinkTask(childLink);
            task.fork();
            taskList.add(task);
        }
    }

    private int getStatusCode(String link) {
        try {
            return Jsoup.connect(link).execute().statusCode();
        } catch (Exception e) {
            if (e instanceof HttpStatusException) {
                return ((HttpStatusException) e).getStatusCode();
            } else {
                log.error(link + " - " + e);
            }
        }
        return -1;
    }
}