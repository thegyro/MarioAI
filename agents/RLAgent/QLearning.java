public class QLearning {
	private float alpha;
	private float gamma;
	private float epsilon;
	private int numTraining;

	private DefaultHashMap<Pair<byte[],boolean[]>, float> QValues;
	
	public QLearning() {
		epsilon = LearningPrams.EPSILON;
		alpha = LearningPrams.ALPHA;
		gamma = LearningParams.GAMMA;

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

	public void update(MarioState currentState, MarioAction action, MarioState nextState, float reward) {
		if(currentState == null || action == null) {
			return;
		}
		currStateRep = currentState.getStateRep();
		actionRep = action.getActionRep();

		float sampleQValue = reward + gamma*computeValueFromQValue(nextState);
		float newQValue = (1-alpha)*getQValue(currentState, action) + alpha*sampleQValue;
		Pair<byte[],boolean[]> stateAction = new Pair<byte[],boolean[]>(currStateRep, actionRep);
		QValue.put(stateAction, newQValue);
	}

	public boolean[] getAction(MarioState currentState) {
		MarioAction[] actions = currentState.getLegalActions();

		if(Math.random() > this.epsilon) {
			MarioAction bestAction = this.computeActionFromQValue(currentState);
			lastAction = bestAction;
			return bestAction.getActionRep();
		} else {
			MarioAction randomAction = actions[new Random().nextInt(actions.length)];
			lastAction = bestAction;
			return randomAction.getActionRep();
		}
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