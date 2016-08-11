//Inode.java
//Jacob J. Parkinson
//Initial code provided by Professor Michael Panitz

public class Inode
{

	private final static int iNodeSize = 32;		//fix to 32B
	private final static int directSize = 11;		//# direct ptrs
	
	public int length;					//file size in B
	public short count;					//# file-table entries pointing to this
	public short flag;					//0 = unused, 1 = used, ...
	public short direct[] = new short[directSize];	//direct ptrs
	public short indirect;				//a single indirect ptr

	//Default constructor
	Inode()
	{
		length = 0;
		count = 0;
		flag = 1;
		
		for (int i = 0; i < directSize; i++)
		{
			direct[i] = -1;
		}
		indirect = -1;
	}

	//Helper constructor
	//Layer calls: relies on toDisk for outgoing call
	//		 takes incoming call from Superblock.format() for test1
	Inode(int Len, int Direct, boolean saveToDisk)
	{
		//Assign the length of the Inode
		length = Len;

		//Cast convert. Takes an int param for interfacing convenience.
		direct[0] = (short)Direct;
		
		flag = 1;

		for (int i = 1; i < directSize; i++)
		{
			direct[i] = -1;
		}
		indirect = -1;

		//If provided a true boolean value as param3, pass control to toDisk()
		if (saveToDisk)
		{
			toDisk((short)0);
			return;
		}
		else
		{
			return;
		}
	}

	//retrieving the inode from the disk
	//Layer calls: calls downward to SysLib to retrieve an Inode from the disk and initialize
	//itself with the retreived Inode
	Inode (short iNumber)
	{
		//Next free block to use
		int blockNumber = 1 + (iNumber / 16);

		byte[] tmpRead = new byte[Disk.blockSize];
		SysLib.rawread(blockNumber, tmpRead);
		int offset = (iNumber % 16) * 32;

		//Code very must inspired by the Test5 code showing how this works
		//Set up the length
		length = SysLib.bytes2int(tmpRead, offset);
		offset += 4;
		//Set up the count
		count = SysLib.bytes2short(tmpRead, offset);
		offset += 2;
		//Set up the flag
		flag = SysLib.bytes2short(tmpRead, offset);
		offset += 2;

		//Set each direct block
		for(int i = 0; i < directSize; i++)
		{
			direct[i] = SysLib.bytes2short(tmpRead, offset);
			offset += 2;
		}
		//Set the indirect block
		indirect = SysLib.bytes2short(tmpRead, offset);
	}

	//save to the disk as the i-th node
	//Layer calls: Calls downward to SysLib (which further calls to the disk)
	int toDisk (short iNumber)
	{
		//The # of inodes to a single block can be found by Disk.blockSize / Inode.inodeSize
		//In this implementation, that value is 512 / 32 = 16

		//Find the right block number
		int blockNum = iNumber / 16 + 1;

		//Track toward the end of the Inode (file)
		int sentinel = (iNumber % 16) * 32;
		
		byte[] tmp = new byte[Disk.blockSize];
		
		//Read from the block
		SysLib.rawread(blockNum, tmp);

		//Convert the length of the file into bytes and place it in 
		SysLib.int2bytes(length, tmp, sentinel);
		SysLib.short2bytes(count, tmp, sentinel + 4);
		SysLib.short2bytes(flag, tmp, sentinel + 6);

		//Increment the sentinel 4 for the length, 2 for the count, 2 for the flag
		sentinel += 8;

		//Convert the 11 direct Inodes into bytes. 
		for (int i = 0; i < directSize; i++)
		{
			SysLib.short2bytes(direct[i], tmp, sentinel);
			//Increment the sentinel variable by two for every append to the byte[]
			//2 is because there are two bytes in a short, and each direct Inode is a short.
			//This is also the reason for short2bytes being used
			sentinel +=2;
		}
		
		//Convert the single indirect Inode into bytes
		SysLib.short2bytes(indirect, tmp, sentinel);

		//Write the entire block to the disk using the byte[] and the block number
		SysLib.rawwrite(blockNum, tmp);

		//Return the # of the block
		return blockNum;
	}

	//Just provides interfacing with integers
	int toDisk (int iNumber)
	{
		short i = (short)iNumber;
		return toDisk(i);
	}

	//Add a new index to be used for Inodeblock creation
	//Layer calls: communicates with SysLib
	boolean addIndex(int newIdx)
	{
		for(int i = 0; i < directSize; i++)
		{
			//If a direct index is unassigned, we can use it and shouldn't add an index
			if(direct[i] == -1)
			{
				return false;
			}
		}

		//If the directs are all taken but the indirect isn't taken, we can use that and shouldn't add an index
		if(indirect != -1)
		{
			return false;
		}
		//Otherwise, we actually add the index
		else
		{
			//Temporary byte[] to write to a block as a new index
			byte[] tmpWrite = new byte[512];

			//Convert short to bytes and fill tmpWrite
			for(int i = 0; i < 256; i++)
			{
				SysLib.short2bytes((short)-1, tmpWrite, (i * 2));
			}
			//Update with added index
			SysLib.rawwrite((short)newIdx, tmpWrite);
			//Assign indirect block to new Idx after 'writing' to it
			indirect = (short)newIdx;

			//Return success
			return true;
		}
	}

	//Setup an Inode's block, given an offset and block#
	//Layer calls: communicates with SysLib
	boolean setupInodeBlock(int offset, int block)
	{
		//The block index is found by the offset over the Disk.blockSize, as explained in the videos
		int blockIdx = offset / Disk.blockSize;

		//Is it a direct block?
		if(blockIdx < 11)
		{
			direct[blockIdx] = (short)block;
			return true;
		}
		//Is it an error?
		else if(indirect < 0)
		{
			return false;
		}
		//Is it an indirect block?
		else
		{
			//Temporary byte[] to read the indirect block
			byte[] tmpRead = new byte[Disk.blockSize];
			SysLib.rawread(indirect, tmpRead);

			//This modifies the short value of the block and puts it into the tmpRead byte[] at the proper position
			//the position is given by the blockIdx (offset / Disk.blockSize) - the direct Inodes (*2 because short
			//= 2B)
			SysLib.short2bytes((short)block, tmpRead, (blockIdx - directSize) * 2);

			//Write the tmpRead[] back to the indirect
			SysLib.rawwrite(indirect, tmpRead);
			return true;
		}
	}

	//Maps the offset of a file to a specific block
	//Layer Calls: calls downward to SysLib
	int mapOffset(int offset)
	{
		//Based on the offset, the block index is either the indirect block, or one of 11 direct blocks
		int blockIdx = offset / Disk.blockSize;

		//Easiest case: offset maps to a direct block which is created
		if(blockIdx < directSize)
		{
			return direct[blockIdx];
		}
		//error
		else if(indirect < 0)
		{
			return -1;
		}
		//Harder case: offset maps to indirect block
		else
		{
			byte[] tmpRead = new byte[Disk.blockSize];
			SysLib.rawread(indirect, tmpRead);

			//Remember that a short = 2 bytes
			int ret = SysLib.bytes2short(tmpRead, (blockIdx - directSize) * 2);
			return ret;
		}
	}

	//Simple mutator to change length
	void setLength(int newLength)
    	{
	    length = newLength;
    	}

}
