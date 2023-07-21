package com.nttdata.bootcamp.banktransactionservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransferDto;
import com.nttdata.bootcamp.banktransactionservice.service.BankTransactionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class BankTransactionController {

	@Autowired
	private BankTransactionService bankTransactionService;
	
	//metodo para realizar un retiro en cuenta
	@PostMapping("/createBankTransactionWithDrawal")
    public Mono<ResponseEntity<Object>>createBankTransactionWithDrawal
    							(@RequestBody BankTransactionDto bankTransactionDto) {
		
		return bankTransactionService.saveBankTransactionWithDrawal(bankTransactionDto)
				.flatMap(objResponse -> {
			        ResponseEntity<Object> responseEntity = ResponseEntity.ok(objResponse);
			        return Mono.just(responseEntity);
		    })
		    .onErrorResume(error -> {
		        ResponseEntity<Object> responseEntity = ResponseEntity
		        		.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
		        return Mono.just(responseEntity);
		    });
		
	}
	//metodo para realizar un deposito en cuenta
	@PostMapping("/createBankTransactionDeposit")
    public Mono<ResponseEntity<Object>>createBankTransactionDeposit(@RequestBody BankTransactionDto bankTransactionDto){
		
		return bankTransactionService.saveBankTransactionDeposit(bankTransactionDto)
				.flatMap(objResponse -> {
			        ResponseEntity<Object> responseEntity = ResponseEntity.ok(objResponse);
			        return Mono.just(responseEntity);
		    })
		    .onErrorResume(error -> {
		        ResponseEntity<Object> responseEntity = ResponseEntity
		        		.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
		        return Mono.just(responseEntity);
		    });
	
	}
	//metodo para consultar movimientos bancarios
	@GetMapping("/getBankTransactionByAccountNumber/{bankAccountNumber}")
    public Flux<BankTransaction> getAccountBalance(@PathVariable String bankAccountNumber) {
        return bankTransactionService.getBankTransactionByAccountNumber(bankAccountNumber);
    }

	//metodo para realizar un registro de Trasaccion
	@PostMapping("/createBankTransaction")
    public Mono<BankTransaction>createBankTransaction(@RequestBody BankTransactionDto bankTransactionDto) {
		return bankTransactionService.saveBankTransaction(bankTransactionDto);
	}

	//metodo para realizar el uso de tarjeta de credito
	@PostMapping("/createBankTransactionCreditCardUsage")
    public Mono<ResponseEntity<Object>>createBankTransactionCardUsage
    							(@RequestBody BankTransactionDto bankTransactionDto) {
		return bankTransactionService.saveBankTransactionCardUsage(bankTransactionDto)
				.flatMap(objResponse -> {
			        ResponseEntity<Object> responseEntity = ResponseEntity.ok(objResponse);
			        return Mono.just(responseEntity);
		    })
		    .onErrorResume(error -> {
		        ResponseEntity<Object> responseEntity = ResponseEntity
		        		.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
		        return Mono.just(responseEntity);
		    });		
	}
	//metodo para realizar un deposito en cuenta
	@PostMapping("/sendBankTransfer")
    public Mono<ResponseEntity<Object>>createBankTransactionDeposit(@RequestBody BankTransferDto transferDto){
		
		return bankTransactionService.sendBankTransfer(transferDto)
				.flatMap(objResponse -> {
			        ResponseEntity<Object> responseEntity = ResponseEntity.ok(objResponse);
			        return Mono.just(responseEntity);
		    })
		    .onErrorResume(error -> {
		        ResponseEntity<Object> responseEntity = ResponseEntity
		        		.status(HttpStatus.BAD_REQUEST).body(error.getMessage());
		        return Mono.just(responseEntity);
		    });	
	}
	//metodo para listar las transacciones en base al typo de cuenta registrado y a un mes determinado
    @GetMapping("/{typeAccount}/{month}")
    public Flux<BankTransaction> getBankTransactionsByTypeAccountAndMonth
    							(@PathVariable String typeAccount, @PathVariable String month) {
        return bankTransactionService.getBankTransactionsByTypeAccountAndMonth(typeAccount, month);
    }
}
