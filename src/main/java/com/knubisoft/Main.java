package com.knubisoft;

import lombok.SneakyThrows;

public class Main {
    @SneakyThrows
    public static void main(String[] args) {
        new LinkCheckerThread("https://karazin.ua/").start();
//        new T().run();
    }

    static class T extends Thread{
        @Override
        public void run() {
            System.out.println(this);
            new T().run();
            while(true);
        }
    }
}

