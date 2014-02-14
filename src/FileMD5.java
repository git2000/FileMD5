import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import joptsimple.OptionParser;
import joptsimple.OptionSet;


public class FileMD5 {

	public static void main(String[] args) {
//	    System.out.println("Doing It");
	    
	    if (args.length <= 0) {
	    	System.out.println("Need argument");
	    	return;
	    }
	    
        OptionSet options = null;

		options = checkOptions(args);

/*		if (options.has("w")) {
			System.out.println("Has w");
		}
    	if (options.has("f") && options.hasArgument("f")) {
			System.out.println("Has f");

    	}
*/
			        
	    if (args[0].compareTo("-w") == 0) {
	    	try {
	    		File outFile = new File("md5.txt");
	    		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

//	    		File dir = new File("c:\\temp");
	    		File dir = new File(".");
	    		File[] filesList = dir.listFiles();
	    		for (File file : filesList) {
	    			if (file.isFile()) {
	    				String md5 = filemd5(file.getName());
	    				System.out.println(file.getName() + "=" + md5);
	    				writer.write(file.getName()+"="+md5+"\n");
	    			}
	    		}
	    		writer.close();
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	return;
	    }
	    if (args[0].compareTo("-f") == 0) {
		    String md5 = filemd5(args[1]);
		    if (md5.length() > 0) {
		    	System.out.println(args[1] + " = " + md5);
		    }
	    }
	    if (args[0].compareTo("-m") == 0) {
	    	Boolean found = false;
	    	try {
	    		File inFile = new File("md5.txt");
	    		BufferedReader reader = new BufferedReader(new FileReader(inFile));
	    		String line;
				while ((line = reader.readLine()) != null) {
//					System.out.println(line);
					
					String[] token = line.split("=");
//					System.out.println(token[0] + " ... " + token[1]);
					if (args[1].compareTo(token[1]) == 0) {
						System.out.println(line);
						found = true;
					}
				}
				reader.close();
				if (found == false) {
					System.out.println("MD5 signature not found in md5.txt");
				}
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }

	    
	}

	private static String filemd5 (String fn) {
		String md5Hex = "";
		File file = null;

		file = new File(fn);
		try {
		  HashCode md5 = Files.hash(file, Hashing.md5());

		  byte[] md5Bytes = md5.asBytes();
		  md5Hex = md5.toString();
		  
//		  System.out.println(md5Hex);
		  
//		  long l = file.length();
//		  System.out.println("File Size: " + l);
		  
		} catch (IOException e) {
			System.out.println("Cant find file.");
		}
		finally {
			
		}

		
		return md5Hex;
	}
	private static OptionSet checkOptions(String[] args) {
        OptionParser parser = new OptionParser( "f:m:w" );
        OptionSet options = null;
        try {
        	options = parser.parse(args);
        } catch (Exception e){
        	
        }   
        return options;
	}

}

// fileMD5 -m filename			-> Get md5 of filename
// fileMD5 -w 					-> Write md5.txt
// fileMD5 -f md5				-> Get filename of md5, if any
