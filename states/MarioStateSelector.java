package myagent.states;

import myagent.states.SimpleState;
import myagent.states.ShortRangeState;
import myagent.states.MinimalShortRangeState;
import myagent.states.BaseState;

public class MarioStateSelector{
	/* Takes a fully qualified module name and returns an instance 
	j/k,You hardcode it here.
	*/
	//private static String filename;
	public static MarioState newStateInstance(){
		//return new SimpleState();
		//return new ShortRangeState();
		//return new MinimalShortRangeState();
		return new BaseState();
	}
}