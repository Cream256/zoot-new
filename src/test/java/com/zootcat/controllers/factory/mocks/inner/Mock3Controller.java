package com.zootcat.controllers.factory.mocks.inner;

import com.zootcat.controllers.Controller;
import com.zootcat.controllers.ControllerPriority;
import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.scene.ZootActor;
import com.zootcat.scene.ZootScene;

public class Mock3Controller implements Controller 
{	
	@CtrlParam public int param;
	@CtrlParam(global=true) public ZootScene scene;

	@Override
	public void onAdd(ZootActor actor) 
	{
		//noop
	}

	@Override
	public void onRemove(ZootActor actor) 
	{
		//noop	
	}

	@Override
	public void onUpdate(float delta, ZootActor actor) 
	{
		//noop
	}

	@Override
	public void init(ZootActor actor) 
	{
		//noop
	}

	@Override
	public ControllerPriority getPriority()
	{
		return ControllerPriority.Normal;
	}

	@Override
	public void setEnabled(boolean value)
	{
		//noop
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public boolean isSingleton()
	{
		return false;
	}
}
