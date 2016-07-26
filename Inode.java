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
        // Will be implemented later


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





}
