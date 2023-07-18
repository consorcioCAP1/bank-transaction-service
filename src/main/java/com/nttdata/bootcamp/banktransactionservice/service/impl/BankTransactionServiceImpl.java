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

	public final String TRANSACTION_TYPE_BANK_DEPOSIT = "DEPOSIT";
	public final String TRANSACTION_TYPE_BANK_WITHDRAWAL = "WITHDRAWAL";
	public final String TRANSACTION_TYPE_BANK_CREDIT_PAYMENT = "CREDIT_PAYMENT";
	public final String TRANSACTION_TYPE_BANK_CARD_USAGE = "CARD_USAGE";

	@Value("${customer-bank-account.api.url}")
    private String customerBankUrl;
	
	@Value("${customer-bank-account.url.getAccountBalance}")
    private String customerBankUrlGetAccountBalance;

	@Override
	public Mono<BankTransaction> saveBankTransactionWithDrawal(BankTransactionDto transactionDto) throws JsonProcessingException{
		//validando tipo de transaccion retiro
		return getAccountBalanceByAccountNumber(transactionDto.getBankAccountNumber())
                .flatMap(accountBalance -> {
                	log.info("cliente con N de cuenta: "+transactionDto.getBankAccountNumber() +" tiene el siguiente saldo:"+ accountBalance);
                    if (accountBalance < transactionDto.getAmount()) {
                        return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    } else {
                    	Double newAmount= accountBalance - transactionDto.getAmount();
	                	log.info("nuevo monto en cuenta: "+newAmount);
	                	updateAccountBalance(transactionDto.getBankAccountNumber(), newAmount).subscribe();
                        BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
                        return repository.save(bankTransaction);
                    }
                });	
	}

	//Metodo para realizar pago de productos de credito
	public Mono<BankTransaction> saveBankTransactionCreditPayment(BankTransactionDto transactionDto) throws JsonProcessingException{
		//validando tipo de transaccion retiro
		return getAccountBalanceByAccountNumber(transactionDto.getBankAccountNumber())
                .flatMap(accountBalance -> {
                	log.info("cliente con N de cuenta: "+transactionDto.getBankAccountNumber() +" tiene el siguiente saldo:"+ accountBalance);
                    if (accountBalance < transactionDto.getAmount()) {
                        return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    } else {
                    	Double newAmount= accountBalance - transactionDto.getAmount();
	                	log.info("nuevo monto en cuenta: "+newAmount);
	                	updateAccountBalance(transactionDto.getBankAccountNumber(), newAmount).subscribe();
                        BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
                        return repository.save(bankTransaction);
                    }
                });	
	}

	@Override
	public Mono<BankTransaction> saveBankTransactionDeposit(BankTransactionDto transactionDto)
			throws JsonProcessingException {
		//obtenemos el saldo actual para modificarlo
		return getAccountBalanceByAccountNumber(transactionDto.getBankAccountNumber())
                .flatMap(accountBalance -> {
                	log.info("cliente con N de cuenta: "+transactionDto.getBankAccountNumber() +" tiene el siguiente saldo:"+ accountBalance);                    
                	Double newAmount= accountBalance + transactionDto.getAmount();
                	log.info("nuevo monto en cuenta: "+newAmount);
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
	public Mono<Double> getAccountBalanceByAccountNumber(String accountNumber) throws JsonProcessingException{
		WebClient webClient = WebClient.create(customerBankUrl); 
		return webClient.get()
		            .uri(customerBankUrlGetAccountBalance, accountNumber)
		            .retrieve()
		            .bodyToMono(Double.class);      
	}

	//metodo para actualizar el saldo
	public Mono<Void> updateAccountBalance(String bankAccountNumber,Double accountBalance) {
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
