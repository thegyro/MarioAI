
import ch.idsia.agents.LearningAgent;

class MarioQLearningAgent extends ReinfocementAgent implements LearningAgent {
	//Mario State and Action hashed into unique itnegers.
	private DefaultHashMap<Pair<byte[],boolean[]>, float> QValues;
	private MarioState currentState;

	public MarioQLearningAgent(int numTraining=100, float epsilon=0.8, float alpha=0.1, float gamma=0.9) {
		super(numTraining, epsilon, alpha, gamma);
		QValues = new DefaultHashMap<Pair<byte[], boolean[]>, float>(0);
	}

	public float getQValue(MarioState state, MarioAction action) {
		byte[] stateRep = state.getStateRep();
		boolean[] actionRep = action.getActionRep();
		Pair<byte[], boolean[]> stateAction = new Pair<byte[],boolean[]>(stateRep, actionRep);
		return QValues.get(stateAction);
	}

	public float computeValueFromQValue(MarioState state) {
		MarioAction[] actions = state.getLegalActions();		
		float max = 0.0;
		float cur = 0.0;
		for(MarioAction action: actions) {
			cur = this.getQValue(state, action);
			if(cur > max)
				max = cur;
		}

		return max;
	}

	public MarioAction computeActionFromQValue(MarioState state) {
		MarioAction[] actions = state.getLegalActions();
		float max = 0.0;
		float cur = 0.0;
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

	public boolean[] getAction() {
		MarioAction[] actions = currentState.getLegalActions();

		if(Math.random() > this.epsilon) {
			MarioAction bestAction = this.computeActionFromQValue(currentState);
			return bestAction.getActionRep();
		} else {
			MarioAction randomAction = actions[new Random().nextInt(actions.length)];
			return randomAction.getActionRep();
		}
	}

	public void update(MarioAction action, MarioState nextState, float reward) {
		currStateRep = currentState.getStateRep();
		actionRep = action.getActionRep();

		float sampleQValue = reward + gamma*computeValueFromQValue(nextState);
		float newQValue = (1-alpha)*getQValue(currentState, action) + alpha*sampleQValue;
		Pair<byte[],boolean[]> stateAction = new Pair<byte[],boolean[]>(currStateRep, actionRep);
		QValue.put(stateAction, newQValue);
	}


}