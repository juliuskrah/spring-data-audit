package com.juliuskrah.audit;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class SpringSecurityAuditorAware implements AuditorAware<String> {
		
	@Override
	public Optional<String> getCurrentAuditor() {
		return ReactiveSecurityContextHolder.getContext()
				.map(SecurityContext::getAuthentication)
				.filter(Authentication::isAuthenticated)
				.map(Authentication::getName)
				.switchIfEmpty(Mono.just("julius"))
				.blockOptional();
	  }
}
