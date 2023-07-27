package com.nttdata.bootcamp.banktransactionservice.service.impl;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nttdata.bootcamp.banktransactionservice.repository.BankTransactionRepository;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;


@Service
public class KakfaService {

	@Autowired
	BankTransactionRepository repository;
	
	private final KafkaSender<String, String> kafkaSender;
    private final String topicUpdateBalanceWallet = "updateBalanceWallet";

    KakfaService(KafkaSender<String, String> kafkaSender){
    	this.kafkaSender = kafkaSender;
    }
	
	public void updateAccountBalanceWallet(String bankAccountNumber,Double accountBalance) {
		String message = "{\"bankAccountNumber\": \"" + bankAccountNumber 
            	+ "\", \"accountBalance\": \"" + accountBalance + "\"}";
            
        kafkaSender.send(Mono.just(SenderRecord.create(
        	new ProducerRecord<>(topicUpdateBalanceWallet, message), null))).subscribe();
	}
}
