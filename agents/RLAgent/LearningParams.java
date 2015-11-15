package myagent.agents.RLAgent;

public class LearningParams {
	public final static int NUM_TRAINING = 300;
	public final static boolean DUMP_INTER_QLOGFILES = true;
	public final static String Q_LOG_FILE = "Q_LOG_%d.txt";
	public final static String SCORE_FILE = "scores.txt";
	public final static String LOG_FILE_NAME = "log.txt";

	public final static float NEGATIVE_INFINITY = -999999f;


	public final static float EPSILON = 0.3f;
	public final static float GAMMA  = 0.9f;
	public final static float ALPHA  = 0.3f;

	public final static int variableExplorationNumerator = 10;
	public final static int variableExplorationDenominator = 30;
}