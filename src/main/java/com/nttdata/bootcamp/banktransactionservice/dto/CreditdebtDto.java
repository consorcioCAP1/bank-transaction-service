package com.nttdata.bootcamp.banktransactionservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreditdebtDto {
	private Double paymentAmount;
	private int bankFeeNumber;
	private String bankAccountNumber;
	private String outStandingBankFee;
	private int numberBankPaymentInstallments;
	private String paymentStartDate;
}
