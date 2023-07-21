package com.nttdata.bootcamp.banktransactionservice.dto;

import lombok.Data;

@Data
public class BankTransferDto {
	private String originNumberAccount;
	private String destinationNumberAccount;
	private Double amount;
	private String typeTransaction;
	private String description;

}
