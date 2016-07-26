//Superblock.java

class Superblock
{
	public int totalBlocks;		//# disk blocks
	public int totalInodes;		//# inodes
	public int freeList;			//block # of the free list's head

	
	public Superblock(int diskSize)
	{
		//Read first disc block 0 from disk
		//Create Inode blocks
		//Write Inode blocks to disk (into free blocks)
		//Write free blocks to disk
		//Write total blocks to disk
	}
	
	public int enqueueBlock(int blockNumber)
	{
		//write to first free block
		return 0;
	}
	
	public int dequeueBlock()
	{
		//Delete last (not free) block
		return 0;
	}

	public int getIndexOfFreeBlock()
	{
		//return first free block number
		return 0;
	}

	public int getNumDiskBlocks()
	{
		//return total disk blocks
		return totalBlocks;
	}

	public int getNumInodes()
	{
		//return total inodes;
		return totalInodes;
	}
}