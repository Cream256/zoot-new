package com.zootcat.fsm.states.ground;

import com.zootcat.controllers.gfx.AnimatedSpriteController;
import com.zootcat.controllers.logic.ClimbSensorController;
import com.zootcat.fsm.events.ZootEvent;
import com.zootcat.fsm.events.ZootEventType;
import com.zootcat.fsm.states.BasicState;
import com.zootcat.fsm.states.HurtState;
import com.zootcat.fsm.states.ZootStateUtils;
import com.zootcat.scene.ZootActor;

public class ClimbState extends BasicState
{
	public static final String CLIMB_SIDE_ANIMATION = "ClimbSide";
	public static final String CLIMB_ANIMATION = "Climb";
	
	public static final int ID = ClimbState.class.hashCode();
		
	public ClimbState()
	{
		super("Climb");
	}
	
	@Override
	public void onEnter(ZootActor actor, ZootEvent event)
	{				
		actor.controllersAction(AnimatedSpriteController.class, ctrl -> 
		{
			String usedAnimation = event.getType() == ZootEventType.GrabSide ? CLIMB_SIDE_ANIMATION : CLIMB_ANIMATION; 			
			ctrl.setAnimation(usedAnimation);
		});
		actor.controllersAction(ClimbSensorController.class, ctrl -> ctrl.grab());
	}
		
	@Override	
	public void onLeave(ZootActor actor, ZootEvent event)
	{
		actor.controllersAction(ClimbSensorController.class, ctrl -> ctrl.letGo());
	}
	
	@Override
	public boolean handle(ZootEvent event)
	{
		ZootActor actor = event.getTargetZootActor();
		
		if(event.getType() == ZootEventType.Hurt && ZootStateUtils.canHurtActor(event))
		{
			changeState(event, HurtState.ID);
			return true;
		}
		
		if(event.getType() == ZootEventType.Up)
		{
			actor.controllersAction(ClimbSensorController.class, ctrl -> 
			{
				if(ctrl.climb()) 
					changeState(event, IdleState.ID);
			});
			return true;
		}
		
		if(event.getType() == ZootEventType.Down)
		{			
			actor.controllersAction(ClimbSensorController.class, ctrl -> ctrl.letGo());			
			changeState(event, FallState.ID);
			return true;
		}
		
		return true;
	}
	
	@Override
	public int getId()
	{
		return ID;
	}	
}
