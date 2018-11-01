package com.zootcat.controllers;

import com.zootcat.scene.ZootActor;

public class ControllerAdapter implements Controller 
{
	private boolean enabled = true;
	
	@Override
	public void init(ZootActor actor) 
	{
		//noop
	}

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
	public boolean isEnabled()
	{
		return enabled;
	}
	
	@Override
	public void setEnabled(boolean value)
	{
		enabled = value;
	}
}
