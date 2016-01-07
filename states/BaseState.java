package myagent.states;


import ch.idsia.benchmark.mario.environments.Environment;
import ch.idsia.tools.EvaluationInfo;
import myagent.actions.MarioAction;

import java.lang.Math;
import java.util.Arrays; // For byte[] hashCodes
import java.util.Random;



public class BaseState implements MarioState{
	protected boolean inited = false;
	BaseState(){
		inited = false;
	}
	protected int[] marioRelXY = new int[]{9,9}; // This is the position of mario's feet
	
	protected EvaluationInfo evaluationInfo;
	/* The params that BasicMarioAIAgent had. Good starting point for our state */
	protected byte[][] levelScene;
	protected byte[][] enemyScene;

	protected float[] 	marioFloatPos = new float[]{0,0},
						prevFloatPos = new float[]{0,0};
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
	
	protected int totalKills = 0;



	@Override
	public byte[] getStateRep(){
		/**
		 Returns a minimal encoding of the state 
		 **/ 
		int [][] ls = encodeLevelScene();
		int [] marioVelocity = new int[]{ (int)(marioFloatPos[0] - prevFloatPos[0]) , (int)(marioFloatPos[1] - prevFloatPos[1]) };
		
		// if(marioVelocity[0]!=0 || marioVelocity[1]!=0)			System.out.println( marioVelocity[0] +" , " + marioVelocity[1] );
		
		byte [] rep = new byte[ ls[0].length * ls[1].length ];
		for(int i=0;i<ls[0].length;i++){
			for(int j=0;j<ls[1].length;j++){
				rep[i * ls[0].length + j ] = (byte)ls[i][j];
			}
		}
		return rep;
	}
	public int [][] encodeLevelScene(){
		/**
		A 6x6 grid ( 3X3 divided into 2 for better resolution )
		Each needs 2 bits, {X,Y}
			X = 0 if air, 1 if solid
			Y = 1 if enemy present
		
		**/
		int [][] ls = new int [6][6];
		int [] halfOff = new int[2]; // X,Y ; X=1 -> mario on right half of square, Y=1 -> Mario on bottom of square
		// Don't bother about ^this being not centered. The learning should figure out where the center is
		halfOff[0] = (((int)marioFloatPos[0])%16) / 8;
		halfOff[1] = (((int)marioFloatPos[1])%16) / 8;
		
		int startY = (marioRelXY[1] - 1)*2 + halfOff[1];
		int startX = (marioRelXY[0] - 1)*2 + halfOff[0];
		
		
		for(int i=0; i<6; i++){
			for(int j=0; j<6; j++){
				ls[i][j] = (levelScene[(startY+i)/2][(startX+j)/2]==1)?1:0;
				ls[i][j] |= (enemyScene[(startY+i)/2][(startX+j)/2]==1)?2:0;
			}
		}
		
		return ls;
	}
	
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
			//MarioAction.JUMP,
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

	    inited=true;
		
	    levelScene = environment.getLevelSceneObservationZ(zLevelScene);
	    enemyScene = environment.getEnemiesObservationZ(zLevelEnemies);
	    
	    this.marioState = environment.getMarioState();

	    receptiveFieldWidth = environment.getReceptiveFieldWidth();
	    receptiveFieldHeight = environment.getReceptiveFieldHeight();
	    evaluationInfo = environment.getEvaluationInfo();
	    marioStatus = marioState[0];
		marioMode = marioState[1];
	    isMarioOnGround = marioState[2] == 1;
	    isMarioAbleToJump = marioState[3] == 1;
	    isMarioAbleToShoot = marioState[4] == 1;
	    isMarioCarrying = marioState[5] == 1;
		    
	    //timeLeft = marioState[10];
	    currentScore = environment.getIntermediateReward();
	    totalKills = environment.getKillsByStomp();


	    

	    this.prevFloatPos = marioFloatPos;
	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();
	    
	    
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
	public float getReward(){
		return evaluationInfo.computeWeightedFitness();
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
		
		copied.totalKills = totalKills;

		copied.inited=true;

		return copied;
	}

	@Override
	public EvaluationInfo getEvaluationInfo(){
		return evaluationInfo;
	}


	// Not used
	@Override
	public boolean isStuck(){	return false;	}
	@Override
	public void resetStuck(){}
	
}

