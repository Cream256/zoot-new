package com.zootcat.controllers.physics;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.physics.ZootDefaultContactFilter;
import com.zootcat.scene.ZootActor;
import com.zootcat.utils.BitMaskConverter;

/**
 * OnCollide Controller - abstract class used to do some action when 
 * collision begins and ends.<br/> 
 * <br/>
 * OnEnter and OnLeave will be executed by default per fixture. It can be 
 * changed by setting the 'collidePerActor' parameter, then the collision
 * will count once per body, even if body has several fixtures.<br/>
 *  * 
 * @override onEnter - will be executed when collision begins<br/>
 * @override onLeave - will be executed when collision ends<br/>
 * 
 * @ctrlParam category - category name for collision detection,
 * if nothing is given a default value will be used
 * 
 * @ctrlParam mask - categories for which the collision will take place, separated with "|". 
 * If nothing is given, mask that collides with everything will be used.
 * 
 * @ctrlParam collideWithSensors - if sensors should count to collision, default true 
 * 
 * @ctrlParam collidePerActor - if collision per actor rather than fixture should be counted
 * 
 * @author Cream
 */
public abstract class OnCollideController extends PhysicsCollisionController
{
	@CtrlParam(debug = true) private String category = null;
	@CtrlParam(debug = true) private String mask = null;
	@CtrlParam(debug = true) private boolean collideWithSensors = true;
	@CtrlParam(debug = true) private boolean collidePerActor = false;
	
	private Filter filter;
	private Set<ZootActor> collidingActors = new HashSet<ZootActor>();
		
	public OnCollideController()
	{
		//noop
	}
	
	public OnCollideController(boolean collidePerActor)
	{
		this.collidePerActor = collidePerActor;
	}
	
	public void setCategory(String category)
	{
		this.category = category;
	}
	
	public void setMask(String mask)
	{
		this.mask = mask;
	}
	
	public void setFilter(Filter filter)
	{
		this.filter = filter;
	}
	
	public Filter getFilter()
	{
		return filter;
	}
	
	@Override
	public void init(ZootActor actor)
	{		
		super.init(actor);
		
		collidingActors.clear();		
		createCollisionFilter(actor);
	}

	private void createCollisionFilter(ZootActor actor)
	{
		filter = new Filter();
		
		if(mask != null || category != null)
		{
			filter.maskBits = BitMaskConverter.Instance.fromString(mask);
			if(category != null && !category.isEmpty())
			{
				filter.categoryBits = BitMaskConverter.Instance.fromString(category);
			}
		}
	}
		
	@Override
	public void beginContact(ZootActor actorA, ZootActor actorB, Contact contact)
	{				
		ZootActor otherActor = getOtherActor(actorA, actorB);
		if(collides(actorA, actorB, contact) && beginCollisionCounts(otherActor))
		{
			collidingActors.add(otherActor);
			onEnter(actorA, actorB, contact);
		}
	}

	private boolean beginCollisionCounts(ZootActor otherActor)
	{
		return !collidePerActor || !collidingActors.contains(otherActor);
	}
	
	@Override
	public void endContact(ZootActor actorA, ZootActor actorB, Contact contact)
	{
		ZootActor otherActor = getOtherActor(actorA, actorB);
		if(collides(actorA, actorB, contact) && endCollisionCounts(otherActor))
		{			
			onLeave(actorA, actorB, contact);
			collidingActors.remove(otherActor);
		}
	}
	
	private boolean endCollisionCounts(ZootActor otherActor)
	{
		return !collidePerActor || collidingActors.contains(otherActor);
	}
	
	@Override
	public void preSolve(ZootActor actorA, ZootActor actorB, Contact contact, Manifold manifold)
	{
		//noop
	}
	
	@Override
	public void postSolve(ZootActor actorA, ZootActor actorB, ContactImpulse contactImpulse)
	{
		//noop
	}
				
	public abstract void onEnter(ZootActor actorA, ZootActor actorB, Contact contact);
	
	public abstract void onLeave(ZootActor actorA, ZootActor actorB, Contact contact);
	
	protected ZootActor getOtherActor(ZootActor actorA, ZootActor actorB)
	{
		return actorA == getControllerActor() ? actorB : actorA;
	}
	
	protected Fixture getOtherFixture(ZootActor actorA, ZootActor actorB, Contact contact)
	{
		return (actorA == getControllerActor()) ? contact.getFixtureB() : contact.getFixtureA();
	}
	
	protected Fixture getControllerActorFixture(ZootActor actorA, ZootActor actorB, Contact contact)
	{
		return (actorA == getControllerActor()) ? contact.getFixtureA() : contact.getFixtureB();
	}
		
	private boolean collides(ZootActor actorA, ZootActor actorB, Contact contact)
	{				
		Fixture otherFixture = getOtherFixture(actorA, actorB, contact);
		
		boolean collisionDetected = ZootDefaultContactFilter.shouldCollide(filter, otherFixture.getFilterData());
		boolean sensorOk = collideWithSensors || !otherFixture.isSensor();
		return collisionDetected && sensorOk;
	}
}