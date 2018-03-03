package com.zootcat.controllers.logic;

import com.badlogic.gdx.physics.box2d.Contact;
import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.controllers.physics.OnCollideController;
import com.zootcat.events.ZootEventType;
import com.zootcat.events.ZootEvents;
import com.zootcat.scene.ZootActor;

/**
 * Switch Controller - used for actors that are switches. When collision
 * happens, the switch changes it's state and fires the SwitchOn/SwitchOff
 * {@link ZootEvent}.
 * <br/><br/>
 * If you want to react when the switch changes it's state, you should
 * listen to SwitchOn/SwitchOff events. This can be done by implementing
 * the {@link SwitchEventListener} class.
 * <br/><br/>
 * WARNING - when using switch controller, the switch will automatically
 * fire SwitchOn/SwitchOff event on initialization. It might override
 * the default behaviour set for the object connected with the switch.
 * <br/><br/>
 * @author Cream
 * @see OnCollideController
 */
public class SwitchController extends OnCollideController
{
	@CtrlParam(debug = true) private boolean active = false;
		
	private boolean firstTriggerDone;
	private boolean firstTriggerState;
	
	@Override
	public void onAdd(ZootActor actor) 
	{
		super.onAdd(actor);
		firstTriggerDone = false;
		firstTriggerState = active;
	}
	
	@Override
	public void onUpdate(float delta, ZootActor actor)
	{
		super.onUpdate(delta, actor);
		if(!firstTriggerDone)
		{
			trigger(firstTriggerState);
			firstTriggerDone = true;
		}
	}
	
	@Override
	public void onEnter(ZootActor actorA, ZootActor actorB, Contact contact)
	{
		switchState();
	}

	@Override
	public void onLeave(ZootActor actorA, ZootActor actorB, Contact contact)
	{
		//noop
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void setActive(boolean isActive)
	{
		if(active != isActive) trigger(isActive);		
		active = isActive;		
	}
	
	public void switchState()
	{
		setActive(!active);
	}
	
	private void trigger(boolean active)
	{
		ZootEvents.fireAndFree(getControllerActor(), active ? ZootEventType.SwitchOn : ZootEventType.SwitchOff, getControllerActor());
	}
}