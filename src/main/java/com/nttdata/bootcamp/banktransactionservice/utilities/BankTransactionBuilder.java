package com.nttdata.bootcamp.banktransactionservice.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;

public class BankTransactionBuilder {
	
	private BankTransactionBuilder(){}
	public static BankTransaction buildTransaction(BankTransactionDto transactionDto) {
		LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return BankTransaction.builder()
				.amount(transactionDto.getAmount())
				.bankAccountNumber(transactionDto.getBankAccountNumber())
				.date(currentDate.format(formatter))
				.type(transactionDto.getType())
				.description(transactionDto.getDescription())
				.build();
	}
}
