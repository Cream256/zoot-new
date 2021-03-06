package com.zootcat.controllers.physics;

import com.zootcat.controllers.ControllerAdapter;
import com.zootcat.controllers.factory.CtrlDebug;
import com.zootcat.exceptions.ZootControllerNotFoundException;
import com.zootcat.fsm.events.ZootEventType;
import com.zootcat.fsm.events.ZootEvents;
import com.zootcat.scene.ZootActor;

public class DetectInAirController extends ControllerAdapter
{
	@CtrlDebug private boolean inAir;	
	private DetectGroundSensorController groundCtrl;	
		
	@Override
	public void init(ZootActor actor)
	{
		inAir = false;
	}

	@Override
	public void onAdd(ZootActor actor)
	{
		groundCtrl = actor.getSingleController(DetectGroundSensorController.class);
		if(groundCtrl == null) throw new ZootControllerNotFoundException(DetectGroundSensorController.class.getSimpleName(), actor.getName());
	}

	@Override
	public void onRemove(ZootActor actor)
	{
		groundCtrl = null;
	}

	@Override
	public void onUpdate(float delta, ZootActor actor)
	{				
		inAir = !groundCtrl.isOnGround(); 
		if(inAir)
		{
			ZootEvents.fireAndFree(actor, ZootEventType.InAir);
		}
	}
	
	@Override
	public boolean isSingleton()
	{
		return true;
	}
	
	public boolean isInAir()
	{
		return inAir;
	}
}
