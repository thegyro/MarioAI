/* Java libraries */
import java.util.ArrayList;
import java.util.List;

/* MarioAI benchmark classes */
import ch.idsia.agents.Agent;
import ch.idsia.agents.LearningAgent;
import ch.idsia.benchmark.tasks.LearningTask;
import ch.idsia.tools.EvaluationInfo;
import ch.idsia.benchmark.mario.environments.Environment;


/* Our agent classes */
import myagent.utils.Logger;

import myagent.states.MarioState;
import myagent.states.MarioStateSelector;

import myagent.actions.MarioAction;

import myagent.agents.QLearning;


class MarioQLearningAgent implements LearningAgent {
	
	private MarioState currentState;
	private MarioState lastState;
	private MarioAction lastAction;


	private LearningTask learningTask;

	private int episodesCovered;
	private ArrayList<Float> episodeRewards;
	private QLearning qlearning;
	private ArrayList<Integer> scores;

	private enum Phase {
		INIT,LEARN,EVAL
	}

	private Phase currentPhase;


	private String name = "QLearningAgent";

	public MarioQLearningAgent() {
		currentPhase = Phase.INIT;
		episodesCovered = 0;
		episodeRewards = new ArrayList<Float>();

		Logger.log("-----------Super Mario Agent Created-------------");
	}


	@Override
	public boolean[] getAction() {
		return qlearning.getAction(currentState);
	}


	@Override
	public void integrateObservation(Environment environment) {
		lastState = currentState.copy();
		currentState.updateObservedState(environment);

		if(this.currentPhase == Phase.INIT) {
			Logger.log("Entering the Learning phase");
			this.currentPhase = Phase.LEARN;
		} else if (this.currentPhase == Phase.LEARN) {
			float reward = currentState.getReward();
			episodeRewards[episodesCovered] += reward;
			this.update(lastState, lastAction, currentState, reward);
		}
	}


	public void learnOnce() {
		List<Object> args = new ArrayList<Object>();
		args.add(episodesCovered);
		Logger.log("Learning started. Episode %d", args);

		init();
		learningTask.runSingleEpisode(1);

		EvaluationInfo evaluationInfo = learningTask.getEnvironment.getEvaluationInfo();
		int score = evaluationInfo.computeWeightedFitness();

		scores.add(score);

		if(LearningParams.DUMP_INTER_QLOGFILES)
			qlearning.dumpQValues(LearningParams.Q_LOGFILE, episodesCovered);

		episodesCovered++;
		episodeRewards.add(0f);
	}

	public void learn() {
		for(int i = 0; i < LearningParams.NUM_TRAINING; i++) {
			learnOnce();
		}

		goToEval();
	}

	public void goToEval() {
		Logger.log("---------------Dumping scores---------------");
		dumpScores(LearningParams.SCORE_FILE);

		Logger.log("---------------Entering the evaluation phase--------- ");
		currentPhase = Phase.EVAL;
		Logger.log("---------------Turning epsilon and alpha off---------");
		qlearning.setEpsilon(0);
		qlearning.setAlpha(0);
	}

	public void dumpScores(String logfile) {

	}

	@Override
	public void init() {
		this.currentPhase = Phase.INIT;
		this.episodesCovered = 0;
		episodeRewards.add(0f);

		lastState = null;
		lastAction = null;
	}

	@Override
	public void reset() {
		this.currentState = MarioStateSelector.newStateInstance();
		this.episodesCovered = 0;
		episodeRewards = new ArrayList<Float>();
		lastState = null;
	}

	@Override
	public Agent getBestAgent() {
		return this;
	}

	@Override
	public void setLearningTask(LearningTask learningTask) {
		this.learningTask = learningTask;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {

	}

}