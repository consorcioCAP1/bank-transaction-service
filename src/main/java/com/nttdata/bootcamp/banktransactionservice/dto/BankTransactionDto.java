package com.nttdata.bootcamp.banktransactionservice.dto;

import lombok.Data;

@Data
public class BankTransactionDto {
	private String id;
	private Double amount;
	private String type;
	private String date;
	private String description;
	private String bankAccountNumber;
	private int paymentBankFee;
	private String paymentStartDate;
	private String typeAccount;
}
