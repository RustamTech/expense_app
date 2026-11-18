package com.example.notes.application.coolproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Entry point of the Expense Tracker API. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CoolprojectApplication {

  /**
   * Starts the application.
   *
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(CoolprojectApplication.class, args);
  }
}
