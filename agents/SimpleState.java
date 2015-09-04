package myagent.agents;


import ch.idsia.benchmark.mario.environments.Environment;


public class SimpleState implements MarioState{
	
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
	int zLevelScene = 2;
	int zLevelEnemies = 2;

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
	}

	public void updateObservationDetails(final int rfWidth, final int rfHeight, final int egoRow, final int egoCol){
	    receptiveFieldWidth = rfWidth;
	    receptiveFieldHeight = rfHeight;

	    marioEgoRow = egoRow;
	    marioEgoCol = egoCol;
	}

	public byte[] getStateRepresentation(){
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
		byte[] mState = new byte[4];
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
		if(ls.length==0)
			return new byte[0];

		int bitsUsed = 2;
		int repLength = ((ls.length*ls[0].length) * bitsUsed); //How many bits

		repLength+=repLength%8;	// Incase we don't fit perfectly.
		repLength/=8;	// How many bytes do we need?

		byte[] rep = new byte[repLength];
		int k=0, shiftBy=0;
		byte unitCode;	// X0=> Air, X1=> Solid, 0=>No Monster, 1X=> Monster
		for(int i=0;i<ls.length;i++){
			for(int j=0;j<ls[i].length;j++){
				unitCode=0;
				if(levelScene[i][j]==1)
					unitCode|=1;
				if(enemyScene[i][j]==1)
					unitCode|=3;
				unitCode <<= shiftBy;
				rep[k] |= unitCode;
				shiftBy = (shiftBy+2)%8;
				if(shiftBy==0){
					k++;
					rep[k]=0;
				}
			}
		}
		return rep;
	}


}

