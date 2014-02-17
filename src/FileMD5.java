import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import joptsimple.OptionParser;
import joptsimple.OptionSet;


public class FileMD5 {
//	private static Logger Log = Logger.getLogger(FileMD5.class.getClass().getName());
	static final String MD5_FILENAME = "md5.txt";
	static final String SLASH = System.getProperty("file.separator");
	
	public static void main(String[] args) {
		ArrayList<String> alLocation = new ArrayList<String>();;
		
        OptionSet options = checkOptions(args);
        if (options == null) {return;};

	    if (options.has("d") && options.hasArgument("d")) { // get directories in the param
	      String val = (String)options.valueOf("d");
	      alLocation = getDirArg(val);	
	    } else {											// current directory
	      String val = ".";
	      alLocation.add(val);
	    }
			        
	    if (options.has("w")) {
	    	writeMD5(alLocation);
	    }
	    
	    if (options.has("f") && options.hasArgument("f")) {
	    	String md5 = (String)options.valueOf("f");
		    if (md5.length() > 0) {
		    	findFilewithMD5(md5, alLocation);
		    }
	    }
	    if (options.has("m") && options.hasArgument("m")) {
	    	String val = (String)options.valueOf("m");
	    	getMD5ofFile(val,options.has("l"));	    	
	    }
	    if (options.has("p")) {
	    	findDuplicate(alLocation, options.has("o"));
	    }
	}
	
	private static void findDuplicate(ArrayList alLocation, Boolean showOnlyDup) {
		Iterator<String> it = alLocation.iterator();
		HashMap<String,String> hmMD5Fn = new HashMap<String,String>();

    	try {
    		while(it.hasNext()) {
    			Boolean found = false;
	    	    String loc = it.next();

	    	    System.out.println("Opening " + loc + SLASH + MD5_FILENAME);
	    	    File inFile = new File(loc +SLASH + MD5_FILENAME);
				BufferedReader reader = new BufferedReader(new FileReader(inFile));
				String line;
				while ((line = reader.readLine()) != null) {					
					String[] token = line.split("=");
					String val = hmMD5Fn.get(token[1]);
					if ((val != null) && (val.length() > 0)) {
						val = val + ";" + loc + SLASH + token[0]; 
					} else {
						val = loc + SLASH + token[0];
					}
					hmMD5Fn.put(token[1], val);
				}
				reader.close();
    		} // while
    		
    		System.out.println(showOnlyDup);
    		int cnt = 0;
    		for (Map.Entry entry: hmMD5Fn.entrySet()) {
    			String val = (String) entry.getValue();
    			if (showOnlyDup) {
    				String[] token = val.split(";");
    				
//    				System.out.println(val + " " + token.length);
    				if (token.length > 1) {
    					cnt++;
        				System.out.println(cnt+") "+entry.getKey() + " " + val);
    				}
    			} else {
    				System.out.println(cnt+") "+entry.getKey() + " " + val);
    			}
    		}
    		
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

	}
	
	private static void getMD5ofFile(String fn, Boolean live) {
		Boolean found = false;
        String filename = FilenameUtils.getName(fn);	// filename + extension
        String fullPath = FilenameUtils.getFullPath(fn); // Has trailing slash
                
		try {
			if (live) {
				String md5 = filemd5(fn);
				if (md5.length() > 0) {
					System.out.println(fn + " = " + md5);
				}
			} else {
				File inFile = new File(fullPath + MD5_FILENAME);
				BufferedReader reader = new BufferedReader(new FileReader(inFile));
				String line;
				while ((line = reader.readLine()) != null) {					
					String[] token = line.split("=");
					if (fn.compareTo(token[1]) == 0) {
						System.out.println(line);
						found = true;
					}
				}
				reader.close();

				if (found == false) {
					System.out.println("MD5 signature not found in " + MD5_FILENAME);
				}
			}
		} catch(IOException ioError) {
	      System.out.println("Cant find " + fullPath + MD5_FILENAME); 
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static void findFilewithMD5(String md5, ArrayList alLocation) {
		Iterator<String> it = alLocation.iterator();

    	try {
    		while(it.hasNext()) {
    			Boolean found = false;
	    	    String loc = it.next();
    			File inFile = new File(loc+SLASH+ MD5_FILENAME);
    			BufferedReader reader = new BufferedReader(new FileReader(inFile));
    			String line;
    			while ((line = reader.readLine()) != null) {					
    				String[] token = line.split("=");
    				if (md5.compareTo(token[1]) == 0) {
    					System.out.println(loc + SLASH + line);
    					String liveMD5 = filemd5(loc + SLASH + token[0]);
    					found = true;
    				}
    			}
    			reader.close();
    			if (found == false) {
    				System.out.println("MD5 signature not found in " + loc + SLASH + MD5_FILENAME);
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
		
	}
	private static void writeMD5(ArrayList alLocation) {
	    Iterator<String> it = alLocation.iterator();  	  

    	try {
	    	  while(it.hasNext()) {
	    	    String loc = it.next();
	    	    System.out.println("Writing " + loc + SLASH + MD5_FILENAME);

	    	    File outFile = new File(loc+SLASH + MD5_FILENAME);
	    		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

	    		File dir = new File(loc);
	    		File[] filesList = dir.listFiles();
	    		for (File file : filesList) {
	    			if (file.getName().equals(MD5_FILENAME)) {
	    				continue;
	    			}
	    			if (file.isFile()) {
	    				String md5 = filemd5(loc+SLASH+file.getName());
	    				writer.write(file.getName()+"="+md5+"\n");
	    			}
	    		}
	    		writer.close();
	    	  }
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    	return;

	}
	private static ArrayList<String> getDirArg(String val) {
		ArrayList<String> al = new ArrayList<String>();
		String[] loc = val.split(";");
		for (int i=0; i < loc.length; i++) {
			al.add(loc[i]);
//			Log.info("Adding directory ["+loc[i]+"]");
		}
		return al;
	}
	
	private static String filemd5 (String fn) {
		String md5Hex = "";
		File file = null;

		file = new File(fn);
		try {
		  HashCode md5 = Files.hash(file, Hashing.md5());

		  byte[] md5Bytes = md5.asBytes();
		  md5Hex = md5.toString();
		  		  
		} catch (IOException e) {
			System.out.println("Cant find file.");
		}
		finally {
			
		}

		return md5Hex;
	}
	private static OptionSet checkOptions(String[] args) {		
	    if (args.length <= 0) {
	    	System.out.println("Need argument");
        	usage();
	    	return null;
	    }
	    
        OptionParser parser = new OptionParser( "owlpd:f:m:d:" );
        OptionSet options = null;
        
        try {
        	options = parser.parse(args);
        } catch (Exception e){
    		System.out.println();
        	System.out.println("Invalid Option");
    		System.out.println();
        	usage();
    		System.out.println();
        }   

	    return options;
	}
	private static void usage() {
		System.out.println("Usage:");
		System.out.println(" fileMD5 -m [-l]                 -> Get MD5 of filename");
		System.out.println("                                 -> -l from live filesystem");
		System.out.println();

		System.out.println(" fileMD5 -w [-d dir;dir;dir]     -> Write MD5 files");
		System.out.println("                                 -> -d = list of directory");
		System.out.println();
		
		System.out.println(" fileMD5 -f md5 [-d dir;dir;dir] -> Find file with MD5 signature");
		System.out.println("                                 -> -d = list of directory");
		System.out.println();

		System.out.println(" fileMD5 -p [-o] [-d dir;dir;dir]-> Find duplicate files");
		System.out.println("                                 -> -o = Only show duplicates");
		System.out.println("                                 -> -d = list of directory");
		System.out.println();
	}

}

// fileMD5 -f md5				-> Search in MD5.txt for a filename with given md5
// fileMD5 -f md5 -d "a;b;c"	-> Search in MD5.txt for a filename with given md5 in dir a, b and c
// fileMD5 -p -d "a;b;c" -o		-> Find duplicate in dir "a;b;c", show only dups
