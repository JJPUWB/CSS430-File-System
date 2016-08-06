//FileTableEntry.java
//Team: Jacob J. Parkinson, Fuli Lan, Duke Dynda, Nicholas Koudsieh
//Original version by Professor Michael Panitz
//UWB Su16 CSS430
//No modifications have been made to this file


public class FileTableEntry {  // Each table entry should have
    public int seekPtr;        //    a file seek pointer
    public final Inode inode;  //    a reference to an inode
    public final short iNumber;//    this inode number
    public int count;          //    a count to maintain #threads sharing this
    public final String mode;  //    "r", "w", "w+", or "a"
    FileTableEntry ( Inode i, short inumber, String m ) {
        seekPtr = 0;           // the seek pointer is set to the file top.
        inode = i;
        iNumber = inumber;
        count = 1;           // at least one thread is using this entry.
        mode = m;            // once file access mode is set, it never changes.

        if ( mode.compareTo( "a" ) == 0 )
            seekPtr = inode.length;
    }
}
