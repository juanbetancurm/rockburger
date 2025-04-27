package com.rockburger.burgermain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.rockburger.burgermain.adapters.driven.feign")
public class BurgerMainApplication {
	public static void main(String[] args) {
		SpringApplication.run(BurgerMainApplication.class, args);
	}

}
