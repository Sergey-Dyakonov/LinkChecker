package com.knubisoft;

import lombok.SneakyThrows;

import java.util.Map;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        LinkChecker linkChecker = new LinkChecker();
        Map<String, Integer> map = linkChecker.collectLinks("https://freemaxpictures.com/", "https://freemaxpictures.com/");
        map.forEach((s, i) -> System.out.println(s + ": " + i));
    }
}

