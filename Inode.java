/**
 * Created by Michael on 7/23/2015.
 */
public class Inode
{
    private final static int iNodeSize = 32;       // fix to 32 bytes
    private final static int directSize = 11;      // # direct pointers

    public int length;                             // file size in bytes
    public short count;                            // # file-table entries pointing to this
    public short flag;                             // 0 = unused, 1 = used, ...
    public short direct[] = new short[directSize]; // direct pointers
    public short indirect;                         // a indirect pointer

    Inode( )
    {                                     // a default constructor
        length = 0;
        count = 0;
        flag = 1;
        for ( int i = 0; i < directSize; i++ )
        {
            direct[i] = -1;
        }
        indirect = -1;
    }

    Inode( short iNumber )  // retrieving inode from disk
    {
        int numBlock = 1 + (iNumber / 16);
        byte[] iBlock = new byte[512];
        SysLib.rawread(numBlock, iBlock);
        int offset = (iNumber % 16) * 32;

        length = SysLib.bytes2int(iBlock, offset);
        offset += 4;
        count = SysLib.bytes2short(iBlock, offset);
        offset += 2;
        flag = SysLib.bytes2short(iBlock, offset);
        offset += 2;

        for (int i = 0; i < directSize; i++, offset += 2)
        {
            this.direct[i] = SysLib.bytes2short(iBlock, offset);
        }

        offset += 2;

        this.indirect = SysLib.bytes2short(iBlock, offset);



    }

    int toDisk( short iNumber )     // save to disk as the i-th inode
    {
        return 0;
        // Will be implemented later
    }

    int getLength()     //Will return file length
    {
        return length;
    }

    short getFlag()     //Will return flag of this Inode
    {
        return flag;
    }

    short getIndexBlockNumber()
    {
        return 0;
    }

    boolean setIndexBlock(short indexBlockNumber)
    {
        return true;
    }

    short findTargetBlock(int offset)
    {
        return 0;
    }



}
