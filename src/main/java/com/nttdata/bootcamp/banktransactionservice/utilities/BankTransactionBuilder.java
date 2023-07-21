package com.nttdata.bootcamp.banktransactionservice.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.nttdata.bootcamp.banktransactionservice.documents.BankTransaction;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransactionDto;
import com.nttdata.bootcamp.banktransactionservice.dto.BankTransferDto;

public class BankTransactionBuilder {
	public static final String TRANSACTION_TYPE_COMMISSION_FEE = "COMISSION_FEE";
	public static final Double TRANSACTION_FEE = 5.00;

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

	public static BankTransaction buildTransactionByTransfer(BankTransferDto transferDto) {
		LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return BankTransaction.builder()
                .amount(transferDto.getAmount())
                .type(transferDto.getTypeTransaction())
                .date(currentDate.format(formatter))
                .description(transferDto.getDescription())
                .bankAccountNumber(transferDto.getOriginNumberAccount())
                .build();
	}

	public static BankTransaction buildTransactionWithProduct(BankTransactionDto transactionDto) {
		LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return BankTransaction.builder()
				.amount(TRANSACTION_FEE)
				.bankAccountNumber(transactionDto.getBankAccountNumber())
				.date(currentDate.format(formatter))
				.type(TRANSACTION_TYPE_COMMISSION_FEE)
				.description(TRANSACTION_TYPE_COMMISSION_FEE)
				.typeAccount(transactionDto.getTypeAccount())
				.build();
	}
}
