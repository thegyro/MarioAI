package myagent.agents;

import ch.idsia.benchmark.mario.environments.Environment;

public interface MarioState{	
	public byte[] getStateRepresentation();
	public void updateObservedState(Environment environment);
	public void updateObservationDetails(
		final int rfWidth, final int rfHeight, final int egoRow, final int egoCol
	);

	public boolean canMarioJump();
	public boolean canMarioShoot();
}