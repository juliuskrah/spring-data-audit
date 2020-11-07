package com.juliuskrah.audit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
public class ApplicationTests {
	@Autowired
	private CustomerRepository repository;
	@Autowired
	private ApplicationContext context;

	private WebTestClient rest;

	@BeforeEach
	public void setup() {
		StepVerifier.create(repository.deleteAll()).verifyComplete();
		this.rest = WebTestClient.bindToApplicationContext(this.context) //
				.apply(springSecurity()).configureClient() //
				.filter(basicAuthentication("julius", "secret")).build();
	}

	@Test
	public void customerWhenCreateThenCreatedByIsNotNull() {
		Customer customer = new Customer();
		customer.setName("Jack Sparrow");
		customer.setEmail("jacksparrow@strangertides.com");

		this.rest.post().uri("/customers/") //
				.body(Mono.just(customer), Customer.class).exchange() //
				.expectStatus().isCreated();

		this.rest.get().uri("/customers/").exchange() //
				.expectStatus().isOk().expectBody().jsonPath("$[0].createdBy").exists();
	}

	@Test
	public void customerWhenUpdateThenLastModifiedDateIsNotNull() {
		Customer customer = new Customer();
		customer.setName("Elizabeth Swarn");
		customer.setEmail("lizzyswarn@strangertides.com");

		StepVerifier.create(repository.save(customer)).assertNext(c -> {
			assertThat(c.getId()).isNotNull();
			customer.setId(c.getId());
		}).verifyComplete();
		customer.setOrder("food stuff");

		this.rest.put().uri("/customers/{id}", customer.getId()) //
				.body(Mono.just(customer), Customer.class) //
				.exchange().expectStatus().isOk();

		this.rest.get().uri("/customers/{id}", customer.getId()) //
				.exchange().expectStatus().isOk() //
				.expectBody().jsonPath("$.lastModifiedBy").exists();
	}

	@Test
	public void customerWhenCreateThenCreatedDateIsNotNull() {
		Customer customer = new Customer();
		customer.setName("James Gunn");
		customer.setEmail("jamesgunn@gmail.com");

		StepVerifier.create(repository.save(customer)) //
				.assertNext(c -> {
					assertThat(c.getId()).isNotNull();
					assertThat(c.getCreatedDate()).isNotNull();
					assertThat(c.getCreatedDate()).isBefore(now());
				}).verifyComplete();

	}

	@Test
	public void customerWhenUpdateThenLastModifiedDateIsAfterCreatedDate() {
		Customer customer = new Customer();
		customer.setName("Tyler Perry");
		customer.setEmail("tylerperry@gmail.com");

		StepVerifier.create(repository.save(customer)).assertNext(c -> {
			assertThat(c.getId()).isNotNull();
			customer.setId(c.getId());
		}).verifyComplete();
		Mono<Customer> c = repository.findById(customer.getId()).flatMap(cus -> {
			cus.setOrder("Samsung Galaxy");
			return repository.save(cus);
		});

		StepVerifier.create(c).assertNext(cus -> {
			assertThat(cus).isNotNull();
			assertThat(cus.getLastModifiedDate()).isAfter(cus.getCreatedDate());
		}).verifyComplete();
	}

}
