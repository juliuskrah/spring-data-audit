package com.juliuskrah.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
	@Autowired
	private CustomerRepository repository;
	
	@Test
	public void contextLoads() {
		Customer customer = new Customer();
		customer.setName("James Gunn");
		customer.setEmail("jamesgunn@gmail.com");
		
		repository.save(customer);
		
		assertThat(customer.getId()).isNotNull();
		assertThat(customer.getCreatedBy()).isSameAs("system");
	}

}
