//Directory.Java
//Team (Group 6): Jacob J. Parkinson, Duke Dynda, Fuli Lan, Nicolas Koudsieh
//Notes: initial code given by instructor / instructor videos
//FINAL Version
//Directory stores information about each file
public class Directory
{

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

        //size[0] = root.length();
        //root.getChars(0, size[0], fileName[0], 0);
    }

    //Take the byte[] and enter them into the directory
    public int bytes2directory(byte[] data)
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

    //Output the directory's file names as a byte[]
    public byte[] directory2bytes()
    {
        int offset = 0;
        // create a new array
        byte[] arr = new byte[4* size.length + (size.length * maxChars * 2 )];

        //Loop through file names
        for (int i = 0; i < size.length; i++)
        {
            SysLib.int2bytes(size[i], arr, offset);
            offset += 4;
        }
        //Convert directories filename to bytes
        for (int i = 0; i <fileName.length; i++)
        {
            for (int j = 0; j < size[i]; j++)
            {
                //convert directories'filesize to bytes
                offset++;
                arr[offset] = (byte)fileName[i][j];
            }
        }

        //Return directory info as byte array
        return arr;
    }


    //Give a free spot for a file in the directory
    public short ialloc(String fname)
    {
        //Loop through the size array
        for (short i = 0; i < size.length; i++)
        {
            //First one that's empty will allocate filaneme/filesize
            if (size[i] == 0)       //If size at this slot is zero, allocate a new inode number for this filename
            {
                int minSize;
                if (fname.length() <= maxChars)     //if length of the filename is less or equal than 30, use its length
                {
                    minSize = fname.length();
                }
                else
                {
                    minSize = maxChars;
                }
                size[i] = minSize;      //Set length at slot 'i' equal to the minimum size
                fname.getChars(0, minSize, fileName[i], 0);     //Copy the filename string into the file name array

                //Return the location
                return i;       //return the array index
            }

        }
        return -1;      //else return -1 for error
    }

    //Takes an index rather than a name, avoiding the need for any loop
    public boolean ifree(short iNumber)
    {
        if (iNumber >= size.length || iNumber < 0 || size[iNumber] == 0 || fileName[iNumber].length == 0)
        {
            return false;
        }

        //Delete the file and filesize
        size[iNumber] = 0;

        return true;
    }

    //Very simple method to map a filename to an inode #
    public short namei(String fname)
    {
        //Default return error
        short ret = -1;

        //Loop through maximum number of filenames
        for (int i = 0; i < size.length; i++)
        {
            //Typical Java string manipulation sequence
            String tmp = new String (fileName[i], 0, size[i]);

            //until finding filename
            if (tmp.equalsIgnoreCase(fname))
            {
                ret = (short)i;
            }
        }

        //return that i node number
        return ret;
    }


}
