package com.zootcat.controllers.logic;

import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.scene.ZootActor;

public class CollectForLifeController extends CollectOnCollideSensorController
{
	@CtrlParam private int life = 1;
	@CtrlParam private int maxLife = 0;
	
	public boolean onCollect(ZootActor collectible, ZootActor collector)
	{
		collector.controllersAction(LifeController.class, ctrl ->
		{
			ctrl.addToMaxValue(maxLife);
			ctrl.addToValue(life);
		});
		return true;
	}
}
