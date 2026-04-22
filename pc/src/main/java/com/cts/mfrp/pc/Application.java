package com.cts.mfrp.pc;

import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@ComponentScan(basePackages = {"com.cts.mfrp.pc"})
@EnableScheduling
public class Application {

	public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        System.out.println("Something");
	}

}
