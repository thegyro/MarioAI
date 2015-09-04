package myagent.actions;

import java.util.ArrayList;

import ch.idsia.benchmark.mario.environments.Environment;
import myagent.states.MarioState;
public class MarioAction{
	// Define the actions
	public static final int numberOfKeys = Environment.numberOfKeys;
	public static final MarioAction 
		LEFT = new MarioAction(1, new int[]{Environment.MARIO_KEY_LEFT}),
		RIGHT = new MarioAction(2, new int[]{Environment.MARIO_KEY_RIGHT}),
		DOWN = new MarioAction(4,new int[]{Environment.MARIO_KEY_DOWN}),
		JUMP = new MarioAction(8, new int[]{Environment.MARIO_KEY_JUMP}),
		
		LEFT_JUMP = new MarioAction(9, new int[]{Environment.MARIO_KEY_LEFT, Environment.MARIO_KEY_JUMP}),
		RIGHT_JUMP = new MarioAction(10, new int[]{Environment.MARIO_KEY_RIGHT, Environment.MARIO_KEY_JUMP}),
		
		SPEED = new MarioAction(16, new int[]{Environment.MARIO_KEY_SPEED}),
		//SHOOT = new MarioAction(16, new int[]{Environment.MARIO_KEY_SPEED}),	// SHOOT and SPEED are the same key
		LEFT_SHOOT = new MarioAction(17, new int[]{Environment.MARIO_KEY_RIGHT, Environment.MARIO_KEY_SPEED}),
		RIGHT_SHOOT = new MarioAction(18, new int[]{Environment.MARIO_KEY_RIGHT, Environment.MARIO_KEY_SPEED}),
		RIGHT_JUMP_SHOOT = 	new MarioAction(26, new int[]{Environment.MARIO_KEY_RIGHT, Environment.MARIO_KEY_JUMP, Environment.MARIO_KEY_SPEED }),
		LEFT_JUMP_SHOOT = 	new MarioAction(25, new int[]{Environment.MARIO_KEY_LEFT, Environment.MARIO_KEY_JUMP, Environment.MARIO_KEY_SPEED });
		

	
	private int actionCode;
	private int[] keyMapping;
	MarioAction(int actn, int[] kMapping){
		actionCode = actn;
		keyMapping = kMapping;
	}

	@Override
	public int hashCode(){
		return actionCode;
	}

	@Override
	public boolean equals(Object ma){
		return (this.actionCode==((MarioAction)ma).actionCode);
	}

	public boolean[] getActionRep(int action){
		/** Returns the boolean array of keys that the benchmark uses **/
		boolean[] keys = new boolean[numberOfKeys];
		for( int km : keyMapping)
			keys[km] = true;
		return keys;
	}

}