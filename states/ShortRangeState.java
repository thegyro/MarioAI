package myagent.states;


import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;
import myagent.actions.MarioAction;

import java.lang.Math;
import java.util.Arrays; // For byte[] hashCodes
import java.util.Random;



public class ShortRangeState implements MarioState{
	private static float HALF_CELL_X = 2.5f;
	private static float ENEMY_HALF_CELL_X = 11f;
	private static float  spaceResolution=5f; // One whole cell?
	private static int enemyRange = 8;
	private static int 	rangeLeft = 2,
						rangeRight = 3,
						rangeUp = 4,
						rangeDown= 2;
	private boolean inited; // true if this represents a valid state
	ShortRangeState(){
		inited = false;
	}
	private EvaluationInfo evaluationInfo;
	/* The params that BasicMarioAIAgent had. Good starting point for our state */
	protected byte[][] levelScene;
	protected byte[][] enemyScene;

	protected float[] marioFloatPos = new float[]{0,0};//Gives relative location of enemies
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
	private int zLevelScene = 2;
	private int zLevelEnemies = 2;


	// Things we don't use in the state but do in the reward ( Is that even allowed? )
	private int prevState_kills =0;
	private int prevState_collisions =0;
	private float 	prevState_x = 0,
					prevState_y = 0,
					maxEver_x = 0;

	private int distanceCovered = 0;
	private int collisions = 0;

	// How many ticks till you're declared stuck?
	private static final int stuckCriteria = 24;	//  sec @ 24 fps
	private int stuckCount = 0;

	// Add something for prevState_action?
	private float prevState_score = 0;
	private int totalKills = 0;

	@Override
	public boolean canMarioJump(){
		return isMarioAbleToJump;
	}
	
	@Override
	public boolean canMarioShoot(){
		return isMarioAbleToShoot;
	}

	private static int didNoAction=0;
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
		 // Hack for random movement to get unstuck
		if( stuckCount > stuckCriteria ){
			didNoAction = 5;
			stuckCount = 0;
		} 
		
		if( didNoAction > 0 ){
			didNoAction--;
			return new MarioAction[]{
				possibleActions[new Random().nextInt(possibleActions.length)]
			};
			//System.out.println("Meh");
			
			//failedActions[failedCount++] = prevAction;
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
	    
	    prevState_x = marioFloatPos[0];
		prevState_y = marioFloatPos[1];
		prevState_collisions = collisions;

	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();

	    if(marioFloatPos[0] > maxEver_x ) maxEver_x = marioFloatPos[0];
	 //    for(float p:enemiesFloatPos)			System.out.printf("%f, ",p);
		// System.out.printf("Total %d. Mario X is: (%f,%f)\n",enemiesFloatPos.length, marioFloatPos[0],marioFloatPos[1]);

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
	    
	    if( Math.abs(maxEver_x - marioFloatPos[0]) < 3 )
	    //if( Math.abs(prevState_x - marioFloatPos[0]) < 3 )
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
	    marioEgoCol = egoCol;
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
		 byte[] enemyInfo = encodeEnemiesScene();
		 byte[] stateRep = new byte[marioInfo.length+levelInfo.length+enemyInfo.length];
		 int limit = 0;
		 int base=0;
		for(int i=0;i<marioInfo.length;i++)
		 	stateRep[base+i]= marioInfo[i];
		 base += marioInfo.length;
		
		for(int i=0;i<levelInfo.length;i++)
		 	stateRep[base+i]=levelInfo[i];
		 base += levelInfo.length;

		for(int i=0;i<enemyInfo.length;i++){
		 	//System.out.println(enemyInfo[i]);
		 	stateRep[base+i]=enemyInfo[i];
		 }
		 base += enemyInfo.length;
		 return stateRep;
	}

	

	private byte[] encodeMarioState(){
		/**
			Encodes any relevant info we have about mario in a byte[]
		**/
		//return new byte[]{};
		//return new byte[]{(byte)marioMode}; // Mario becomes more careful when he's dying.
		// return new byte[]{(byte)((canMarioShoot())?1:0)}; // Mario shouldn't try to shoot if he can't

		return new byte[]{
			  (byte)(Math.round(marioFloatPos[0])%5),
			 // (byte)Math.round(marioFloatPos[1]/spaceResolution),
			  //(byte)((canMarioShoot())?1:0),
			  (byte)marioMode
		}; // This is needed else he gets stuck when the frame is stati
		
		
	}
	
	private int[] computeMarioRelXY(){
		/** Used to compute the limited field of vision**/
		int x = (int)Math.floor(marioFloatPos[0]/16),
			y = (int)Math.floor(marioFloatPos[1]/16);
		if(x<10)
			x=10;
		return new int[]{x,y};
		
	}

	private byte[] encodeLevelScene(byte[][] ls){
		/**
			Returns a byte[] from the levelScene and enemyScene information.
			Provides information on what the block holds.
			As of now:
				1 bits are used to represent each block.
					0=> Air,
					1=> Solid,
				Hence 8 blocks are encoded in a single byte.
		**/
		if(ls == null || ls.length==0)	// Just incase
			return new byte[0];

		int bitsUsed = 1;
		byte unitCode;	// 0=> Air, 1=> Solid
		int[] marioRelXY = computeMarioRelXY();
		int startI = marioRelXY[0]- rangeLeft,
			startJ = marioRelXY[1]- rangeDown,
			endI = marioRelXY[0] + rangeRight,
			endJ = marioRelXY[1] + rangeUp;
		
		int repLength = ((endI-startI+1)*(endJ-startJ+1) * bitsUsed); //How many bits
		
		if(repLength%8!=0)
			repLength+= 8-(repLength%8);	// Incase we don't fit perfectly.
		
		repLength/=8;	// How many bytes do we need?
		byte[] rep = new byte[repLength];
		int k=-1, shiftBy=0;


		for(int i=startI;i<=endI;i++){
			for(int j=startJ;j<=endJ;j++){
				if(shiftBy==0){
					k++;
					rep[k]=0;
				}
				if( i<0 || j<0 || i>=ls.length || j>=ls[i].length )
					unitCode=0;
				else
					unitCode = levelScene[i][j];;

				unitCode <<= shiftBy;
				rep[k] |= unitCode;
				shiftBy = (shiftBy+bitsUsed)%8;
			}
		}

		return rep;
	}

	private byte[] encodeEnemiesScene(){
		// Pick the closest 2
		byte rep[] = new byte[]{(byte)127,(byte)127,(byte)127,(byte)127};;
		
		if(enemiesFloatPos==null)
			return rep;
		int enemiesInRange = enemiesFloatPos.length / 3;
		
		int x,y;
		
		for(int i=0;i<enemiesInRange;i++){
			x= Math.round(enemiesFloatPos[3*i+1] / ENEMY_HALF_CELL_X );
			y= Math.round(enemiesFloatPos[3*i+2] / (2*ENEMY_HALF_CELL_X) );
			//System.out.printf("e(%d), ", x);
			if( Math.abs(x) > enemyRange )
				continue;
			if( Math.abs(x) < Math.abs(rep[0]) ){
				rep[2] = rep[0];//byte[1].x = byte[0].x;	//change this
				rep[3] = rep[1];
				rep[0] = (byte)x;
				rep[1] = (byte)y;
			}
			else if( Math.abs(x) < Math.abs(rep[2]) ){
				rep[2] = (byte)x;//byte[1].x = byte[0].x;	//change this
				rep[3] = (byte)y;
			
			}
		}
		//if(rep[0]!=127)			System.out.println(rep[0]+" "+rep[1]+" "+rep[2]+" "+rep[3]);
		return new byte[]{rep[0],rep[1],rep[2],rep[3]};
	}

	/** 
		Members to calculate the immediate reward
	**/
	public float closestEnemyX(){
		int enemiesInRange = enemiesFloatPos.length / 3;
			
			float x = 0f,closestX = 1000f;
			for(int i=0;i<enemiesInRange;i++){
				x= Math.abs(enemiesFloatPos[3*i+1]);
				if( x < closestX )
					closestX = x;
			}
			return closestX;
	}
	
	protected boolean wasStuckInLastTurn = false;
	@Override
	public float getReward(){
		
		float livingReward = -1f;
		float reward = livingReward;
		reward +=  (marioFloatPos[0] - prevState_x); // /timeLeft;

		reward -= 100f *  (collisions - prevState_collisions);
		
		 if( closestEnemyX() < 2*ENEMY_HALF_CELL_X ) reward -= 50f; // this makess a huge difference
			
		
		//if(isStuck())			reward -= 20f;
		

		if( wasStuckInLastTurn && !isStuck() )			reward+= 20f; // Reward unstuck
		wasStuckInLastTurn = isStuck();
		/*
		//reward += 5 * (totalKills - prevState_kills);	// Can't distinguish b/w collision and kill
		
		prevState_kills = totalKills;
		//reward -= 10 *  (collisions - prevState_collisions);
		// Try something new.
		
		// Being stuck is bad
		
		if(stuckCount > stuckCriteria){
			//System.out.println("Stuck!"+stuckCount);
			reward-= 20;
		}
		*/
		
		prevState_score = currentScore;
		
		// System.out.println("Reward: "+reward);
		return reward;
	}

	@Override
	public MarioState copy(){
		ShortRangeState copied = new ShortRangeState();
		
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
}

