/**
 * Created by Michael on 7/23/2015.
 */
public class Superblock
{
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head

    public Superblock( int diskSize )
    {
        totalBlocks = diskSize / 512;       //Since each block has 512 bytes
        freeList = 1;       //Upon construction block number of free list's head is 1, since superblock is 0
        totalInodes = totalInodes * 16;      //Since each block can contain 16 Inodes

    }
}
