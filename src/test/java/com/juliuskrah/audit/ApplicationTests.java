package com.juliuskrah.audit;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.hibernate.envers.AuditReaderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@SpringJUnitConfig
public class ApplicationTests {

	@PersistenceUnit
	private EntityManagerFactory emf;
	private Customer customer;

	private EntityManagerFactory entityManagerFactory() {
		return this.emf;
	}

	@BeforeEach
	public void init() {
		this.customer = new Customer();
		this.customer.setName("Julius");
		this.customer.setEmail("juliuskrah@example.com");
	}

	@Test
	public void testAudits() {
		// Save
		doInJPA(this::entityManagerFactory, entityManager -> {
			entityManager.persist(this.customer);
			assertTrue(entityManager.contains(this.customer));
		});

		String id = this.customer.getId();

		Customer firstRevision = doInJPA(this::entityManagerFactory, entityManager -> {
			return AuditReaderFactory.get(entityManager).find(Customer.class, id, 1);
		});

		assertThat(firstRevision).isNotNull();
		assertThat(firstRevision.getCreatedDate()).isNotNull();
		assertThat(firstRevision.getCreatedDate()).isBefore(now());
		assertThat(firstRevision.getName()).isSameAs("Julius");

		// Retrieve and Update
		doInJPA(this::entityManagerFactory, entityManager -> {
			Customer customer = entityManager.find(Customer.class, id);
			customer.setItem("Biscuits");
			customer.setName("Abeiku");
		});

		Customer secondRevision = doInJPA(this::entityManagerFactory, entityManager -> {
			return AuditReaderFactory.get(entityManager).find(Customer.class, id, 2);
		});

		assertThat(secondRevision).isNotNull();
		assertThat(secondRevision.getLastModifiedDate()).isNotNull();
		assertThat(secondRevision.getLastModifiedDate()).isAfter(firstRevision.getCreatedDate());
		assertThat(secondRevision.getName()).isSameAs("Abeiku");

		// Delete
		doInJPA(this::entityManagerFactory, entityManager -> {
			Customer customer = entityManager.getReference(Customer.class, id);
			entityManager.remove(customer);
		});

		Customer thirdRevision = doInJPA(this::entityManagerFactory, entityManager -> {
			return AuditReaderFactory.get(entityManager).find(
					Customer.class, 
					Customer.class.getName(),
					id, 
					3,
					true);
		});

		assertThat(thirdRevision).isNotNull();
		assertThat(thirdRevision.getLastModifiedDate()).isNull();
		assertThat(thirdRevision.getName()).isNull();

		List<Number> revisions = doInJPA(this::entityManagerFactory, entityManager -> {
			return AuditReaderFactory.get(entityManager).getRevisions(Customer.class, id);
		});

		log.info("revisions: {}", revisions);
	}

}
