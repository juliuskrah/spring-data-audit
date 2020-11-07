package com.juliuskrah.audit;

import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;
import static io.r2dbc.spi.ConnectionFactoryOptions.HOST;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import reactor.core.publisher.Mono;

@SpringBootApplication(exclude = R2dbcAutoConfiguration.class)
@EnableR2dbcAuditing
@EnableR2dbcRepositories
public class Application extends AbstractR2dbcConfiguration {

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

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http.authorizeExchange(exchanges -> exchanges //
				.anyExchange().authenticated() //
		).csrf(csfr -> csfr.disable()) //
				.httpBasic(withDefaults()) //
				.formLogin(withDefaults());
		return http.build();
	}

	@Bean
	public BeforeConvertCallback<Customer> addGeneratedId() {
		return (entity, table) -> {
			if (entity.getId() == null)
				entity.setId(UUID.randomUUID().toString());
			return Mono.just(entity);
		};
	}

	@Override
	public ConnectionFactory connectionFactory() {
		return ConnectionFactories.get(ConnectionFactoryOptions.builder() //
				.option(DATABASE, "r2dbc") //
				.option(HOST, "localhost") //
				.option(USER, "r2dbc") //
				.option(PASSWORD, "r2dbc") //
				.option(DRIVER, "postgresql") //
				.build());
	}
}
