package com.zootcat.controllers.logic.triggers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.physics.box2d.Contact;
import com.zootcat.controllers.factory.ControllerAnnotations;
import com.zootcat.fsm.events.ZootActorEventCounterListener;
import com.zootcat.fsm.events.ZootEvent;
import com.zootcat.fsm.events.ZootEventType;
import com.zootcat.scene.ZootActor;

public class TriggerControllerTest
{
	private TriggerController ctrl;	
	private ZootActor ctrlActor;
	private ZootActorEventCounterListener eventCounter;
	
	@Before
	public void setup()
	{		
		ctrlActor = new ZootActor();		
		eventCounter = new ZootActorEventCounterListener();
		ctrlActor.addListener(eventCounter);
		
		ctrl = new TriggerController();
		ctrl.init(ctrlActor);
	}

	@Test
	public void shouldPerformFirstTriggerOnFalseByDefault()
	{		
		//when
		ctrl.onAdd(ctrlActor);
		ctrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertEquals("Switch was not triggered", 1, eventCounter.getCount());
		assertEquals("Should be triggered as not active", ZootEventType.TriggerOff, ((ZootEvent)eventCounter.getLastEvent()).getType());
	}
	
	@Test
	public void shouldHaveControllerActorInEvent()
	{
		//when
		ctrl.onAdd(ctrlActor);
		ctrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertEquals("Switch was not triggered", 1, eventCounter.getCount());
		assertEquals(ctrlActor, ((ZootEvent)eventCounter.getLastEvent()).getUserObject(ZootActor.class));
	}
	
	@Test
	public void shouldPerformFirstTriggerBasedOnParameterValue()
	{
		//given
		ControllerAnnotations.setControllerParameter(ctrl, "active", true);
		
		//when
		ctrl.onAdd(ctrlActor);
		ctrl.onUpdate(0.0f, ctrlActor);
		
		//then
		assertEquals("Switch was not triggered", 1, eventCounter.getCount());
		assertEquals("Should be triggered as active", ZootEventType.TriggerOn, ((ZootEvent)eventCounter.getLastEvent()).getType());
	}
	
	@Test
	public void shouldDoNothingWhenEndingCollision()
	{
		//given
		Contact contact = mock(Contact.class);
		ZootActor actorA = mock(ZootActor.class);
		ZootActor actorB = mock(ZootActor.class);
		
		//when
		ctrl.onLeave(actorA, actorB, contact);
		
		//then
		verifyZeroInteractions(actorA, actorB, contact);
	}
	
	@Test
	public void shouldTriggerIfStateWasChanged()
	{
		//given
		ctrl.onAdd(ctrlActor);
		assertFalse(ctrl.isActive());
		
		//when
		ctrl.setActive(true);
		
		//then
		assertEquals("Switch was not triggered", 1, eventCounter.getCount());
		assertEquals("Should be triggered as active", ZootEventType.TriggerOn, ((ZootEvent)eventCounter.getLastEvent()).getType());
		
		//when
		ctrl.setActive(false);
		
		//then
		assertEquals("Switch was not triggered", 2, eventCounter.getCount());
		assertEquals("Should be triggered as not active", ZootEventType.TriggerOff, ((ZootEvent)eventCounter.getLastEvent()).getType());
	}
	
	@Test
	public void shouldNotTriggerIfStateWasNotChanged()
	{
		//given
		ctrl.onAdd(ctrlActor);
		assertFalse(ctrl.isActive());
		
		//when
		ctrl.setActive(false);
		
		//then
		assertEquals("Switch should not be triggered", 0, eventCounter.getCount());
	}
	
	@Test
	public void shouldAlwaysTriggerWhenSwitchingState()
	{
		//given
		ctrl.onAdd(ctrlActor);
		assertFalse(ctrl.isActive());
		
		//when
		boolean active = false;
		for(int i = 1; i < 10; ++i)
		{
			active = !active;			
			ctrl.switchState();
			
			//then
			assertEquals(active, ctrl.isActive());			
			assertEquals("Switch was not triggered", i, eventCounter.getCount());
			assertEquals(active ? ZootEventType.TriggerOn : ZootEventType.TriggerOff, ((ZootEvent)eventCounter.getLastEvent()).getType());
		}
	}
	
	@Test
	public void shouldSwitchStatesEachTimeWhenCollisionHappens()
	{
		//given
		ctrl.onAdd(ctrlActor);
		assertFalse(ctrl.isActive());
		
		//when
		ctrl.onEnter(mock(ZootActor.class), mock(ZootActor.class), mock(Contact.class));
		
		//then
		assertEquals("Switch was not triggered", 1, eventCounter.getCount());
		assertEquals("Should be triggered as active", ZootEventType.TriggerOn, ((ZootEvent)eventCounter.getLastEvent()).getType());
		
		//when
		ctrl.onEnter(mock(ZootActor.class), mock(ZootActor.class), mock(Contact.class));
		
		//then
		assertEquals("Switch was not triggered", 2, eventCounter.getCount());
		assertEquals("Should be triggered as not active", ZootEventType.TriggerOff, ((ZootEvent)eventCounter.getLastEvent()).getType());
	}
	
	@Test
	public void shouldBeSingleton()
	{
		assertTrue(ctrl.isSingleton());
	}
}
