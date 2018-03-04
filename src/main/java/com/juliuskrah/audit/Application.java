package com.juliuskrah.audit;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public RouterFunction<ServerResponse> customerRouter(CustomerHandler customerHandler) {
		return nest(path("/customers"),
				route(GET("/").and(accept(APPLICATION_JSON)), customerHandler::retrieveCustomers)
						.andRoute(GET("/{id}").and(accept(APPLICATION_JSON)), customerHandler::retrieveCustomer)
						.andRoute(POST("/").and(contentType(APPLICATION_JSON)), customerHandler::createCustomer)
						.andRoute(PUT("/{id}").and(contentType(APPLICATION_JSON)), customerHandler::updateCustomer)
						.andRoute(DELETE("/{id}"), customerHandler::deleteCustomer));
	}
}
