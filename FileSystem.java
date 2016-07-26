/**
 * Created by nicko on 7/26/2016.
 */
public class FileSystem
{
    private Superblock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem(int diskBlocks)
    {
        //Create superBlock, and format disk with 64 Inodes in default
        superblock = new Superblock(diskBlocks);

        //Create directory, and register "/" in directory entry 0 (done internally to directory.java)
        //directory = new Directory(superblock.inodeBlocks);
        directory = new Directory(superblock.totalInodes);

        //File table is created, and store directory int he file table
        filetable = new FileTable(directory);

        //Directory reconstruction
        FileTableEntry directoryEntry = open("/", "r");
        int dirSize = fsize(directoryEntry);

        if (dirSize > 0)
        {
            byte[] directoryData = new byte[dirSize];
            read(directoryEntry, directoryData);
            directory.bytes2directory(directoryData);
        }
        close (directoryEntry);
    }

    void sync()
    {

    }

    boolean format(int files)
    {
        return false;
    }

    FileTableEntry open(String fileName, String mode)
    {
        return null;
    }

    boolean close(FileTableEntry fte)
    {
        return false;
    }

    int fsize(FileTableEntry fte)
    {
        if (fte != null)
        {
            return fte.inode.length;
        }
        else
        {
            return -1;
        }
    }

    int read(FileTableEntry fte, byte[] buffer)
    {
        return 0;
    }

    int write(FileTableEntry fte, byte[] buffer)
    {
        return 0;
    }

    private boolean deallocAllBlocks(FileTableEntry fte)
    {
        return false;
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    int seek(FileTableEntry fte, int offset, int whence)
    {
        return 0;
    }

    boolean delete(FileTableEntry fte)
    {
        return false;
    }

    boolean delete(Object fto)
    {
        FileTableEntry fte = (FileTableEntry)fto;
        return false;
    }
}
