import java.util.ArrayList;

import ch.idsia.benchmark.mario.environments.Environment;
import myagent.agents.MarioState;
public class MarioAction{
	// Define the actions
	public static final int numberOfKeys = Environment.numberOfKeys;
	public static final MarioAction DOWN = new MarioAction(0,Environment.MARIO_KEY_DOWN);
	public static final MarioAction JUMP = new MarioAction(1, Environment.MARIO_KEY_JUMP);
	public static final MarioAction LEFT = new MarioAction(2,Environment.MARIO_KEY_LEFT);
	public static final MarioAction RIGHT = new MarioAction(3, Environment.MARIO_KEY_RIGHT);
	public static final MarioAction SPEED = new MarioAction(4, Environment.MARIO_KEY_SPEED);
	public static final MarioAction SHOOT = new MarioAction(5,Environment.MARIO_KEY_SPEED);	// SHOOT and SPEED are the same key
	
	public int actionCode, keyMapping;
	MarioAction(int actn, int kMapping){
		actionCode = actn;
		keyMapping = kMapping;
	}

	@Override
	public int hashCode(){
		return actionCode;
	}

	public boolean[] getKeys(int action){
		boolean[] keys = new boolean[numberOfKeys];
		keys[keyMapping]=true;
		return keys;
	}

	public MarioAction[] getLegalActions(MarioState state){
		int paSize = 4;
		if( state.canMarioJump() )
			paSize++;

		MarioAction possibleActions[] = new MarioAction[paSize];
		int i=0;
		possibleActions[i++]= LEFT;
		possibleActions[i++]= RIGHT;
		possibleActions[i++]= DOWN;
		if( state.canMarioShoot() )
			possibleActions[i++]= SHOOT;
		else
			possibleActions[i++]= SPEED;
		
		if( state.canMarioJump() )
			possibleActions[i++] = JUMP;

		return possibleActions;
	}
}