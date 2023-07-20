package com.nttdata.bootcamp.banktransactionservice.service;

import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankTransactionService {

	public Mono<BankTransaction> saveBankTransactionWithDrawal(BankTransactionDto transactionDto);
	public Mono<BankTransaction> saveBankTransactionDeposit(BankTransactionDto transactionDto);
	public Flux<BankTransaction> getBankTransactionByAccountNumber(String accountNumber);
	public Mono<BankTransaction> saveBankTransaction(BankTransactionDto transactionDto);
	public Mono<BankTransaction> saveBankTransactionCardUsage(BankTransactionDto transactionDto);
}

