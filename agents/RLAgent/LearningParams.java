package myagent.agents.RLAgent;

public class LearningParams {
	public final static int NUM_TRAINING = 500;
	public final static boolean DUMP_INTER_QLOGFILES = true;
	public final static String Q_LOG_FILE = "Q_LOG_%d.txt";
	public final static String SCORE_FILE = "scores.txt";
	public final static String LOG_FILE_NAME = "log.txt";

	public final static float NEGATIVE_INFINITY = -999999f;


	public final static float EPSILON = 0.5f;
	public final static float GAMMA  = 0.8f;
	public final static float ALPHA  = 0.3f;
}