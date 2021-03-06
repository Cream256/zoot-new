package com.zootcat.controllers.ai;

import com.zootcat.controllers.factory.CtrlParam;
import com.zootcat.controllers.physics.PhysicsBodyController;
import com.zootcat.fsm.ZootStateMachine;
import com.zootcat.fsm.states.ForwardingState;
import com.zootcat.fsm.states.PatrolState;
import com.zootcat.fsm.states.flying.FlyIdleState;
import com.zootcat.fsm.states.flying.FlyPatrolState;
import com.zootcat.scene.ZootActor;

public class PatrolStateMachineController extends DefaultStateMachineController
{
	@CtrlParam(required = true) private int patrolRange;
	@CtrlParam private boolean flying = false;
	
	@Override
	public void onAdd(ZootActor actor)
	{
		PhysicsBodyController physicsCtrl = actor.getSingleController(PhysicsBodyController.class);
		
		PatrolState patrolState = flying ? new FlyPatrolState() : new PatrolState();
		patrolState.setPatrolRange(patrolRange);
		patrolState.setStartX(physicsCtrl.getCenterPositionRef().x);
		
		ZootStateMachine sm = actor.getStateMachine();		
		if(flying)
		{
			sm.addState(new FlyIdleState());			
		}
		
		sm.addState(patrolState);
		sm.init(new ForwardingState(FlyIdleState.ID, patrolState.getId()));		
	}
}
