package org.hibernate.bugs;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.Consumer;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	@Before
	public void init() {

		entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");

	}

	@After
	public void destroy() {
		entityManagerFactory.close();
	}

	// Entities are auto-discovered, so just add them anywhere on class-path
	// Add your tests, using standard JUnit.
	@Test
	public void hhh123Test() {

		withEm(em -> {

			EntityTwo a2 = new EntityTwo();
			a2.name = "alpha 2";

			EntityOne alpha = new EntityOne();
			alpha.name = "alpha";
			alpha.two = a2;

			em.persist(alpha);

		});


		withEm(em -> {

			CriteriaQuery<Object> query = em.getCriteriaBuilder().createQuery(Object.class);
			Root<EntityOne> root = query.from(EntityOne.class);
			query = query.multiselect(root.get("id"), root.get("name"), root.get("two"));

			List<Object> resultList = em.createQuery(query).getResultList();

			Assert.assertEquals(1, resultList.size());
			resultList.forEach(o -> {
				Assert.assertTrue(o + " must be an instance of Object[]", o instanceof Object[]);
				Assert.assertNotNull("the referenced entity is missing", ((Object[]) o)[2]);
			});
		});

	}

	void withEm(Consumer<EntityManager> consumer) {

		EntityManager em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();

		try {
			consumer.accept(em);
		} finally {

			em.getTransaction().commit();
			em.close();
		}

	}
}
