//FileSystem.java
//Jacob J. Parkinson
//Purpose: Given a byte[], it'll read & write blocks so as to create the illusion that the
//		   file is a giant byte array (video). Basically uses the Singleton Class Architecture.
//		   Responsible for managing the logic related to the fileSystem. Handles logic of reading off of
//		   the disk and reading into the byte[] and then saving that block back to the disk (superBlock.sync)
//		   In the layered architecture, occupies the second level along with the TCb and FileTableEntry,
//		   below the Kernel (which implements SysLib)
//
//
//Inter-class communication Specifications: 
//	Incoming:
//		The Kernel is given a file descriptor by sysLib; this is further converted into a 
//		FileTableEntry by the TCB based on which file it corresponds to. This is then fed to
//		FileSystem.java
//	Outgoing:
//		Communicates with SuperBlock, who manages our freeBlockList.
//		Also reSyncs with the superBlock after changing any byte[] data so that it can properly
//		maintain the freeBlockList.
//		Communicates with INode, who tells us which blocks are already in the file.
//		FileSystem.Read() for exmaple can be used to get the byte at offset X. In this case,
//		The INode for the file takes this offset and maps to a specific block
//		If Writing past the end of the file, it combines both the prior mentioned outgoing calls and
//		obtains a free block from the superBlock and then adds that block to the file (INode)
//		Communicates with FileTable.java, who keeps track of which INodes are in memory and which are
//		on disk.
//		If asking to open an unopened file, the FileTable loads an inode from the disk into memory,
// 		creates an FTE, hands it to the TCB, which is handed to user code. The FileTable needs to
//		use Directory.java to map the file name to the Inode in between loading the Inode from the
//		disk into memory and creating an FTE
//
public class FileSystem
{

	private Superblock superblock;
	private Directory directory;
	private FileTable filetable;

	//Constructor, provided by instructor
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

	//Convert the open data to bytes and write that byte data into the filetable entry
	void sync()
	{
		//Declare a FileTableEntry and then point it at the file name '/' in mode WRITE
		FileTableEntry FTE = open("/", "w");
		//Create a temporary byte[] and convert the data waiting in the directory into bytes
		byte[] bytes = directory.directory2bytes();
		//Write those bytes into the filetable entry and then sync to superblock
		write(FTE, bytes);
		superblock.sync();
	}

	//Reformat everything again from scratch, basically.
	//Layered calls: format -> superBlock.format [implemented for test1] -> Inode.toDisk
	//		   	   -> new directory
	//			   -> new fileTable
	boolean format(int files)
	{
		//Format() must accomplish a reformatting task for each module in the underlying layer, 
		//except Inode. Each module should independently reformat themselves


		//No reason to reformat to 0 files
		if (files <= 0)
		{
			return false;
		}
		
		//1. Reformat the superblock
		//For this purpose, utilize the superblock.format to reformat the disk with a new
		//number of files
		superblock.format(files); //I don't think I can just reread from disk?
		
		//2. With the new superblock, recreate the directory just as it was made in the constructor
		directory = new Directory(superblock.totalInodes);		

		//3. With the new superblock, recreate the filetable just as it was made in the constructor
		filetable = new FileTable(directory);
		
		return true;
	}
	
	//Get me a FileTableEntry to give to the caller which represents the fileName given
	//Layered calls: open -> filetable.falloc() -> directory.namei/ialloc
	FileTableEntry open(String fileName, String mode)
	{
		if (!modeValid(mode))
		{
			return null;
		}
		
		//Take fileName and mode and use the FileTable's interface with the directory to
		//get me an FTE
		FileTableEntry FTE = filetable.falloc(fileName, mode);

		if(mode.equals("w")) {
			boolean deallocSuccess = deallocAllBlocks(FTE);
			if(!deallocSuccess) {
				return null;
			}
		}

		//Check if the FileTable was able to use the Directory to map the fileName to an INode
		if (FTE == null)
		{
			return null;
		}
		//If it was, return the FileTableEntry
		else
		{
			return FTE;
		}
		
	}

	//Just close the filetableEntry that I'm given.
	boolean close(FileTableEntry FTE)
	{
		//Since this is layered architecture, I can just hand down the task to the guy below me.
		boolean delegate = filetable.ffree(FTE);
		return delegate;
	}
	
	//Just return the size with an error check
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

	//Read some byte[] from a file represented by a FTE
	//FileSystem.Read() is used to get the byte at offset X. In this case,
	//The INode for the file takes this offset and maps to a specific block which is then read.
	//Layer calls: call up to kernel to use rawread
	//			   call down to Inode.mapoffset(),
	//			   		through a call sideways to FTE.inode
		synchronized int read(FileTableEntry FTE, byte[] buffer)
	{
		//As we are meant to code defensively, I should check that the mode is 'read'
		if (FTE.mode != "r")
		{
			return -1;
		}
		if (FTE == null || buffer == null)
		{
			return -1;
		}

		//Each loop iteration represents 512B being read into the data buffer
		int sentinel = 0;
		while (sentinel < buffer.length)
		{
			//Get Inode Block # - the offset is given by the seek pointer in the FTE
			int blockIndex = FTE.inode.mapOffset(FTE.seekPtr);
			
			//Error occurs
			if (blockIndex <= 0)
			{
				return -1;
			}
			
			//Temporary array to read 512B from the file
			byte[] tmpRead;
			
			//Default increment value of the sentinel
			int increment = 512;
			
			//Before I read, make sure I'm not going past the eof character
			if ((buffer.length - sentinel) < 512)
			{
				increment = buffer.length - sentinel;
				tmpRead = new byte[buffer.length - sentinel];
			}
			else
			{
				tmpRead = new byte[512];
			}

			//Actual read command
			SysLib.rawread(blockIndex, tmpRead);
			
			//Move the array of read byte data into buffer
			for (int i = sentinel; i < (sentinel + increment); i++)
			{
				buffer[i] = tmpRead[i];
			}


			//Block size = 512B (see disk.java)
			sentinel += increment;
			FTE.seekPtr += increment;	//Increment seek pointer
		}
		
		//Return the # of bytes read
		return sentinel;
	}



	//Write some byte[] buffer into a file represented by an FTE
	//Layer calls: call up to kernel to use rawwrite
	//			   call down to Inode.mapOffset and other methods in Inode,
	//			   		through a call sideways to the FTE.inode
	int write(FileTableEntry FTE, byte[] buffer)
	{
		//As we are meant to code defensively, I should check that the mode is 'write/+'
		if (FTE.mode != "w" && FTE.mode != "w+")
		{
			return -1;
		}
		if (FTE == null || buffer == null)
		{
			return -1;
		}

		//The current block to be writing to. Depending on the size of the write, we may need more than 1.
		//THIS IS NECESSARY to pass tests where a large amount of byte data is written! Note that this is the
		//block's number, not the block index that we map to within the loop!!
		int currentBlock = FTE.seekPtr;

		//The size to write
		int writeSize = buffer.length;


		//Each loop iteration represents a write of a set 512B size or less
		int LeftSentinel = 0;
		while (LeftSentinel < writeSize)
		{
			//We're given an FTE who is all set up to be used to find any Inode data from it. This means that
			//the seek pointer has already been sought to the block that has the file data to write into for iteration 0
			//and for any subsequent iterations, the current block has been incremented and must be used to map the new
			//offset
			int blockIdx = FTE.inode.mapOffset(currentBlock);

			//If we need to, let's go ahead and get a new free block for iteration 0.
			if (blockIdx < 0 )
			{
				blockIdx = superblock.getFreeBlock();
			}

			//Within the loop, one last task to do is to increment by the amount read which is typically 512B
			//Increment = 512B by default; but can be less
			int increment = 512;

			//Increment the sentinel and the current block if needed
			//Case: there's less than 512B to write, so I've got to write a partial block
			if ((LeftSentinel + increment) > writeSize)
			{
				//Temporary write array used once per loop
				byte[] tmpWrite = new byte[increment];
				byte[] tmpWrite2 = new byte[increment];
				//The read statement seems out of place in the write method, but I actually need it to 'fetch' me
				//the block data into tmpWrite - read an entire block as usual, but we'll overwrite part of it
				SysLib.rawread(blockIdx, tmpWrite);

				for (int i = 0; i < writeSize - LeftSentinel; i++)
				{
					tmpWrite[i] = buffer[LeftSentinel + i];
				}


				for (int i = 0; i < writeSize - LeftSentinel; i++)
				{
					tmpWrite2[i] = tmpWrite[i];
				}

				SysLib.rawwrite(blockIdx, tmpWrite2);


				LeftSentinel = writeSize;
			}
			//Case: there's more than 512B to write, so just write an entire block
			else
			{
				//Temporary write array used once per loop
				byte[] tmpWrite = new byte[increment];
				//The read statement seems out of place in the write method, but I actually need it to 'fetch' me
				//the block data into tmpWrite
				SysLib.rawread(blockIdx, tmpWrite);

				//Write the 512 bytes from buffer into a tmpWrite array so that I can actually use rawwrite without
				//screwing up and trying to write more than 512B into a 512B block

				for (int i = 0; i < increment; i++)
				{
					tmpWrite[i] = buffer[LeftSentinel + i];
				}

				//The actual entire goal of the function is acocmplished!!!
				SysLib.rawwrite(blockIdx, tmpWrite);

				//Increment by x Bytes
				LeftSentinel += increment;
				currentBlock += increment;
			}
		}


		//The only time I need to explicitly modify the Inode. To preserve layered architecture, I use the
		//inode.updateLength()
		FTE.inode.setLength(writeSize + FTE.inode.length);

		//Return the updated iNode length because this is the original length + the written bytes
		return FTE.inode.length;
	}

	//
	private boolean deallocAllBlocks(FileTableEntry fte)
	{
		if(fte != null) {
			fte.seekPtr = 0;
			for(int i = 0; i < fte.inode.direct.length; i++) {
				superblock.returnBlock(fte.inode.direct[i]);
			}
			fte.inode.length = 0;
			fte.inode.count = 0;
			fte.inode.flag = 0;
			for(int i = 0; i < 11; i++) {
				fte.inode.direct[i] = 0;
			}
			return true;
		} else {
			return false;
		}
	}
	
	//To be implemented
	int seek(FileTableEntry fte, int offset, int whence)
	{
		final int SEEK_SET = 0;
		final int SEEK_CUR = 1;
		final int SEEK_END = 2;

		int fileSize = fsize( fte );
		if( whence == SEEK_SET )
			fte.seekPtr = offset;
		else if( whence == SEEK_CUR )
			fte.seekPtr += offset;
		else if( whence == SEEK_END )
			fte.seekPtr = fileSize + offset;
		else
			return -1;
		if( fte.seekPtr < 0 )
			fte.seekPtr = 0;
		if( fte.seekPtr > fileSize )
			fte.seekPtr = fileSize;
		return 0;
	}

	boolean delete(String fileName) {
		short nameiNum = directory.namei(fileName);
		return nameiNum != -1 && directory.ifree(nameiNum);
	}

	//Helper check method to ensure that the mode is valid
	boolean modeValid(String mode)
	{
		if (mode == "r" || mode == "w" || mode == "a" | mode == "w+")
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
