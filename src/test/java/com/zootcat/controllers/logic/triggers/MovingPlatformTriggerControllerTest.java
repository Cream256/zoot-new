package com.zootcat.controllers.logic.triggers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.zootcat.controllers.factory.ControllerAnnotations;
import com.zootcat.controllers.logic.MovingPlatformController;
import com.zootcat.scene.ZootActor;
import com.zootcat.testing.ZootActorStub;
import com.zootcat.testing.ZootSceneMock;

public class MovingPlatformTriggerControllerTest
{
	private ZootSceneMock scene;
	private ZootActor platformActor;
	private MovingPlatformTriggerController ctrl;
	
	@Mock private ZootActor ctrlActor;
	@Mock private MovingPlatformController movingPlatformCtrl;
	
	@Before
	public void setup()
	{
		MockitoAnnotations.initMocks(this);
		
		platformActor = new ZootActorStub();
		platformActor.addController(movingPlatformCtrl);
		platformActor.setName("platform");
		
		scene = new ZootSceneMock();
		scene.addActor(platformActor);
		
		ctrl = new MovingPlatformTriggerController();
		ControllerAnnotations.setControllerParameter(ctrl, "scene", scene);
		ControllerAnnotations.setControllerParameter(ctrl, "platformActorName", "platform");
		ctrl.init(ctrlActor);
	}
	
	@Test
	public void shouldEnableMovingWhenTriggeredOn()
	{
		ctrl.triggerOn(ctrlActor);
		verify(movingPlatformCtrl).setMoving(true);
	}
	
	@Test
	public void shouldDisableMovingWhenTriggeredOff()
	{
		ctrl.triggerOff(ctrlActor);
		verify(movingPlatformCtrl).setMoving(false);
	}
	
	@Test
	public void shouldNotMoveWhenActorWithValidNameIsNotFound()
	{
		//given
		ZootActor bunny = new ZootActor();
		bunny.setName("bunny");
		
		//when
		scene.removeActor(platformActor);
		scene.addActor(bunny);
		ctrl.triggerOn(ctrlActor);
		
		//then
		verify(movingPlatformCtrl, times(0)).setMoving(true);		
	}
}
