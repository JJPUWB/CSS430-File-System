//FileTable.java
//Team (Group 6): Jacob J. Parkinson, Duke Dynda, Fuli Lan, Nicolas Koudsieh
//Original version by Professor Michael Panitz
//UWB Su16 CSS430
//Heavy modifications have been made to this file
//FINAL Version
import java.util.Vector;

public class FileTable
{
    private Vector table;    // the actual entity of this file table
    private Directory dir;        // the root directory

    public FileTable( Directory directory )
    { // constructor
        table = new Vector( );     // instantiate a file (structure) table
        dir = directory;           // receive a reference to the Director
    }                             // from the file system

    // major public methods
    public synchronized FileTableEntry falloc( String filename, String mode )
    {
        // allocate a new file (structure) table entry for this file name
        // allocate/retrieve and register the corresponding inode using dir
        // increment this inode's count
        // immediately write back this inode to the disk
        // return a reference to this file (structure) table entry

        Inode inode = null;     //Declare new Inode object
        short iNumber = -1;     //Corresponding iNumber
        while(true)
        {
            if(filename.equals("/"))
            {  // in root dir so stay at 0
                iNumber = 0;
            }
            else
            {
                iNumber = dir.namei(filename);  // not in root dir, find the corresponding inumber
            }

            if(iNumber >= 0)
            {  // there has to be a file
                // setup to load inode from disk to memory
                inode = new Inode(iNumber);
                if(mode.equals("r"))
                {
                    if(inode.flag != 0 && inode.flag != 1)
                    {    // not unused nor used
                        try
                        {
                            wait();     //Have this thread wait
                        }
                        catch (InterruptedException e)
                        {

                        }
                        continue;
                    }
                    inode.flag = 1;     //set state to used
                    break;
                }

                if(inode.flag != 0 && inode.flag != 3)
                {
                    if(inode.flag == 1 || inode.flag == 2)
                    {
                        inode.flag = (short)(inode.flag + 3);
                    }
                    try
                    {
                        this.wait();        //Have this thread wait
                    }
                    catch (InterruptedException e)
                    {

                    }
                    continue;
                }
                inode.flag = 2;
                break;
            }

            if(mode.equals("r"))
            {
                return null;
            }

            iNumber = dir.ialloc(filename);     //allocate a inumber for this file
            inode = new Inode();        //instantiate a new Inode
            inode.flag = 2;     //set the flag to 2
            break;
        }

        inode.count++;      //Increment the count
        inode.toDisk(iNumber);      //Write the Inode to disk
        FileTableEntry fte = new FileTableEntry(inode, iNumber, mode);
        table.addElement(fte);      //Add the file table entry to the vector
        return fte;

    }

    // free memory and store on disk instead
    // 0 = unused, 1 = used, 2 = read, 3 = write, 4 = delete.
    public synchronized boolean ffree( FileTableEntry FTE )
    {
        if (FTE == null)
        {
            return false;
        }

        //If there exists some such FTE
        if (table.contains(FTE))
        {
            //Then remove it
            table.removeElement(FTE);

            //Decrement the counts of references to the Inode (but don't go negative!)
            if (FTE.inode.count >= 1)
            {
                FTE.inode.count--;
            }
            //Remember that the FileTable is allowed to close a file when there no longer exist any references to it
            FTE.inode.flag = 0;

            // save the corresponding inode to the disk
            FTE.inode.toDisk(FTE.iNumber);

            //Deallocate memory space
            FTE = null;

            //Notify that the file has been closed
            notify();

            // return true
            return true;
        }
        //The FTE being removed doesn't exist
        else
        {
            return false;
        }
    }


    public synchronized boolean fempty() {
        return table.isEmpty();
    }
}
