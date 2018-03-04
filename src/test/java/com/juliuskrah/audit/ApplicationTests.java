package com.juliuskrah.audit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
	@Autowired
	private CustomerRepository repository;
	@Autowired
	private ApplicationContext context;

	private WebTestClient rest;

	@Before
	public void setup() {
		this.rest = WebTestClient
				.bindToApplicationContext(this.context)
				.apply(springSecurity())
				.configureClient()
				.filter(basicAuthentication("julius", "secret"))
				.build();
	}
	
	@Test
	public void customerWhenCreateThenCreatedByIsNotNull() {
		Customer customer = new Customer();
		customer.setName("Jack Sparrow");
		customer.setEmail("jacksparrow@strangertides.com");
		
		this.rest
			.mutateWith(csrf())
			.post()
			.uri("/customers")
			.body(Mono.just(customer), Customer.class)
			.exchange()
			.expectStatus().isCreated();
		
		this.rest
			.get()
			.uri("/customers")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$[0].createdBy").exists();
	}
	
	@Test
	public void customerWhenUpdateThenLastModifiedDateIsNotNull() {
		Customer customer = new Customer();
		customer.setName("Elizabeth Swarn");
		customer.setEmail("lizzyswarn@strangertides.com");
		
		repository.save(customer);
		customer.setOrder("food stuff");
		
		this.rest
			.mutateWith(csrf())
			.put()
			.uri("/customers/{id}", customer.getId())
			.body(Mono.just(customer), Customer.class)
			.exchange()
			.expectStatus().isNoContent();
		
		this.rest
			.get()
			.uri("/customers/{id}", customer.getId())
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.lastModifiedBy").exists();
	}

	@Test
	public void customerWhenCreateThenCreatedDateIsNotNull() {
		Customer customer = new Customer();
		customer.setName("James Gunn");
		customer.setEmail("jamesgunn@gmail.com");

		repository.save(customer);

		assertThat(customer.getId()).isNotNull();
		assertThat(customer.getCreatedDate()).isNotNull();
		assertThat(customer.getCreatedDate()).isBefore(now());
	}

	@Test
	public void customerWhenUpdateThenLastModifiedDateIsAfterCreatedDate() {
		Customer customer = new Customer();
		customer.setName("Tyler Perry");
		customer.setEmail("tylerperry@gmail.com");

		repository.save(customer);
		Optional<Customer> c = repository.findById(customer.getId());

		assertThat(c.isPresent()).isTrue();

		customer = c.get();
		customer.setOrder("Samsung Galaxy");

		customer = repository.save(customer);

		assertThat(customer.getLastModifiedDate()).isAfter(customer.getCreatedDate());
	}

}
