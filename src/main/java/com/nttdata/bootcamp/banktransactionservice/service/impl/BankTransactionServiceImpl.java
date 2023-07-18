package com.nttdata.bootcamp.banktransactionservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;
import com.nttdata.bootcamp.banktransactionservice.repository.BankTransactionRepository;
import com.nttdata.bootcamp.banktransactionservice.service.BankTransactionService;
import com.nttdata.bootcamp.banktransactionservice.utilities.BankTransactionBuilder;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class BankTransactionServiceImpl implements BankTransactionService{

	@Autowired
	BankTransactionRepository repository;

	public static final String TRANSACTION_TYPE_BANK_DEPOSIT = "DEPOSIT";
	public static final String TRANSACTION_TYPE_BANK_WITHDRAWAL = "WITHDRAWAL";
	public static final String TRANSACTION_TYPE_BANK_CREDIT_PAYMENT = "CREDIT_PAYMENT";
	public static final String TRANSACTION_TYPE_BANK_CARD_USAGE = "CARD_USAGE";

	@Value("${customer-bank-account.api.url}")
    private String customerBankUrl;
	
	@Value("${customer-bank-account.url.getAccountBalance}")
    private String customerBankUrlGetAccountBalance;

	@Override
	public Mono<BankTransaction> saveBankTransactionWithDrawal(BankTransactionDto transactionDto){
		//validando tipo de transaccion retiro
		return getAccountBalanceByAccountNumber(transactionDto.getBankAccountNumber())
                .flatMap(accountBalance -> {
                    if (accountBalance < transactionDto.getAmount()) {
                        return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    } else {
                    	Double newAmount= accountBalance - transactionDto.getAmount();
	                	updateAccountBalance(transactionDto.getBankAccountNumber(), newAmount).subscribe();
                        BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
                        return repository.save(bankTransaction);
                    }
                });	
	}

	//Metodo para realizar pago de productos de credito
	public Mono<BankTransaction> saveBankTransactionCreditPayment(BankTransactionDto transactionDto){
		//validando tipo de transaccion retiro
		return getAccountBalanceByAccountNumber(transactionDto.getBankAccountNumber())
                .flatMap(accountBalance -> {
                    if (accountBalance < transactionDto.getAmount()) {
                        return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    } else {
                    	Double newAmount= accountBalance - transactionDto.getAmount();
	                	updateAccountBalance(transactionDto.getBankAccountNumber(), newAmount).subscribe();
                        BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
                        return repository.save(bankTransaction);
                    }
                });	
	}

	@Override
	public Mono<BankTransaction> saveBankTransactionDeposit(BankTransactionDto transactionDto){
		//obtenemos el saldo actual para modificarlo
		return getAccountBalanceByAccountNumber(transactionDto.getBankAccountNumber())
	            .onErrorResume(JsonProcessingException.class, ex -> {
	                log.error("Error al procesar la respuesta del clienteBank para obtener el saldo de la cuenta.", ex);
	                return Mono.error(new RuntimeException("Error al obtener el saldo de la cuenta."));
	            })
	            .flatMap(accountBalance -> {
	                Double newAmount = accountBalance + transactionDto.getAmount();
	                updateAccountBalance(transactionDto.getBankAccountNumber(), newAmount).subscribe();
	                BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
	                return repository.save(bankTransaction);
	            });
	}
	
	//metodo para registrarTransaccion
	@Override
	public Mono<BankTransaction> saveBankTransaction(BankTransactionDto transactionDto){
		BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
		return repository.save(bankTransaction);	
	}
	
	//obtener todos los movimientos bancarios de un producto por numero de cuenta
	@Override
	public Flux<BankTransaction> getBankTransactionByAccountNumber(String accountNumber){
		return repository.findByBankAccountNumber(accountNumber);     
	}
	//metodo para obtener el saldo disponible del cliente
	public Mono<Double> getAccountBalanceByAccountNumber(String accountNumber) {
		WebClient webClient = WebClient.create(customerBankUrl); 
		return webClient.get()
		            .uri(customerBankUrlGetAccountBalance, accountNumber)
		            .retrieve()
		            .bodyToMono(Double.class);
	}

	//metodo para actualizar el saldo
	public Mono<Void> updateAccountBalance(String bankAccountNumber,Double accountBalance) {
    	log.info("nuevo monto en cuenta: "+bankAccountNumber + " es: "+ accountBalance);
		WebClient webClient = WebClient.create(customerBankUrl); 
		return webClient.put()
                .uri("/updateAccountBalance/{bankAccountNumber}?accountBalance={accountBalance}", bankAccountNumber, accountBalance)
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
	                	log.info("se realizo la actualizacion del monto");
                    	return Mono.empty(); // No se espera una respuesta específica
                    } else {
                        return Mono.error(new RuntimeException("Fallo actualizacion del Saldo del cliente."));
                    }
                });
	}
}
