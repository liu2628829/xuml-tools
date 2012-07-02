package xuml.tools.jaxb.compiler.test;

import static org.junit.Assert.assertEquals;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Test;

import xuml.tools.model.compiler.runtime.Signaller;
import xuml.tools.util.database.DerbyUtil;
import abc.A;
import abc.A.AId;
import abc.A.Events.Create;
import abc.A.Events.StateSignature_DoneSomething;

public class AbcTest {

	@Test
	public void testCreateEntityManagerFactoryAndCreateAndPersistOneEntity()
			throws InterruptedException {
		DerbyUtil.disableDerbyLog();
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("abc");
		Signaller.getInstance().setEntityManagerFactory(emf);

		A.setBehaviourFactory(new A.BehaviourFactory() {
			@Override
			public A.Behaviour create(final A entity) {
				return new A.Behaviour() {

					@Override
					public void onEntryHasStarted(Create event) {
						AId id = new AId();
						id.setAOne(event.getAOne());
						id.setATwo(event.getATwo());
						entity.setId(id);
						entity.setAThree(event.getAccountNumber());
						System.out.println("created");
					}

					@Override
					public void onEntryDoneSomething(
							StateSignature_DoneSomething event) {
						entity.setAThree(event.getTheCount());
						System.out.println(event.getTheCount());
					}
				};
			}
		});

		Signaller.getInstance().sendSignalsInQueue();

		EntityManager em = emf.createEntityManager();

		A a1 = A.create(em, new A.Events.Create("value1.1", "value2.1", "1234"));
		A a2 = A.create(em, new A.Events.Create("value1.2", "value2.2", "1234"));
		A a3 = A.create(em, new A.Events.Create("value1.3", "value2.3", "1234"));

		em.close();

		a1.signal(new A.Events.SomethingDone("12a"));
		a2.signal(new A.Events.SomethingDone("12b"));
		a3.signal(new A.Events.SomethingDone("12c"));
		Thread.sleep(5000);

		em = emf.createEntityManager();
		assertEquals("12a", a1.merge(em).getAThree());
		assertEquals("12b", a2.merge(em).getAThree());
		assertEquals("12c", a3.merge(em).getAThree());
		em.close();

		Signaller.getInstance().stop();

	}

	@Test(expected = NullPointerException.class)
	public void testBehaviourNotSetForAThrowsException() {
		A.setBehaviourFactory(null);
		new A();
	}
}
