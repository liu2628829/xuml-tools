package com.github.davidmoten;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import ordertracker.Context;


public class App {

	public static void startup() {
		// create the entity manager factory
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("testPersistenceUnit");

		// pass the EntityManagerFactory to the generated xuml Context
		Context.setEntityManagerFactory(emf);
		
		// setup behaviour factories and assign them to Context here
		
		// set the behaviour factory for the class A
		// A.setBehaviourFactory(createBehaviourFactory());
		
		// send any signals not processed from last shutdown
		Context.sendSignalsInQueue();
	}
	
	public static void shutdown() {
		// shutdown the actor system
		Context.stop();

		// close the entity manager factory if desired
		Context.close();

	}
	
}