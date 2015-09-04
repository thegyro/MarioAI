package myagent.states;

import ch.idsia.benchmark.mario.environments.Environment;
import myagent.actions.MarioAction;

public interface MarioState{	
	public byte[] getStateRepresentation();
	public void updateObservedState(Environment environment);
	public void updateObservationDetails(
		final int rfWidth, final int rfHeight, final int egoRow, final int egoCol
	);

	public boolean canMarioJump();
	public boolean canMarioShoot();
	public MarioAction[] getLegalActions();
	public float getReward();
}