package com.juliuskrah.audit;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class CustomerRepository {
	@PersistenceContext
	private EntityManager em;

	public Customer save(Customer customer) {
		if (customer.getId() == null)
			em.persist(customer);
		else
			customer = em.merge(customer);
		return customer;
	}

	public void delete(String id) {
		Optional<Customer> customer = findOne(id);
		if (customer.isPresent())
			delete(customer.get());
	}

	public void delete(Customer customer) {
		em.remove(customer);
	}

	@Transactional(readOnly = true)
	public Optional<Customer> findOne(String id) {
		return Optional.ofNullable(em.find(Customer.class, id));
	}
}
