package com.nttdata.bootcamp.banktransactionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankTransactionService {

	public Mono<BankTransaction> saveBankTransactionWithDrawal(BankTransactionDto transactionDto) throws JsonProcessingException;
	public Mono<BankTransaction> saveBankTransactionDeposit(BankTransactionDto transactionDto) throws JsonProcessingException;
	public Flux<BankTransaction> getBankTransactionByAccountNumber(String accountNumber);
	public Mono<BankTransaction> saveBankTransaction(BankTransactionDto transactionDto);
}

