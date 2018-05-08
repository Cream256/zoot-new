package com.zootcat.actions;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import com.zootcat.controllers.Controller;
import com.zootcat.scene.ZootActor;

public class ZootActions
{
	private static final int DEFAULT_POOL_SIZE = 20;
	
	static public <T extends ZootAction> T zootAction(Class<T> type) {
		Pool<T> pool = Pools.get(type, DEFAULT_POOL_SIZE);
		T action = pool.obtain();
		action.setPool(pool);
		return action;
	}

	static public ZootKillActorAction killActorAction(ZootActor actor)
	{
		ZootKillActorAction killAction = zootAction(ZootKillActorAction.class);
		killAction.setTarget(actor);
		return killAction;
	}

	public static ZootMoveActorAction moveActorAction(ZootActor actor, float mx, float my)
	{
		ZootMoveActorAction moveAction = zootAction(ZootMoveActorAction.class);
		moveAction.setMovementX(mx);
		moveAction.setMovementY(my);
		moveAction.setTarget(actor);
		return moveAction;
	}
	
	public static ZootAddControllerAction addControllerAction(ZootActor actor, Controller ctrl)
	{
		ZootAddControllerAction addCtrlAction = zootAction(ZootAddControllerAction.class);
		addCtrlAction.setController(ctrl);
		addCtrlAction.setTarget(actor);
		return addCtrlAction;
		
	}
}