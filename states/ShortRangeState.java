package myagent.states;


import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;
import myagent.actions.MarioAction;

import java.lang.Math;
import java.util.Arrays; // For byte[] hashCodes
import java.util.Random;



public class ShortRangeState implements MarioState{
	protected static final int HOW_MANY_ENEMIES = 2; // do you want in the rep
	protected static float HALF_CELL_X = 8f,
							HALF_CELL_Y = 8f;
	protected static float ENEMY_HALF_CELL_X = 13.47f,
						 ENEMY_HALF_CELL_Y = 11.63f;

	protected static float  spaceResolution=5f; // One whole cell?
	protected static int enemyRange = 6;
	protected static int rangeLeft = 2,
						rangeRight = 3,
						rangeUp = 4,
						rangeDown= 2;
	protected boolean inited; // true if this represents a valid state
	ShortRangeState(){
		inited = false;
	}
	protected EvaluationInfo evaluationInfo;
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
	protected static final int stuckCriteria = 48;	//  sec @ 24 fps
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
	
		return possibleActions;
	}

	@Override
	public void updateObservedState(Environment environment){

	    levelScene = environment.getLevelSceneObservationZ(zLevelScene);
	    enemyScene = environment.getEnemiesObservationZ(zLevelEnemies);
	    
	    
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

	    
	    prevState_x = marioFloatPos[0];
		prevState_y = marioFloatPos[1];
		prevState_collisions = collisions;

	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();
	    // Check if stuck:
	    //
	    /*
	    float mx = Math.round(marioFloatPos[0]/5);
	    if(mx > maxEver_x) { maxEver_x = (mx+1f); stuckCount = 0;}
	    else
	    	stuckCount++;
    	//*/
    	/*
		if(marioFloatPos[0] > maxEver_x ){ maxEver_x = marioFloatPos[0]; stuckCount = 0;}
	    else if ( (maxEver_x+0.1f) >= marioFloatPos[0] )//( Math.abs(maxEver_x - marioFloatPos[0]) < 2f )
	    	stuckCount++;
	    else 
	    	stuckCount=0;
	    //*/
	    ///*
	     
	    if(marioFloatPos[0] > maxEver_x )maxEver_x = marioFloatPos[0];
		
	    if( Math.abs(prevState_x - marioFloatPos[0]) < 3f )
	    	stuckCount++;
	    else
	    	stuckCount=0;
	    //*/
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

	

	protected byte[] encodeMarioState(){
		/**
			Encodes any relevant info we have about mario in a byte[]
		**/
		//return new byte[]{};
		//if(isStuck()) System.out.println("\n STUCK@"+marioFloatPos[0]/8);
		return new byte[]{
			(byte)((Math.round(marioFloatPos[0])%5)/2), // This will work, with or without you. (and the /2)
			(byte)(isStuck()?1:0),
		}; // This is needed else he gets stuck when the frame is stati
		
		
	}
	

	protected int[] computeMarioRelXY(){
		/** Used to compute the limited field of vision**/
		int x = (int)Math.floor(marioFloatPos[0]/16)-1,
			y = (int)Math.floor(marioFloatPos[1]/16)-1;
		if(x>9)
			x=9;
		
		return new int[]{x,y};
		
	}

	protected byte[] encodeLevelScene(byte[][] ls){
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

	protected byte[] encodeEnemiesScene(){
		// Pick the closest 2
		byte rep[] = new byte[]{(byte)127,(byte)127,(byte)127,(byte)127};;
		
		if(enemiesFloatPos==null)
			return rep;
		int enemiesInRange = enemiesFloatPos.length / 3;
		
		int x,y;
		
		for(int i=0;i<enemiesInRange;i++){
			x= Math.round(enemiesFloatPos[3*i+1] / (2*ENEMY_HALF_CELL_X) );
			y= Math.round(enemiesFloatPos[3*i+2] / (2*ENEMY_HALF_CELL_Y) );//2*ENEMY_HALF_CELL_X
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
		// if(rep[0]!=127)			System.out.println(rep[0]+" "+rep[1]+" ");
		if( HOW_MANY_ENEMIES ==2 )
			return new byte[]{rep[0],rep[1],rep[2],rep[3]};
		else
			return new byte[]{rep[0],rep[1]};
	}

	/** 
		Members to calculate the immediate reward
	**/
	public float[] closestEnemyPos(){
		int enemiesInRange = enemiesFloatPos.length / 3;
			
			float x = 0f,closestX = 1000f, closestY = 1000f;
			for(int i=0;i<enemiesInRange;i++){
				x= Math.abs(enemiesFloatPos[3*i+1]);
				if( x < closestX ){
					closestX = x;
					closestY = enemiesFloatPos[3*i+2];
				}
			}
			return new float[]{closestX,closestY};
	}
	

	protected boolean wasStuckInLastTurn = false;
	@Override
	public float getReward(){
		
		
		float reward = 0f;
		
		reward += (marioFloatPos[0] - prevState_x);
		
		reward -= 100f *  (collisions - prevState_collisions);
		if(inDanger())				reward -= 20f;
		
		//if(isStuck())			reward -= 5f;
		

		//if( wasStuckInLastTurn && !isStuck() )			reward+= 50f; // Reward unstuck
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


	public boolean inDanger(){


		int enemiesInRange = enemiesFloatPos.length / 3;
			
			float x = 0f,y = 0f;
			for(int i=0;i<enemiesInRange;i++){
				x= Math.abs(enemiesFloatPos[3*i+1]);
				y= enemiesFloatPos[3*i+2];
				if(  ( x < 3*ENEMY_HALF_CELL_X ) && (y < ENEMY_HALF_CELL_Y) ) // Y is positive downwards
					return true;
					
			}
			return false;
	}

}

