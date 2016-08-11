//FileTable.java
//Team: Jacob J. Parkinson, Fuli Lan, Duke Dynda, Nicholas Koudsieh
//Original version by Professor Michael Panitz
//UWB Su16 CSS430
//Heavy modifications have been made to this file

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
        Inode inode = null;
        short iNumber = -1;
        while(true)
        {
            if(filename.equals("/"))
            {  // in root dir so stay at 0
                iNumber = 0;
            }
            else
            {
                iNumber = dir.namei(filename);  // not in root dir
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
                            wait();
                        }
                        catch (InterruptedException var7)
                        {

                        }
                        continue;
                    }
                    inode.flag = 1;
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
                        this.wait();
                    }
                    catch (InterruptedException var6)
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
