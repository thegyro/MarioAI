package myagent.states;

import ch.idsia.benchmark.mario.environments.Environment;
import myagent.actions.MarioAction;

public interface MarioState{	

	public byte[] getStateRep();
	public MarioAction[] getLegalActions();
	public float getReward();


	public int hashCode();
	public boolean equals(Object marioState);	// Has to be of type object to override the java equals
	public MarioState copy();

	public void updateObservedState(Environment environment);
	public void updateObservationDetails(
		final int rfWidth, final int rfHeight, final int egoRow, final int egoCol
	);

	
	public boolean canMarioJump();
	public boolean canMarioShoot();
	

}