package com.kiit.campus_auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Enable scheduled tasks for auto-closing auctions
public class CampusAuctionPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusAuctionPlatformApplication.class, args);
    }
}
