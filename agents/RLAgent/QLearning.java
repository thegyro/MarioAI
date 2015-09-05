package myagent.agents.RLAgent;

import myagent.utils.DefaultHashMap;
import myagent.utils.Pair;

import java.util.Random;

import myagent.states.MarioState;
import myagent.actions.MarioAction;

public class QLearning {
	private float alpha;
	private float gamma;
	private float epsilon;
	private int numTraining;

	private MarioAction lastAction;

	private DefaultHashMap<Pair<MarioState,MarioAction>, Float> QValues;
	
	public QLearning() {
		epsilon = LearningParams.EPSILON;
		alpha = LearningParams.ALPHA;
		gamma = LearningParams.GAMMA;

		QValues = new DefaultHashMap<Pair<MarioState,MarioAction>, Float>(0f);

	}

	public float getQValue(MarioState state, MarioAction action) {
		Pair<MarioState,MarioAction> stateAction = new Pair<MarioState,MarioAction>(state,action);
		
		// for(byte b:stateRep)
		// 	System.out.printf("%d",(int)b);
		// System.out.println();
		// System.out.println(stateAction.hashCode());
		
		
		return QValues.get(stateAction);
	}

	public float computeValueFromQValue(MarioState state) {
		MarioAction[] actions = state.getLegalActions();		
		float max = LearningParams.NEGATIVE_INFINITY;
		float cur = 0.0f;
		for(MarioAction action: actions) {
			cur = this.getQValue(state, action);
			if(cur > max)
				max = cur;
		}

		return max;
	}

	public MarioAction computeActionFromQValue(MarioState state) {
		MarioAction[] actions = state.getLegalActions();
		float max = LearningParams.NEGATIVE_INFINITY;
		float cur = 0.0f;
		MarioAction bestAction = null;
		
		for(MarioAction action: actions) {
			cur = this.getQValue(state, action);
			if(cur > max) {
				max = cur;
				bestAction = action;
			}
		}

		return bestAction;

	}

	public void update(MarioState currentState, MarioAction action, MarioState nextState, float reward) {
		if(currentState == null || action == null) {
			return;
		}
		
		float sampleQValue = reward + gamma*computeValueFromQValue(nextState);
		float newQValue = (1-alpha)*getQValue(currentState, action) + alpha*sampleQValue;
		Pair<MarioState, MarioAction> stateAction = new Pair<MarioState, MarioAction>(currentState, action);
		QValues.put(stateAction, newQValue);
	}

	public boolean[] getAction(MarioState currentState) {
		MarioAction[] actions = currentState.getLegalActions();

		if(Math.random() > this.epsilon) {
			MarioAction bestAction = this.computeActionFromQValue(currentState);
			lastAction = bestAction;
			return bestAction.getActionRep();
		} else {
			MarioAction randomAction = actions[new Random().nextInt(actions.length)];
			lastAction = randomAction;
			return randomAction.getActionRep();
		}
	}

	public MarioAction getLastAction() {
		return lastAction;
	}

	public void setLastAction(MarioAction action) {
		lastAction = action;
	}

	public void dumpQValues(String logfile, int num) {

	}

	public void setEpsilon(float epsilon) {
		this.epsilon = epsilon;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}
}