package com.juliuskrah.audit;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerHandler {
	private final CustomerRepository repository;

	public Mono<ServerResponse> retrieveCustomers(ServerRequest request) {
		var customers = repository.findAll();
		return ServerResponse.ok().body(customers, Customer.class);
	}

	public Mono<ServerResponse> retrieveCustomer(ServerRequest request) {
		var id = request.pathVariable("id");
		var customer = repository.findById(id);
		return customer.flatMap(mapper -> ServerResponse.ok().body(customer, Customer.class)) //
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> createCustomer(ServerRequest request) {
		var customer = request.bodyToMono(Customer.class);
		return customer.flatMap(repository::save) //
				.flatMap(c -> {
					URI uri = request.uriBuilder().path("/{id}").build(c.getId());
					return ServerResponse.created(uri).build();
				});
	}

	public Mono<ServerResponse> updateCustomer(ServerRequest request) {
		var customer = request.bodyToMono(Customer.class);
		var id = request.pathVariable("id");

		return repository.findById(id) //
				.zipWith(customer, (t1, t2) -> {
					t1.setOrder(t2.getOrder());
					t1.setEmail(t2.getEmail());
					t1.setName(t2.getName());
					return t1;
				}).flatMap(repository::save) //
				.flatMap(c -> ServerResponse.ok().body(fromValue(c)))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

	public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
		var id = request.pathVariable("id");
		repository.deleteById(id).subscribe();
		return ServerResponse.noContent().build();
	}
}
