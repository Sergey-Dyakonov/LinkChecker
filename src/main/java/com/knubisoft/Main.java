package com.knubisoft;

import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        new LinkCheckerThread("https://www.work.ua/", "").start();
    }
}

