//Inode.java
//Jacob J. Parkinson
//Initial code provided by Professor Michael Panitz

public class Inode
{

	private final static int iNodeSize = 32;		//fix to 32B
	private final static int directSize = 11;		//# direct ptrs
	
	public int length;					//file size in B
	public short count;					//# file-table entries pointing to this
	public short flag;					//0 = unused, 1 = used, ...
	public short direct[] = new short[directSize];	//direct ptrs
	public short indirect;				//a single indirect ptr

	//Default constructor
	Inode()
	{
		length = 0;
		count = 0;
		flag = 1;
		
		for (int i = 0; i < directSize; i++)
		{
			direct[i] = -1;
		}
		indirect = -1;
	}

	//retrieving the inode from the disk
	Inode (short iNumber)
	{
		//To be implemented!
	}

	//save to the disk as the i-th node
	int toDisk (short iNumber)
	{
		//To be implemented!
		return 0;
	}


}