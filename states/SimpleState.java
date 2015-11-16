package myagent.states;
/**
 if you want to edit anything, It should be encodeMarioState

**/



import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;
import myagent.actions.MarioAction;

import java.lang.Math;
import java.util.Arrays; // For byte[] hashCodes
import java.util.Random;



public class SimpleState implements MarioState{
	
	protected boolean inited; // true if this represents a valid state
	SimpleState(){
		inited = false;
	}

	protected EvaluationInfo evaluationInfo;
	
	/* The params that BasicMarioAIAgent had. Good starting point for our state */
	protected byte[][] levelScene;
	protected byte[][] enemyScene;

	protected float[] marioFloatPos = new float[]{0,0};
	protected float[] enemiesFloatPos = null;

	protected int[] marioState = null;

	protected int currentScore = 0;

	protected int marioStatus;
	protected int marioMode;
	protected boolean isMarioOnGround;
	protected boolean isMarioAbleToJump;
	protected boolean isMarioAbleToShoot;
	protected boolean isMarioCarrying;


	// Observation details
	protected int receptiveFieldWidth;
	protected int receptiveFieldHeight;
	protected int 	marioEgoRow,
					marioEgoCol;
	protected int 	marioEgoX,
					marioEgoY;

	//what level of detail do we want?
	protected int zLevelScene = 2;
	protected int zLevelEnemies = 2;


	// Things we don't use in the state but do in the reward ( Is that even allowed? )
	protected int prevState_kills =0;
	protected int prevState_collisions =0;
	protected float 	prevState_x = 0,
					prevState_y = 0,
					maxEver_x = 0;

	protected int distanceCovered = 0;
	protected int collisions = 0;

	// How many ticks till you're declared stuck?
	protected static final int stuckCriteria = 6;	// 0.5 secs @ 48 fps
	protected int stuckCount = 0;

	// Add something for prevState_action?
	protected float prevState_score = 0;
	protected int totalKills = 0;

	@Override
	public boolean canMarioJump(){
		return isMarioAbleToJump;
	}
	
	@Override
	public boolean canMarioShoot(){
		return isMarioAbleToShoot;
	}

	protected static int didNoAction=0;
	@Override
	public MarioAction[] getLegalActions(){
		
		MarioAction[] possibleActions = new MarioAction[]{
			MarioAction.LEFT,
			MarioAction.RIGHT,
		//	MarioAction.JUMP,
			MarioAction.LEFT_JUMP,
			MarioAction.RIGHT_JUMP,
		//	MarioAction.SPEED,
			MarioAction.LEFT_SHOOT,
			MarioAction.RIGHT_SHOOT,
			MarioAction.RIGHT_JUMP_SHOOT,
			MarioAction.LEFT_JUMP_SHOOT,
		
		};
		//if(true)		return possibleActions;

		if( isStuck() ){
//			System.out.println("STUCK!");

			didNoAction =2;
			stuckCount=0;
		} 
		
		if( didNoAction > 0 ){
			didNoAction--;
			return new MarioAction[]{
				possibleActions[new Random().nextInt(possibleActions.length)]
			};
			
		}

		/*
		static int isStuck = false;
		static MarioAction[] failedActions = new MarioAction[possibleActions.size()];

		if(stuckCount > stuckCriteria){
			failedActions[failedCount++] = prevAction;
		}
		else{
			failedActions.clear();
		}
		for(int p=0;p<possibleActions.size();p++){
			for(int f=0;f<failedCount;f++){

			}
		}
		*/
		return possibleActions;
	}

	@Override
	public void updateObservedState(Environment environment){

	    levelScene = environment.getLevelSceneObservationZ(zLevelScene);
	    enemyScene = environment.getEnemiesObservationZ(zLevelEnemies);
	    
	    if(marioFloatPos[0]>maxEver_x) maxEver_x = marioFloatPos[0];
	    
	    prevState_x = marioFloatPos[0];
		prevState_y = marioFloatPos[1];
		prevState_collisions = collisions;

	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();


		// for(float p:enemiesFloatPos)			System.out.printf("%f, ",p);
		// System.out.printf("Total %d\n",enemiesFloatPos.length);
	    this.marioState = environment.getMarioState();

	    receptiveFieldWidth = environment.getReceptiveFieldWidth();
	    receptiveFieldHeight = environment.getReceptiveFieldHeight();
	    evaluationInfo = environment.getEvaluationInfo();
	    distanceCovered = evaluationInfo.distancePassedCells;
	    collisions = evaluationInfo.collisionsWithCreatures;
	    marioStatus = marioState[0];
		marioMode = marioState[1];
	    isMarioOnGround = marioState[2] == 1;
	    isMarioAbleToJump = marioState[3] == 1;
	    isMarioAbleToShoot = marioState[4] == 1;
	    isMarioCarrying = marioState[5] == 1;
		    
	    //timeLeft = marioState[10];
	    currentScore = environment.getIntermediateReward();

	    totalKills = environment.getKillsByStomp();

	    inited=true;

	    // Check if stuck:
	    //if( Math.abs(prevState_x - marioFloatPos[0]) < 3 )
	    if( Math.abs(maxEver_x - marioFloatPos[0]) < 2 )
	    	stuckCount++;
	    else
	    	stuckCount=0;
	    int[] marioEgoPos = environment.getMarioEgoPos();
	    marioEgoX = marioEgoPos[0];
	    marioEgoY = marioEgoPos[1];
	}

	@Override
	public void updateObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol){
	    receptiveFieldWidth = rfWidth;
	    receptiveFieldHeight = rfHeight;

	    marioEgoRow = egoRow;
	    marioEgoCol = egoCol;	// useless
	 
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(getStateRep());
	}

	@Override
	public boolean equals(Object marioState){
		return Arrays.equals(getStateRep(), ((MarioState)marioState).getStateRep());
	}

	@Override
	public byte[] getStateRep(){
		/**
		 Returns a minimal encoding of the state 
		 **/
		 byte[] marioInfo = encodeMarioState();
		 byte[] levelInfo = encodeLevelScene(levelScene);
		 byte[] stateRep = new byte[marioInfo.length+levelInfo.length];
		 int limit = 0;
		 int base=0;
		 for(int i=0;i<marioInfo.length;i++)
		 	stateRep[base+i]=0;
		 for(int i=0;i<levelInfo.length;i++)
		 	stateRep[base+i]=levelInfo[i];

		 return stateRep;
	}

	

	protected byte[] encodeMarioState(){
		/**
			Encodes any relevant info we have about mario in a byte[]
		**/
		return new byte[]{
			(byte)Math.round(marioFloatPos[0]/5),
			(byte)(marioEgoY/5),
			(byte)((canMarioShoot())?1:0)	// Else shooting makes no sense
		}; // This is needed else he gets stuck when the frame is stati
		
		
	}

	protected byte[] encodeLevelScene(byte[][] ls){
		/**
			Returns a byte[] from the levelScene and enemyScene information.
			Provides information on what the block holds.
			As of now:
				2 bits are used to represent each block.
					X0=> Air,
					X1=> Solid,
					1X=> Monster
				Hence 4 blocks are encoded in a single byte.
		**/
		if(ls == null || ls.length==0)	// Just incase
			return new byte[0];

		int bitsUsed = 2;
		int repLength = ((ls.length*ls[0].length) * bitsUsed); //How many bits
		
		if(repLength%8!=0)
			repLength+= 8-(repLength%8);	// Incase we don't fit perfectly.
		
		repLength/=8;	// How many bytes do we need?
		byte[] rep = new byte[repLength];
		int k=-1, shiftBy=0;
		byte unitCode;	// X0=> Air, X1=> Solid, 0=>No Monster, 1X=> Monster

		for(int i=0;i<ls.length;i++){
			for(int j=0;j<ls[i].length;j++){
				if(shiftBy==0){
					k++;
					rep[k]=0;
				}
				unitCode=0;
				if(levelScene[i][j]==1)
					unitCode|=1;
				if(enemyScene[i][j]==1)
					unitCode|=3;
				unitCode <<= shiftBy;
				rep[k] |= unitCode;
				shiftBy = (shiftBy+2)%8;
			}
		}
		return rep;
	}

	/** 
		Members to calculate the immediate reward
	**/

	protected boolean wasStuckInLastTurn = false;
	@Override
	public float getReward(){
		/* 
			10 for a kill.
			a little bit for going in the right direction
		*/
		float livingReward = -1f;
		float reward = livingReward;
		reward += 10 * (totalKills - prevState_kills);	// Can't distinguish b/w collision and kill
		
		
		if( collisions > prevState_collisions && totalKills==prevState_kills){
			//System.out.print("ouch!\t");
			reward-=20f;
		}
		prevState_kills = totalKills;
		// Try something new.
		reward +=  (marioFloatPos[0] - prevState_x); // without the /5, the reward is too large
		
		prevState_score = currentScore;
		
		// Being stuck is bad

		if( wasStuckInLastTurn && !isStuck() )
			reward+= 20f; // Reward unstuck
		wasStuckInLastTurn = isStuck();
	
		return reward;
	}

	@Override
	public MarioState copy(){
		SimpleState copied = new SimpleState();
		
		if(!inited)
			return copied;
		
		copied.levelScene = new byte[levelScene.length][levelScene[0].length];
		int i=0;
		int j=0;
		for(byte[] l:levelScene){
			j=0;
			for(byte b:l){
				copied.levelScene[i][j++] = b;
			}
			i++;
		}

		copied.enemyScene = new byte[enemyScene.length][enemyScene[0].length];
		i=0;
		j=0;
		for(byte[] e:enemyScene){
			j=0;
			for(byte b:e){
				copied.enemyScene[i][j++] = b;
			}
			i++;
		}

		copied.marioFloatPos = marioFloatPos;
		copied.enemiesFloatPos = enemiesFloatPos;

		i=0;
		copied.marioState = new int[marioState.length];
		for(int ms:marioState)
			copied.marioState[i++]=ms;


		copied.marioStatus = marioStatus;
		copied.marioMode = marioMode;
		copied.isMarioOnGround = isMarioOnGround;
		copied.isMarioAbleToJump = isMarioAbleToJump;
		copied.isMarioAbleToShoot = isMarioAbleToShoot;
		copied.isMarioCarrying = isMarioCarrying;


		// Observation details
		copied.receptiveFieldWidth = receptiveFieldWidth;
		copied.receptiveFieldHeight = receptiveFieldHeight;
		copied.marioEgoRow = marioEgoRow;
		copied.marioEgoCol = marioEgoCol;

		copied.marioEgoX = marioEgoX;
		copied.marioEgoY = marioEgoY;
		// Things we don't use in the state but do in the reward ( Is that even allowed? )
		copied.prevState_kills = prevState_kills;
		copied.prevState_x = prevState_x;
		copied.maxEver_x = maxEver_x;
		copied.totalKills = totalKills;

		copied.inited=true;

		return copied;
	}
	
	@Override
	public boolean isStuck(){
		return (stuckCount > stuckCriteria);
	}

	@Override
	public void resetStuck(){
		stuckCount=0;
	}

	@Override
	public EvaluationInfo getEvaluationInfo(){
		return evaluationInfo;
	}

	protected int[] computeMarioRelXY(){
		/** Used to compute the limited field of vision**/
		int x = (int)Math.floor(marioFloatPos[0]/16),
			y = (int)Math.floor(marioFloatPos[1]/16);
		if(x<10)
			x=10;
		return new int[]{x,y};
		
	}

}

