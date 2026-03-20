package com.example.daj2ee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Daj2eeApplication {

  public static void main(String[] args) {
    SpringApplication.run(Daj2eeApplication.class, args);
  }
}
