/**
 * Created by Michael on 7/23/2015.
 */
import java.util.Vector;

public class FileTable
{
    private Vector table;    // the actual entity of this file table
    private Directory dir;        // the root directory

    public FileTable( Directory directory ) { // constructor
        table = new Vector( );     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    // major public methods
    public synchronized FileTableEntry falloc( String filename, String mode ) {
        Inode inode = null;
        short iNumber = -1;
        while(true) {
            if(filename.equals("/")) {  // in root dir so stay at 0
                iNumber = 0;
            } else {
                iNumber = dir.namei(filename);  // not in root dir
            }

            if(iNumber >= 0) {  // there has to be a file
                // setup to load inode from disk to memory
                inode = new Inode(iNumber);
                if(mode.equals("r")) {
                    if(inode.flag != 0 && inode.flag != 1) {    // not unused nor used
                        try {
                            wait();
                        } catch (InterruptedException var7) {

                        }
                        continue;
                    }
                    inode.flag = 1;
                    break;
                }

                if(inode.flag != 0 && inode.flag != 3) {
                    if(inode.flag == 1 || inode.flag == 2) {
                        inode.flag = (short)(inode.flag + 3);
                    }
                    try {
                        this.wait();
                    } catch (InterruptedException var6) {

                    }
                    continue;
                }
                inode.flag = 2;
                break;
            }

            if(mode.equals("r")) {
                return null;
            }

            iNumber = dir.ialloc(filename);
            inode = new Inode();
            inode.flag = 2;
            break;
        }
        inode.count++;
        inode.toDisk(iNumber);
        FileTableEntry fte = new FileTableEntry(inode, iNumber, mode);
        table.addElement(fte);
        return fte;
        // allocate a new file (structure) table entry for this file name
        // allocate/retrieve and register the corresponding inode using dir
        // increment this inode's count
        // immediately write back this inode to the disk
        // return a reference to this file (structure) table entry
    }

    // free memory and store on disk instead
    // 0 = unused, 1 = used, 2 = read, 3 = write, 4 = delete.
    public synchronized boolean ffree( FileTableEntry fte ) {
        // receive a file table entry reference
        if(table.removeElement(fte)) {  // fte is a file table entry
            // free this file table entry.
            fte.inode.count--;
            if(fte.inode.flag == 2 || fte.inode.flag == 3) {
                fte.inode.flag = 0;
            }
            // save the corresponding inode to the disk
            fte.inode.toDisk(fte.iNumber);
            fte = null;
            notify();
            // return true if this file table entry found in my table
            return true;
        }
        else {
            return false;
        }
    }

    public synchronized boolean fempty( ) {
        return table.isEmpty( );  // return if table is empty
    }                            // should be called before starting a format
}
