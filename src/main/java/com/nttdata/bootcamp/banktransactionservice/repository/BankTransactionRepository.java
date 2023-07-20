package com.nttdata.bootcamp.banktransactionservice.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import reactor.core.publisher.Flux;

public interface BankTransactionRepository extends ReactiveMongoRepository<BankTransaction, String>{
	Flux<BankTransaction> findByBankAccountNumber(String bankAccountNumber);

	@Query("{'bankAccountNumber': ?0,  'date': {$regex: ?1}}")
	Flux<BankTransaction> findTransactionsByBankAccountNumberAndMonth(String bankAccountNumber,
			String currentMonthRegex);
}
