/**
 * This class
 *
 * @author Duke Dynda, Fuli, Jake, Nick
 * @version 0.0.0 7/25/2016.
 */
public class FileSystem {
    FileTable ft;
    // format
    // open
    // read
    // write
    // seek
    // close - closes the file
    public synchronized int close(FileTableEntry fte) {
        if(fte != null) {
            return ft.ffree(fte) ? 0 : -1;
        } else {
            return -1;
        }
    }
    // delete
    // fsize - returns the file size
    public int fsize(FileTableEntry fte) {
        if(fte != null) {
            return fte.inode.length;
        } else {
            return -1;
        }
    }
}
