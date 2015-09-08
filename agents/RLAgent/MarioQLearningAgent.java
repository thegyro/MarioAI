package myagent.agents.RLAgent;

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

import myagent.agents.RLAgent.QLearning;
import myagent.agents.RLAgent.LearningParams;


public class MarioQLearningAgent implements LearningAgent {
	
	private MarioState currentState;
	private MarioState lastState;

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
		scores = new ArrayList<Integer>();
		currentState = MarioStateSelector.newStateInstance();
		qlearning = new QLearning();	
		Logger.log("-----------Super Mario Agent Created-------------\n");
	}


	@Override
	public boolean[] getAction() {
		return qlearning.getAction(currentState, (currentPhase==Phase.EVAL));
	}


	private float evalReward;
	@Override
	public void integrateObservation(Environment environment) {
		lastState = currentState.copy();
		currentState.updateObservedState(environment);

		if(this.currentPhase == Phase.INIT) {
			Logger.log("-------------Entering the Learning phase--------------");
			this.currentPhase = Phase.LEARN;
		} else if (this.currentPhase == Phase.LEARN) {	// Let's learn the rewards in eval as well.
			float reward = currentState.getReward();
			
			float sofar = episodeRewards.get(episodesCovered) + reward;
			episodeRewards.set(episodesCovered, sofar);
			qlearning.update(lastState, qlearning.getLastAction(), currentState, reward);
		}
		 else if( currentPhase == Phase.EVAL ){	// Anti stuck
		 	float initialValue = qlearning.getQValue(lastState,qlearning.getLastAction());
		 	if(currentState.isStuck())
		 		qlearning.update(lastState, qlearning.getLastAction(), currentState, initialValue/2);
		}
	}

	@Override
	public void setObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol){
		currentState.updateObservationDetails(rfWidth, rfHeight, egoRow, egoCol);
	}

	public void learnOnce() {
		List<Object> args = new ArrayList<Object>();
		args.add(episodesCovered);
		Logger.log("Learning started. Episode %d", args);
		System.out.println("Episode " + episodesCovered);
		init();
		episodeRewards.add(0f);
		System.out.println("Done. QTable size is: "+ qlearning.QValues.size());

		learningTask.runSingleEpisode(1);
		EvaluationInfo evaluationInfo = learningTask.getEnvironment().getEvaluationInfo();
		int score = evaluationInfo.computeWeightedFitness();
		System.out.println("Done. maxX is: "+ evaluationInfo.computeDistancePassed() );
		scores.add(score);

		if(LearningParams.DUMP_INTER_QLOGFILES)
			qlearning.dumpQValues(LearningParams.Q_LOG_FILE, episodesCovered);

		episodesCovered++;
		
	}

	public void learn() {
		for(int i = 0; i < LearningParams.NUM_TRAINING; i++) {
			learnOnce();
		}
		System.out.printf("LEARNT %d times\n", LearningParams.NUM_TRAINING);
		goToEval();
	}

	public void goToEval() {
		Logger.log("---------------Dumping scores---------------");
		Logger.dumpScores(scores);

		Logger.log("---------------Entering the evaluation phase--------- ");
		currentPhase = Phase.EVAL;
		Logger.log("---------------Turning epsilon and alpha off---------");
		qlearning.setEpsilon(0);
		qlearning.setAlpha(0);
	}


	@Override
	public void init() {
		this.currentPhase = Phase.INIT;
		
		lastState = null;
		qlearning.setLastAction(null);
	}

	@Override
	public void reset() {
		this.currentState = MarioStateSelector.newStateInstance();
		lastState = null;
		qlearning.setLastAction(null);
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
		return this.name + "("+currentState.getClass().getSimpleName()+")";
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void giveIntermediateReward(float intermediateReward) {

	}

	/* These need to be implemented */
	@Override
	public void setEvaluationQuota(long evalQuota){
		
	}

	@Override
	public void newEpisode(){

	}
	@Override
	public void giveReward(float reward){
		System.out.println("Got reward!");
	}

}