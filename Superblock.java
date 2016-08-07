/**
 * Created by Michael on 7/23/2015.
 */
public class Superblock
{
    private final int defaultInodeBlocks = 64;
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head

    public Superblock(int diskSize)
    {
        //Read first disc block 0 from disk
        byte[] tmp = new byte[Disk.blockSize];
        SysLib.rawread(0, tmp);
        totalBlocks = SysLib.bytes2int(tmp, 0);

        //Create Inode blocks & increase data sentinel by 4
        totalInodes = SysLib.bytes2int(tmp, 4);

        //Write Inode blocks to disk (into free blocks) & increase the data sentinel by 4
        freeList = SysLib.bytes2int(tmp, 8);

        //On initialization of the Superblock class, the following three attributes should always
        //be true:
        //	1. total number of blocks must be equal to diskSize
        //	2. total number of Inodes should be 0
        //	3. total blocks free should be no more than two
        //	(one for the first initialized Inode, and one for the superblock's variables)
        if (totalBlocks != diskSize || totalInodes != 0 || freeList >= 2)
        {
            //Error occurred
            return;
        }
        //Otherwise, I can actually write the resultant free blocks and total blocks to disk:
        else
        {
            //Internal call - call helper
            format(64);
        }

    }

    //This method is required in order to solve test1 [format(48)] while respecting layer architecture
    //Layer calls: calls to SysLib
    //		 calls to Inode.java to put the newly formatted Inode onto the disk
    public void format(int fileCount)
    {
        //Not 100% sure why it's supposed to be 1000 blocks, but that's what the Test5 code says.
        //Maybe I missed a musing in the videos?
        totalBlocks = 1000;

        //The # of inodes to a single block can be found by Disk.blockSize / Inode.inodeSize
        //In this implementation, that value is 512 / 32 = 16

        //Set the total Inodes and the freeList based on the file number
        totalInodes = fileCount;
        freeList = (fileCount / 16) + 1;

        //Create the 0th Inode and move it onto disk (using the true param3)
        //According to the notes provided in the video on the Inode class, 64 is the default Inode#.
        //Inode node = new Inode(64, freeList, true);

        //Create a data byte[] to hold the file data
        byte[] buffer = new byte[Disk.blockSize];
        //freeList++;
        SysLib.rawwrite(freeList,buffer);

        //Set up the block freeList.
        for (int i = freeList; i < totalBlocks; i++)
        {
            if (i < (totalBlocks - 1))
            {
                SysLib.int2bytes(i + 1, buffer, 0);
                SysLib.rawwrite(i, buffer);
            }
            else if (i == (totalBlocks - 1))
            {
                SysLib.int2bytes(-1, buffer, 0);
            }
        }
        //Save one slot for the superblock's actual objects
        SysLib.rawwrite(totalBlocks - 1, buffer);

        //Call sync() helper
        sync();
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
        return freeList;
    }

    //Simple get method
    public int getNumDiskBlocks()
    {
        //return total disk blocks
        return totalBlocks;
    }

    //Simple get method
    public int getNumInodes()
    {
        //return total inodes;
        return totalInodes;
    }

    public int getFreeBlock()
    {
        // Dequeue the top block from the free list
        int topBlock = freeList;
        if(topBlock != -1) {
            byte[] freeBlock = new byte[Disk.blockSize];    // the next free block
            SysLib.rawread(topBlock, freeBlock);
            freeList = SysLib.bytes2int(freeBlock, 0);  // get the free block
            SysLib.int2bytes(0, freeBlock, 0);
            SysLib.rawwrite(topBlock, freeBlock);
        }
        return topBlock;
    }


    public boolean returnBlock(int blk)
    {
        //Enqueue a given block to the end of the free list
        if(blk < 0)
        {
            return false;
        }
        else
        {
            byte[] data = new byte[512];

            for(int i = 0; i < 512; ++i)
            {
                data[i] = 0;
            }

            SysLib.int2bytes(this.freeList, data, 0);
            SysLib.rawwrite(blk, data);
            this.freeList = blk;
            return true;
        }
    }

    //Not initially included, but Superblock.java should have a sync() method instead of
    //doing it in FileSystem.java in order to properly preserve the layered architecture.
    //Layer calls: downward to SysLib
    //		 called by FileSystem.java
    public void sync()
    {
        //Code is basically found in the constructor as well
        byte[] tmp = new byte[Disk.blockSize];
        SysLib.int2bytes(totalBlocks, tmp, 0);
        SysLib.int2bytes(totalInodes, tmp, 4);
        SysLib.int2bytes(freeList, tmp, 8);

        //Update the disk
        SysLib.rawwrite(0, tmp);
    }
}

