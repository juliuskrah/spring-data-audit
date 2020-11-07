package com.juliuskrah.audit;

import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SpringSecurityAuditorAware implements ReactiveAuditorAware<String> {

	@Override
	public Mono<String> getCurrentAuditor() {
		return ReactiveSecurityContextHolder.getContext() //
				.map(SecurityContext::getAuthentication) //
				.filter(Authentication::isAuthenticated) //
				.map(Authentication::getName) //
				.switchIfEmpty(Mono.just("system"));
	}
}
