package myagent.states;
import myagent.states.ShortRangeState;

public class MinimalShortRangeState extends ShortRangeState{
	protected static final int HOW_MANY_ENEMIES = 1; // do you want in the rep
	protected static int rangeLeft = 0,
						rangeRight = 2,
						rangeUp = 1,
						rangeDown= 2;
	protected static int enemyRange = 4;
}