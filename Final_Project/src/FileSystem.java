/**
 * This class
 *
 * @author Duke Dynda, Fuli, Jake, Nick
 * @version 0.0.0 7/25/2016.
 */
public class FileSystem {
    // format
    // open
    // read
    // write
    // seek
    // close
    // delete
    // fsize
    public int fsize(FileTableEntry fte) {
        if(fte != null) {
            return fte.inode.length;
        }
        else {
            return -1;
        }
    }
}
