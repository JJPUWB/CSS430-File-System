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
		//To be implemented!
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
		SysLib.int2bytes(count, tmp, sentinel + 4);
		SysLib.int2bytes(flag, tmp, sentinel + 6);

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

	//Requires implementation!
	int mapOffset(int offset)
	{
		return 0;
	}

}