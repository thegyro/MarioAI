package myagent.states;


import ch.idsia.benchmark.mario.environments.Environment;
import myagent.actions.MarioAction;

public class SimpleState implements MarioState{
	
	private boolean inited; // true if this represents a valid state
	SimpleState(){
		inited = false;
	}

	/* The params that BasicMarioAIAgent had. Good starting point for our state */
	protected byte[][] levelScene;
	protected byte[][] enemyScene;

	protected float[] marioFloatPos = null;
	protected float[] enemiesFloatPos = null;

	protected int[] marioState = null;

	protected int marioStatus;
	protected int marioMode;
	protected boolean isMarioOnGround;
	protected boolean isMarioAbleToJump;
	protected boolean isMarioAbleToShoot;
	protected boolean isMarioCarrying;


	// Observation details
	protected int receptiveFieldWidth;
	protected int receptiveFieldHeight;
	protected int marioEgoRow;
	protected int marioEgoCol;

	//what level of detail do we want?
	private int zLevelScene = 2;
	private int zLevelEnemies = 2;


	// Things we don't use in the state but do in the reward ( Is that even allowed? )
	private int prevState_kills =0;
	private float prevState_x = 0;
	private int totalKills = 0;

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
		int paSize = 4;
		if( canMarioJump() )
			paSize++;

		MarioAction possibleActions[] = new MarioAction[paSize];
		int i=0;
		possibleActions[i++]= MarioAction.LEFT;
		possibleActions[i++]= MarioAction.RIGHT;
		possibleActions[i++]= MarioAction.DOWN;
		// if( canMarioShoot() )
		// 	possibleActions[i++]= MarioAction.SHOOT;
		// else
		// 	possibleActions[i++]= MarioAction.SPEED;
		possibleActions[i++]= MarioAction.SPEED;
		
		if( canMarioJump() )
			possibleActions[i++] = MarioAction.JUMP;

		return possibleActions;
	}

	@Override
	public void updateObservedState(Environment environment){

	    levelScene = environment.getLevelSceneObservationZ(zLevelScene);
	    enemyScene = environment.getEnemiesObservationZ(zLevelEnemies);
	    
	    this.marioFloatPos = environment.getMarioFloatPos();
	    this.enemiesFloatPos = environment.getEnemiesFloatPos();
	    this.marioState = environment.getMarioState();

	    receptiveFieldWidth = environment.getReceptiveFieldWidth();
	    receptiveFieldHeight = environment.getReceptiveFieldHeight();


	    marioStatus = marioState[0];
		marioMode = marioState[1];
	    isMarioOnGround = marioState[2] == 1;
	    isMarioAbleToJump = marioState[3] == 1;
	    isMarioAbleToShoot = marioState[4] == 1;
	    isMarioCarrying = marioState[5] == 1;
		    
	    //timeLeft = marioState[10];

	    totalKills = environment.getKillsTotal();

	    inited=true;
	}

	@Override
	public void updateObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol){
	    receptiveFieldWidth = rfWidth;
	    receptiveFieldHeight = rfHeight;

	    marioEgoRow = egoRow;
	    marioEgoCol = egoCol;
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

	private byte[] encodeMarioState(){
		/**
			Encodes any relevant info we have about mario in a byte[]
		**/
		byte[] mState = new byte[5];
		mState[3] = (byte)(marioStatus >>> 24);
        mState[2] = (byte)(marioStatus >>> 16);
        mState[1] =(byte)(marioStatus >>> 8);
        mState[0] =(byte)(marioStatus);
		

		mState[4]= 0;
		if(isMarioOnGround)
			mState[4]|=1;
		if(isMarioAbleToJump)
			mState[4]|=2;
		if(isMarioAbleToShoot)
			mState[4]|=4;
		if(isMarioCarrying)
			mState[4]|=8;
		return mState;
	}
	private byte[] encodeLevelScene(byte[][] ls){
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
		if(ls.length==0)	// Just incase
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
	@Override
	public float getReward(){
		/* 
			10 for a kill.
			a little bit for going in the right direction
		*/
		float reward = 0;
		reward += 10 * (totalKills - prevState_kills);
		prevState_kills = totalKills;
		
		// Try something new.
		reward +=  (marioFloatPos[0] - prevState_x); // /timeLeft;
		prevState_x = marioFloatPos[0];
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

		// Things we don't use in the state but do in the reward ( Is that even allowed? )
		copied.prevState_kills = prevState_kills;
		copied.prevState_x = prevState_x;
		copied.totalKills = totalKills;

		copied.inited=true;

		return copied;
	}
}

