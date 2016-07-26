//Directory.Java
//Jacob J. Parkinson
//Notes: initial code given by instructor / instructor videos

public class Directory
{
	//Directory stores information about each file

	//Max characters in each file name
	private static int maxChars = 30;

	//Directory entries
	private int size[];			// each element stores a different file size.
	private char fileName[][];		// each element stores a different file name.

	//Constructor (1-arg)
	public Directory(int maxInumber)
	{
		//Allocate the array with the argument provided; this is the maximum file number
		size = new int[maxInumber];

		for (int i = 0; i < maxInumber; i++)
		{
			size[i] = 0;
		}
		fileName = new char[maxInumber][maxChars];
		String root = "/";
		
		size[0] = root.length();
		root.getChars(0, size[0], fileName[0], 0);
	}
	
	public int bytesToDirectory(byte[] data)
	{
		//Assumes data[] received directory information from disk
		//Initializes the directory instance with this data[]

		//The difficulty with converting raw byte data to a char[][] is knowing where each file ends
		//Luckily, there is a SysLib method to help: bytes2int(byte[] b, int offset) converts b[o],b[o+1],b[o+2],b[o+3] 
		//into an int and returns it
		
		//Set the size equivalent to byte[].length and then loop through the fileName array, setting equivalent to
		//the byte[] data. Make sure to use something to keep track of where each file name provided in the byte[] ends
		for (int i = 0; i < data.length; i++)
		{
			//An integer is 32bits in Java (4 bytes). The second argument is the offset of the previous conversion.
			//Converting 4B each time gives an offset of i*4B
			size[i] = SysLib.bytes2int(data, i*4);
		
			//Now fill the fileName char[][] with the data
			//Java internally stores its characters as TWO BYTES.
			for (int j = 0; j < (data.length >> 1); j++)
			{
				int bpos = j << 1;
				fileName[i][j] = (char)(((data[bpos]&0x00FF)<<8) + (data[bpos + 1]&0x00FF));	
			}

		}

		//Resultant structure:
		//size[0] = data[0...3]
		//size[1] = data[4...7]
		//...
		int checkSum = 0;
		for (int i = 0; i < size.length; i++)
		{
			checkSum += size[i];
		}
		
		if (data.length == checkSum)
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}

	public byte[] directoryToBytes()
	{
		//Loop through file names
		//Convert directories filename to bytes
		//Convert directories' filesize to bytes
		//Return directory info as byte array
		return new byte[10];
	}

	public short ialloc(String fileName)
	{
		//Loop through filenames
		//First one that's empty will allocate filaneme/filesize
		//Return the location
		return 0;
	}

	public boolean ifree(String fileName)
	{
		//Loop through all filenames at iNumber
		//Deallocate filename at iNumber
		//Delete the file and filesize
		return false;
	}

	public short namei(String fileName)
	{
		//Loop through maximum number of filenames
		//until finding filename
		//return that i node number
		return 0;
	}

}