package com.zootcat.controllers.physics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.zootcat.controllers.factory.ControllerAnnotations;
import com.zootcat.events.ZootActorEventCounterListener;
import com.zootcat.events.ZootEvent;
import com.zootcat.events.ZootEventType;
import com.zootcat.physics.ZootPhysics;
import com.zootcat.scene.ZootActor;
import com.zootcat.scene.ZootScene;

public class DetectGroundControllerTest
{		
	private static final float ACTOR_WIDTH = 20.0f;
	private static final float ACTOR_HEIGHT = 10.0f;
	private static final float SCENE_UNIT_SCALE = 1.0f;
	
	@Mock private ZootScene scene;
	@Mock private Contact contact;
	@Mock private Manifold manifold;
	@Mock private Fixture otherFixture;
	@Mock private ContactImpulse contactImpulse;
	
	private ZootActor ctrlActor;
	private ZootActor otherActor;
	private PhysicsBodyController otherActorPhysicsCtrl;
	private ZootPhysics physics;
	private PhysicsBodyController physicsCtrl;
	private DetectGroundController groundCtrl;
	private ZootActorEventCounterListener eventCounter;
	
	@Before
	public void setup()
	{
		//create physics
		physics = new ZootPhysics();
		
		//create scene
		MockitoAnnotations.initMocks(this);
		when(scene.getPhysics()).thenReturn(physics);
		when(scene.getUnitScale()).thenReturn(SCENE_UNIT_SCALE);		
		
		//create main actor
		ctrlActor = new ZootActor();
		ctrlActor.setSize(ACTOR_WIDTH, ACTOR_HEIGHT);
		
		eventCounter = new ZootActorEventCounterListener();
		ctrlActor.addListener(eventCounter);
		
		physicsCtrl = new PhysicsBodyController();
		ControllerAnnotations.setControllerParameter(physicsCtrl, "scene", scene);
		
		physicsCtrl.init(ctrlActor);		
		ctrlActor.addController(physicsCtrl);
		
		//create other actor
		otherActor = new ZootActor();
		otherActor.setSize(ACTOR_WIDTH, ACTOR_HEIGHT);
		
		otherActorPhysicsCtrl = new PhysicsBodyController();
		ControllerAnnotations.setControllerParameter(otherActorPhysicsCtrl, "scene", scene);
		
		otherActorPhysicsCtrl.init(ctrlActor);
		otherActor.addController(otherActorPhysicsCtrl);
		
		//create ground detector controller
		groundCtrl = new DetectGroundController();
		ControllerAnnotations.setControllerParameter(groundCtrl, "scene", scene);	
	}
	
	@After
	public void tearDown()
	{
		physics.dispose();
		physics = null;
	}
		
	@Test
	public void shouldAddFeetFixtureToPhysicsBodyCtrl()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		
		//then
		assertTrue(physicsCtrl.getFixtures().contains(groundCtrl.getFeetFixture()));
	}
	
	@Test
	public void shouldRemoveFeetFixtureFromPhysicsBodyCtrl()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		groundCtrl.onRemove(ctrlActor);
		
		//then
		assertFalse("Sensor fixture should be removed", physicsCtrl.getFixtures().contains(groundCtrl.getFeetFixture()));
		assertFalse("Actor should have deregistered ground detector listener", ctrlActor.getListeners().contains(groundCtrl, true));
	}
	
	@Test
	public void shouldCreateFeetFixture()
	{		
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);

		//then
		assertEquals("Sensor fixture should be present", 2, physicsCtrl.getFixtures().size());
		
		Fixture feetFixture = groundCtrl.getFeetFixture();
		assertTrue("Fixture should be a sensor", feetFixture.isSensor());		
		assertEquals("Fixture should be assigned to proper body", physicsCtrl.getBody(), feetFixture.getBody());
		assertEquals("Fixture shape should be polygon", Type.Polygon, feetFixture.getShape().getType());		
		assertTrue("Actor should have registered ground detector listener", ctrlActor.getListeners().contains(groundCtrl, true));
	}
	
	@Test
	public void shouldCreateFeetFixtureUsingCustomWidth()
	{
		//when
		final int customWidth = 128;
		ControllerAnnotations.setControllerParameter(groundCtrl, "sensorWidth", customWidth);
		
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		PolygonShape fixtureShape = (PolygonShape)groundCtrl.getFeetFixture().getShape();
		
		//then
		Vector2 vertex1 = new Vector2();
		Vector2 vertex2 = new Vector2();
		Vector2 vertex3 = new Vector2();
		fixtureShape.getVertex(0, vertex1);
		fixtureShape.getVertex(1, vertex2);
		fixtureShape.getVertex(2, vertex3);
		assertEquals("Sensor should have actor width", customWidth, Math.abs(vertex1.x - vertex2.x), 0.0f);
		assertEquals("Sensor should have 10% of actor height", ACTOR_HEIGHT * DetectGroundController.SENSOR_HEIGHT_PERCENT, 
					Math.abs(vertex1.y - vertex3.y), 0.0f);
	}
	
	@Test
	public void shouldCreateFeetFixtureUsingActorWidthWhenWidthIsSetToZero()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		PolygonShape fixtureShape = (PolygonShape)groundCtrl.getFeetFixture().getShape();
		
		//then
		Vector2 vertex1 = new Vector2();
		Vector2 vertex2 = new Vector2();
		Vector2 vertex3 = new Vector2();
		fixtureShape.getVertex(0, vertex1);
		fixtureShape.getVertex(1, vertex2);
		fixtureShape.getVertex(2, vertex3);
		assertEquals("Sensor should have actor width", ACTOR_WIDTH, Math.abs(vertex1.x - vertex2.x), 0.0f);
		assertEquals("Sensor should have 10% of actor height", ACTOR_HEIGHT * DetectGroundController.SENSOR_HEIGHT_PERCENT, 
					Math.abs(vertex1.y - vertex3.y), 0.0f);
	}
	
	@Test
	public void shouldNotDetectGroundByDefault()
	{
		//given
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		
		//then
		assertFalse("Should not be on ground by default", groundCtrl.isOnGround());
	}
	
	@Test
	public void shouldCreateFeetFixtureWithTheSameCollisionFilterAsForControllerActor()
	{
		//given
		Filter expectedFilter = mock(Filter.class);
		expectedFilter.categoryBits = 1;
		expectedFilter.maskBits = 2;
		expectedFilter.groupIndex = 3;
		
		CollisionFilterController filterCtrl = mock(CollisionFilterController.class);
		when(filterCtrl.getCollisionFilter()).thenReturn(expectedFilter);
		
		//when
		ctrlActor.addController(filterCtrl);
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		
		//then		
		Filter feetFilter = groundCtrl.getFeetFixture().getFilterData();
		assertNotNull(feetFilter);
		assertEquals(expectedFilter.categoryBits, feetFilter.categoryBits);
		assertEquals(expectedFilter.maskBits, feetFilter.maskBits);
		assertEquals(expectedFilter.groupIndex, feetFilter.groupIndex);
	}
		
	@Test
	public void shouldProperlyDetectGroundForNormalFixture()
	{		
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);		
		when(contact.getFixtureA()).thenReturn(groundCtrl.getFeetFixture());
		when(contact.getFixtureB()).thenReturn(otherFixture);
		when(contact.isEnabled()).thenReturn(true);
		
		groundCtrl.beginContact(ctrlActor, otherActor, contact);
		groundCtrl.preSolve(ctrlActor, otherActor, contact, manifold);
		groundCtrl.postSolve(ctrlActor, otherActor, contactImpulse);
		groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertTrue("Should detect ground", groundCtrl.isOnGround());
		assertTrue("Ground event should be sent to actor", isGroundEvent(eventCounter.getLastEvent()));
		
		//when
		groundCtrl.endContact(ctrlActor, otherActor, contact);
		
		//then
		assertTrue("Should not detect ground when fixture is not colliding", groundCtrl.isOnGround());
		assertEquals("Should send only 1 event", 1, eventCounter.getCount());
	}
	
	@Test
	public void shouldFireEventsOnlyWhenGroundIsDetected()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);		
		when(contact.getFixtureA()).thenReturn(groundCtrl.getFeetFixture());
		when(contact.getFixtureB()).thenReturn(otherFixture);
		when(contact.isEnabled()).thenReturn(true);
		
		groundCtrl.beginContact(ctrlActor, otherActor, contact);
		groundCtrl.preSolve(ctrlActor, otherActor, contact, manifold);
		groundCtrl.postSolve(ctrlActor, otherActor, contactImpulse);
		
		for(int i = 0; i < 10; ++i) groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertTrue("Should detect ground", groundCtrl.isOnGround());
		assertEquals("Should send 10 events", 10, eventCounter.getCount());
		assertTrue("Should send events of Ground type", isGroundEvent(eventCounter.getLastEvent()));
		
		//when
		groundCtrl.endContact(ctrlActor, otherActor, contact);
		groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertFalse("Should not detect ground when fixture is not colliding", groundCtrl.isOnGround());
		assertEquals("Should send only 10 events", 10, eventCounter.getCount());
	}
		
	@Test
	public void shouldProperlyDetectGroundWhenContactIsDisabledAndReenabled()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		when(contact.getFixtureA()).thenReturn(groundCtrl.getFeetFixture());
		when(contact.getFixtureB()).thenReturn(otherFixture);
		when(contact.isEnabled()).thenReturn(false);
		
		groundCtrl.beginContact(ctrlActor, otherActor, contact);
		groundCtrl.preSolve(ctrlActor, otherActor, contact, manifold);
		groundCtrl.postSolve(ctrlActor, otherActor, contactImpulse);
		groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertFalse("Should not detect ground when contact was disabled", groundCtrl.isOnGround());
		assertEquals("No event should be sent", 0, eventCounter.getCount());
		
		//when
		when(contact.isEnabled()).thenReturn(true);
		groundCtrl.preSolve(ctrlActor, otherActor, contact, manifold);
		groundCtrl.postSolve(ctrlActor, otherActor, contactImpulse);
		groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertTrue("Should detect ground", groundCtrl.isOnGround());
		assertTrue("Ground event should be sent to actor", isGroundEvent(eventCounter.getLastEvent()));
	}
	
	@Test
	public void shouldProperlyDetectGroundWhenCollidingWithSensorFixtures()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);		
		when(contact.getFixtureA()).thenReturn(groundCtrl.getFeetFixture());
		when(contact.getFixtureB()).thenReturn(otherFixture);
		when(contact.isEnabled()).thenReturn(true);
		when(otherFixture.isSensor()).thenReturn(true);
		
		groundCtrl.beginContact(ctrlActor, otherActor, contact);
		groundCtrl.preSolve(ctrlActor, otherActor, contact, manifold);
		groundCtrl.postSolve(ctrlActor, otherActor, contactImpulse);
		groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertFalse("Should not detect ground between sensors", groundCtrl.isOnGround());
		assertEquals("No event should not be sent to actor", 0, eventCounter.getCount());
		
		//when
		when(otherFixture.isSensor()).thenReturn(false);
		groundCtrl.preSolve(ctrlActor, otherActor, contact, manifold);
		groundCtrl.postSolve(ctrlActor, otherActor, contactImpulse);
		groundCtrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertTrue("Should detect ground when fixture is no longer a sensor", groundCtrl.isOnGround());
		assertTrue("Ground event should be sent to actor", isGroundEvent(eventCounter.getLastEvent()));
	}
				
	@Test
	public void shouldProperlyHandleMultiplyContacts()
	{
		//when
		groundCtrl.init(ctrlActor);
		groundCtrl.onAdd(ctrlActor);
		
		Fixture fixtureB = mock(Fixture.class);
		Fixture fixtureC = mock(Fixture.class);
		Fixture fixtureD = mock(Fixture.class);
		Fixture groundSensorFixture = groundCtrl.getFeetFixture();
			
		Contact contact1 = mock(Contact.class);
		when(contact1.getFixtureA()).thenReturn(groundSensorFixture);
		when(contact1.getFixtureB()).thenReturn(fixtureB);
		when(contact1.isEnabled()).thenReturn(true);
		
		Contact contact2 = mock(Contact.class);
		when(contact2.getFixtureA()).thenReturn(groundSensorFixture);
		when(contact2.getFixtureB()).thenReturn(fixtureC);
		when(contact2.isEnabled()).thenReturn(true);
		
		Contact contact3 = mock(Contact.class);
		when(contact3.getFixtureA()).thenReturn(groundSensorFixture);
		when(contact3.getFixtureB()).thenReturn(fixtureD);
		when(contact3.isEnabled()).thenReturn(true);
		
		//when
		groundCtrl.beginContact(ctrlActor, otherActor, contact1);
		groundCtrl.beginContact(ctrlActor, otherActor, contact2);
		groundCtrl.beginContact(ctrlActor, otherActor, contact3);
		groundCtrl.preSolve(ctrlActor, otherActor, contact1, mock(Manifold.class));
		groundCtrl.preSolve(ctrlActor, otherActor, contact2, mock(Manifold.class));
		groundCtrl.preSolve(ctrlActor, otherActor, contact3, mock(Manifold.class));			
		groundCtrl.onUpdate(1.0f, ctrlActor);
		
		//then
		assertTrue("Ground should be detected", groundCtrl.isOnGround());
		assertEquals("Event should be sent", 1, eventCounter.getCount());
		
		//when only one contact is left enabled
		when(contact2.isEnabled()).thenReturn(false);
		when(contact3.isEnabled()).thenReturn(false);
		groundCtrl.preSolve(ctrlActor, otherActor, contact1, mock(Manifold.class));
		groundCtrl.preSolve(ctrlActor, otherActor, contact2, mock(Manifold.class));
		groundCtrl.preSolve(ctrlActor, otherActor, contact3, mock(Manifold.class));			

		//this is required to simulate box2d behaviour, all contacts are reenabled after postsolve
		when(contact2.isEnabled()).thenReturn(true);
		when(contact3.isEnabled()).thenReturn(true);

		groundCtrl.onUpdate(1.0f, ctrlActor);
		
		//then
		assertTrue("Ground should be detected", groundCtrl.isOnGround());
		assertEquals("Event should be sent", 2, eventCounter.getCount());
		
		//when all contacts are disabled
		when(contact1.isEnabled()).thenReturn(false);
		when(contact2.isEnabled()).thenReturn(false);
		when(contact3.isEnabled()).thenReturn(false);
		groundCtrl.preSolve(ctrlActor, otherActor, contact1, mock(Manifold.class));
		groundCtrl.preSolve(ctrlActor, otherActor, contact2, mock(Manifold.class));
		groundCtrl.preSolve(ctrlActor, otherActor, contact3, mock(Manifold.class));			
		
		//this is required to simulate box2d behaviour, all contacts are reenabled after postsolve
		when(contact1.isEnabled()).thenReturn(true);
		when(contact2.isEnabled()).thenReturn(true);
		when(contact3.isEnabled()).thenReturn(true);
		
		groundCtrl.onUpdate(1.0f, ctrlActor);
		
		//then
		assertFalse("Ground should not be detected", groundCtrl.isOnGround());
		assertEquals("Event should not be sent", 2, eventCounter.getCount());
	}
	
	@Test
	public void shouldDoNothingOnPostSolve()
	{
		ZootActor actorA = mock(ZootActor.class);
		ZootActor actorB = mock(ZootActor.class);
		ContactImpulse contactImpulse = mock(ContactImpulse.class);
				
		groundCtrl.postSolve(actorA, actorB, contactImpulse);
		verifyZeroInteractions(actorA, actorB, contactImpulse);
	}
	
	private boolean isGroundEvent(Event event)
	{
		if(ClassReflection.isInstance(ZootEvent.class, event))
		{
			return ((ZootEvent)event).getType() == ZootEventType.Ground;
		}
		return false;
	}
}
