package org.hibernate.bugs;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {

	private EntityManagerFactory entityManagerFactory;

	String sql = "select\n" +
			"        entityone0_.id as col_0_0_,\n" +
			"        entityone0_.name as col_1_0_,\n" +
			"        entityone0_.id as col_2_0_,\n" +
			"        entitytwo1_.id as id1_1_,\n" +
			"        entitytwo1_.name as name2_1_ \n" +
			"    from\n" +
			"        EntityOne entityone0_ \n" +
			"    inner join\n" +
			"        EntityTwo entitytwo1_ \n" +
			"            on entityone0_.id=entitytwo1_.id";


	@Before
	public void init() {

		entityManagerFactory = Persistence.createEntityManagerFactory("templatePU");
		initData();

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

	@Test
	public void testDataIsActuallyAvailable() {
		withEm(em -> {
			Session session = (Session) em.getDelegate();
			session.doWork(con -> {
				JdbcTemplate template = new JdbcTemplate(new SingleConnectionDataSource(con, false));
				AtomicBoolean atLeastOne = new AtomicBoolean(false);
				template.query(sql, resultSet -> {
					int count = resultSet.getMetaData().getColumnCount();
					for (int i = 1; i <= count; i++) {
						Assert.assertNotNull("column " + i + " is null", resultSet.getObject(i));
						atLeastOne.set(true);
					}
				});
				Assert.assertTrue(atLeastOne.get());
			});
		});
	}


	private void initData() {
		withEm(em -> {

			EntityTwo a2 = new EntityTwo();
			a2.name = "alpha 2";

			EntityOne alpha = new EntityOne();
			alpha.name = "alpha";
			alpha.two = a2;

			em.persist(alpha);

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
