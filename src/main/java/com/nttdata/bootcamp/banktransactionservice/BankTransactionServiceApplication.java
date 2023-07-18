package com.nttdata.bootcamp.banktransactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories
public class BankTransactionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankTransactionServiceApplication.class, args);
	}

}
