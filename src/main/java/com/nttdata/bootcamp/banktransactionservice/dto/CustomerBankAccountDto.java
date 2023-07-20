package com.nttdata.bootcamp.banktransactionservice.dto;

import lombok.Data;

@Data
public class CustomerBankAccountDto {

	private String id;
	private String typeCustomer;
	private String clientId;
	private String name;
	private String dni;
	private String businessName;
	private String ruc;
	private Double maintenanceFeeApplies;
	private int bankMovementLimit;
	private int bankMovementDay;
	private String[] bankAccountHolder;
	private String[] bankSignatory;
	private Double accountBalance;
	private String openingDate;
	private String bankAccountNumber;
	private String accountType;
}


