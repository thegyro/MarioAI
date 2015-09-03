class ReinforcementAgent {
	private int episodesSoFar = 0;
	private int numTraining;
	private float accumTrainRewards = 0;
	private float accumTestRewards = 0;

	private float episodeRewards;

	private float epsilon;
	private float alpha;
	private float gamma;

	public ReinforcementAgent(int numTraining=100, float epsilon=0.8, float alpha=0.8, float gamma=0.9) {
		this.numTraining = numTraining;
		this.epsilon = epsilon;
		this.alpha = alpha;
		this.gamma = gama;
	}

	public void update(byte[] currState, boolean[] action, byte[] nextState, float reward) {

	}

	public boolean[] getLegalActions(state) {
		return state.getLegalActions()
	}

	public void observeTransition(currState, action, nextState, reward) {
		this.episodeRewards += reward;
		update(currState, action, nextState, reward);
	}

	public void startEpisode() {
		this.lastState = null;
		this.lastAction = null;
		this.episodeRewards = 0.0;
	}

	public void stopEpisode() {
		if(episodesSoFar < numTraining)
			this.accumTrainRewards += this.episodeRewards;
		else
			this.accumTestRewards += this.episodeRewards;

		this.episodesSoFar += 1;
		if(episodesSoFar >= numTraining) {
			this.epsilon = 0.0;
			this.alpha = 0.0;
		}

	}
}