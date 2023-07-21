package com.nttdata.bootcamp.banktransactionservice.documents;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Document(collection = "bankTransaction")
@AllArgsConstructor
@Data
@Builder
public class BankTransaction {

	@Id
	private String id;
	private Double amount;
	private String type;
	private String date;
	private String description;
	private String bankAccountNumber;
	private String typeAccount;

}
