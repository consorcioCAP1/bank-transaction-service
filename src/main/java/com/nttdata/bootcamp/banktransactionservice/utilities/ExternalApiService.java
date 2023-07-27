package com.nttdata.bootcamp.banktransactionservice.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;
import com.nttdata.bootcamp.banktransactionservice.dto.CreditdebtDto;
import com.nttdata.bootcamp.banktransactionservice.dto.CustomerBankAccountDto;
import com.nttdata.bootcamp.banktransactionservice.service.impl.KakfaService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ExternalApiService {
    @Value("${customer-bank-account.api.url}")
    private String customerBankUrl;

    @Value("${customer-bank-account.url.getAccountBalance}")
    private String customerBankUrlGetAccountBalance;

    @Value("${credit-account.api.url}")
    private String creditServiceUrl;

    @Value("${credit-account.url.getAccountBalance}")
    private String creditServiceUrlGetAccountBalance;

	@Value("${create-credit-debts.api.url}")
    private String createCreditDebtsApiUrl;
	
	@Autowired
	KakfaService kafkaService;
	
	//metodo para obtener el saldo disponible de tarjetas credito
	public Mono<Double> getCreditCardBalanceByAccountNumber(String accountNumber) {
		WebClient webClient = WebClient.create(creditServiceUrl); 
		return webClient.get()
		            .uri(creditServiceUrlGetAccountBalance, accountNumber)
		            .retrieve()
		            .bodyToMono(Double.class);
	}

	//metodo para obtener document customerBankAccount
	public Mono<CustomerBankAccountDto> getCustomerBankAccountByAccountNumber(String accountNumber) {
		WebClient webClient = WebClient.create(customerBankUrl); 
		 return webClient.get()
	                .uri("/getCustomerBankAccountByAccountNumber/{accountNumber}", accountNumber)
	                .retrieve()
	                .bodyToMono(CustomerBankAccountDto.class);
	}

	//metodo para obtener el saldo disponible de cuentas bancarias
	public Mono<Double> getAccountBalanceByAccountNumber(String accountNumber) {
		WebClient webClient = WebClient.create(customerBankUrl); 
		return webClient.get()
		            .uri(customerBankUrlGetAccountBalance, accountNumber)
		            .retrieve()
		            .bodyToMono(Double.class);
	}

	//metodo para actualizar el saldo en Credit account
	public Mono<Void> updateCreditAccountBalance(String bankAccountNumber,Double accountBalance) {
    	log.info("nuevo monto en cuenta: "+bankAccountNumber + " es: "+ accountBalance);
		WebClient webClient = WebClient.create(creditServiceUrl); 
		return webClient.put()
                .uri("/updateAccountBalance/{bankAccountNumber}?accountBalance={accountBalance}",
                		bankAccountNumber, accountBalance)
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
	//metodo para actualizar el saldo
	public Mono<Void> updateAccountBalance(String bankAccountNumber,Double accountBalance) {
    	log.info("nuevo monto en cuenta: "+bankAccountNumber + " es: "+ accountBalance);
		WebClient webClient = WebClient.create(customerBankUrl); 
		return webClient.put()
                .uri("/updateAccountBalance/{bankAccountNumber}?accountBalance={accountBalance}",
                		bankAccountNumber, accountBalance)
                .retrieve()
                .toBodilessEntity()
                .flatMap(response -> {
                    if (response.getStatusCode() == HttpStatus.OK) {
	                	log.info("se realizo la actualizacion del monto");
	                	kafkaService.updateAccountBalanceWallet(bankAccountNumber, accountBalance);
                    	return Mono.empty(); // No se espera una respuesta específica
                    } else {
                        return Mono.error(new RuntimeException("Fallo actualizacion del Saldo del cliente."));
                    }
                });
	}

	//metodo para el consumo de la api de creacion de deudas de credito
	public void createDebtsAccount(BankTransactionDto bankTransactionDto) throws JsonProcessingException {
		CreditdebtDto creditDebts = CreditdebtDto.builder()
				.bankAccountNumber(bankTransactionDto.getBankAccountNumber())
				.numberBankPaymentInstallments(bankTransactionDto.getPaymentBankFee())
				.paymentAmount(bankTransactionDto.getAmount())
				.paymentStartDate(bankTransactionDto.getPaymentStartDate())
				.build();
		String objectToJson;
		objectToJson = ConvertJson.toJson(creditDebts);
		WebClient webClient = WebClient.create();
		webClient.post()
	        .uri(createCreditDebtsApiUrl)
	        .contentType(MediaType.APPLICATION_JSON)
	        .body(BodyInserters.fromValue(objectToJson))
	        .retrieve()
	        .bodyToMono(String.class)
	        .subscribe();				
	}
}
