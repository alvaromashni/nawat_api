package br.com.smartmesquitaapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartMesquitaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartMesquitaApiApplication.class, args);
    }

}
