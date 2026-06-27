package com.yolo.springweb1;

import org.apache.catalina.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class Springweb1Application {

	public static void main(String[] args) {
		SpringApplication.run(Springweb1Application.class, args);
	}

}
