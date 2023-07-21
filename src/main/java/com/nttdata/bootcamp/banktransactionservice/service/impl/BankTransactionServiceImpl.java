package com.nttdata.bootcamp.banktransactionservice.service.impl;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;
import com.nttdata.bootcamp.banktransactionservice.dto.CustomerBankAccountDto;
import com.nttdata.bootcamp.banktransactionservice.repository.BankTransactionRepository;
import com.nttdata.bootcamp.banktransactionservice.service.BankTransactionService;
import com.nttdata.bootcamp.banktransactionservice.utilities.BankTransactionBuilder;
import com.nttdata.bootcamp.banktransactionservice.utilities.ExternalApiService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BankTransactionServiceImpl implements BankTransactionService{

	@Autowired
	BankTransactionRepository repository;

	public static final String TRANSACTION_TYPE_BANK_DEPOSIT = "DEPOSIT";
	public static final String TRANSACTION_TYPE_BANK_WITHDRAWAL = "WITHDRAWAL";
	public static final String TRANSACTION_TYPE_BANK_CREDIT_PAYMENT = "CREDIT_PAYMENT";
	public static final String TRANSACTION_TYPE_BANK_CARD_USAGE = "CARD_USAGE";
	public static final String ACCOUNT_TYPE_SAVING = "SAVING";
	public static final String ACCOUNT_TYPE_CURRENT = "CURRENT";
	public static final String ACCOUNT_TYPE_FIXED_TERM = "FIXED";
	public static final int MAX_TRANSACTION = 20;
	public static final Double TRANSACTION_FEE = 50.00;

	@Autowired
	ExternalApiService externalApiService;
	
	//metodo para realizar retiro en base a saldo en cuenta
	@Override
	public Mono<BankTransaction> saveBankTransactionWithDrawal(BankTransactionDto transactionDto) {
	    return externalApiService.getCustomerBankAccountByAccountNumber(transactionDto.getBankAccountNumber())
	            .flatMap(bankAccount -> {
	                //cuando es cuenta de ahorro verificamos el limite maximo de movimientos mensuales
	            	if (bankAccount.getAccountType().equals(ACCOUNT_TYPE_SAVING)) {
	            		Mono<Long> drawalsCount =transactionCount(transactionDto.getBankAccountNumber()
	            				,ACCOUNT_TYPE_SAVING);
	                    return drawalsCount.flatMap(withdrawalsCount -> {
	                        if (withdrawalsCount >= bankAccount.getBankMovementLimit()) {
	                            return Mono.error(
	                            	new RuntimeException("Cliente alcanzó límite máximo de movimiento mensual"));
	                        } else {
	                            return processWithdrawal(bankAccount, transactionDto);
	                        }
	                    });
	                } else if (bankAccount.getAccountType().equals(ACCOUNT_TYPE_FIXED_TERM)) {
	            		//metodo para validar que se realice un retiro en un dia especifico
	                	Mono<Long> drawalsCount = transactionCount(transactionDto.getBankAccountNumber()
	                			,ACCOUNT_TYPE_FIXED_TERM);
	                	LocalDate currentDate = LocalDate.now();
	                    // Obtener el día del mes actual
	                    int dayOfMonth = currentDate.getDayOfMonth();
	                    Long drawalsCountLong = drawalsCount.block();
	                    if(drawalsCountLong>0) {
	                    	return Mono.error(
	                            	new RuntimeException("Se supero maximo de movimientos permitidos."));
	                    }
	                    if (dayOfMonth != bankAccount.getBankMovementDay())
	                    	return Mono.error(
	                            	new RuntimeException("día permitido para realizar movimiento es: "
	                            			+ bankAccount.getBankMovementDay()));
	                    else return processWithdrawal(bankAccount, transactionDto);
	            	} else {
	                    return processWithdrawal(bankAccount, transactionDto);
	                }
	            });
	}
	//metodo para validar la cantidad de transacciones
	public Mono<Long> transactionCount(String bankAccountNumber, String type) {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String currentMonthRegex = formatter.format(currentDate);

        Flux<BankTransaction> transactions = repository
        		.findTransactionsByBankAccountNumberAndMonth(bankAccountNumber, currentMonthRegex);
        //retornando la cantidad de retiros  
        if (type.equals(ACCOUNT_TYPE_SAVING)) {
            transactions = transactions.filter(transaction -> transaction.getType()
            		.equals(TRANSACTION_TYPE_BANK_WITHDRAWAL));
        } else if (type.equals(ACCOUNT_TYPE_FIXED_TERM)) {
        	//retorna la conteo de cantidad de retiros y depositos 
            transactions = transactions.filter(transaction -> transaction.getType()
            		.equals(TRANSACTION_TYPE_BANK_WITHDRAWAL)
            		|| transaction.getType().equals(TRANSACTION_TYPE_BANK_DEPOSIT));
        } else {
            return transactions.count();
        }
        //si la busqueda de transacciones no existe devolver un mono<Long> Cero
        return transactions.count().defaultIfEmpty(0L);
    }

	private Mono<BankTransaction>processWithdrawal(CustomerBankAccountDto bankAccount,
								BankTransactionDto transactionDto) {
	    
		
		 // Realizar la transacción y obtener el conteo de transacciones
	    return externalApiService.updateAccountBalance(transactionDto.getBankAccountNumber(),
	            bankAccount.getAccountBalance() - transactionDto.getAmount())
	            .then(repository
	            	.findDepositsAndWithdrawalsByBankAccountNumber(transactionDto.getBankAccountNumber())
                    .count()
                    .flatMap(countBankAccount -> {
                        Double newAmount;
                        if (countBankAccount > MAX_TRANSACTION) {
                        	//validar que saldo sea suficiente aplicando comision
                            if (bankAccount.getAccountBalance() < (transactionDto.getAmount()+ TRANSACTION_FEE)) {
                    			return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    		}
                        	newAmount = bankAccount.getAccountBalance() - countBankAccount - TRANSACTION_FEE;
                        } else {
                        	if (bankAccount.getAccountBalance() < transactionDto.getAmount()) {
                    			return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    	    
                    		}
                            newAmount = bankAccount.getAccountBalance();
                        }
                        externalApiService.updateAccountBalance(transactionDto.getBankAccountNumber(), newAmount)
                                .subscribe();

                        BankTransaction bankTransaction = BankTransactionBuilder.buildTransaction(transactionDto);
                        return repository.save(bankTransaction);
                    }));
	}	

	//metodo para realizar deposito en cuenta
	@Override
	public Mono<BankTransaction> saveBankTransactionDeposit(BankTransactionDto transactionDto){
		//obtenemos el saldo actual para modificarlo
		return externalApiService.getAccountBalanceByAccountNumber(transactionDto
					.getBankAccountNumber())
	            .flatMap(accountBalance -> {
	                Double newAmount = accountBalance + transactionDto.getAmount();
	                externalApiService.updateAccountBalance(transactionDto.getBankAccountNumber(),newAmount)
	                	.subscribe();
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

	//metodo para cargar consumo en tarjeta de credito
	@Override
	public Mono<BankTransaction> saveBankTransactionCardUsage(BankTransactionDto transactionDto){
		return externalApiService.getCreditCardBalanceByAccountNumber(transactionDto.getBankAccountNumber())
				.flatMap(accountBalance -> {
                    //si el saldo es mayor a la carga del consumo
                	if (accountBalance < transactionDto.getAmount()) {
                        return Mono.error(new RuntimeException("Saldo en cuenta es insuficiente."));
                    } else {
                    	Double newAmount= accountBalance - transactionDto.getAmount();
                    	//actualizamos el credito en customer-credit-account nuevo saldo
                    	externalApiService.updateCreditAccountBalance(transactionDto.getBankAccountNumber()
                    			,newAmount).subscribe();
                    	//creamos las cuotas de pago en credit-debts y registramos la transaccion
                    	try {
                    		externalApiService.createDebtsAccount(transactionDto);
						} catch (JsonProcessingException e) {
							e.printStackTrace();
						}
	                	BankTransaction bankTransaction = BankTransactionBuilder
	                			.buildTransaction(transactionDto);
                        return repository.save(bankTransaction);
                    }
                });	
	}
	


}
