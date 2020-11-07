package com.juliuskrah.audit;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Data;

@Data
@Table
public class Customer implements Serializable {

	private static final long serialVersionUID = 265315120455998196L;

	@Id
	private String id;
	private String name;
	private String email;
	@Column("orders")
	private String order;
	@CreatedBy
	private String createdBy;
	@CreatedDate
	private LocalDateTime createdDate;
	@LastModifiedBy
	private String lastModifiedBy;
	@LastModifiedDate
	private LocalDateTime lastModifiedDate;
}
