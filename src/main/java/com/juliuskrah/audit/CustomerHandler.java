package com.juliuskrah.audit;

import java.net.URI;
import java.util.Optional;

import org.springframework.core.ParameterizedTypeReference;
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
		Iterable<Customer> customers = repository.findAll();
		return ServerResponse.ok().body(Mono.just(customers), new ParameterizedTypeReference<Iterable<Customer>>() {
		});
	}

	public Mono<ServerResponse> retrieveCustomer(ServerRequest request) {
		String id = request.pathVariable("id");
		Optional<Customer> customer = repository.findById(id);
		if (customer.isPresent())
			return ServerResponse.ok().body(Mono.just(customer.get()), Customer.class);
		return ServerResponse.notFound().build();
	}

	public Mono<ServerResponse> createCustomer(ServerRequest request) {
		Mono<Customer> customer = request.bodyToMono(Customer.class);

		return customer.flatMap(c -> {
			repository.save(c);
			URI uri = request.uriBuilder().path("/{id}").build(c.getId());
			return ServerResponse.created(uri).build();
		});
	}

	public Mono<ServerResponse> updateCustomer(ServerRequest request) {
		Mono<Customer> customer = request.bodyToMono(Customer.class);

		return customer.flatMap(c -> {
			String id = request.pathVariable("id");
			Optional<Customer> custom = repository.findById(id);
			if (custom.isPresent()) {
				String order = c.getOrder();
				c = custom.get();
				c.setOrder(order);
				repository.save(c);
			} else
				return ServerResponse.notFound().build();
			return ServerResponse.noContent().build();
		});
	}

	public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
		String id = request.pathVariable("id");
		repository.deleteById(id);

		return ServerResponse.noContent().build();
	}
}
