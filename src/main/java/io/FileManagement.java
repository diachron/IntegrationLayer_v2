package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManagement
{
    public String getFileMD5Hash(String filename)
    throws NoSuchAlgorithmException, FileNotFoundException, IOException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(filename);
 
        byte[] dataBytes = new byte[1024];
 
        int nread = 0; 
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        };
        
        byte[] mdbytes = md.digest();
 
        //convert the byte to hex format method 1
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++)
        {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        StringBuffer hexString = new StringBuffer();
        
    	for (int i=0;i<mdbytes.length;i++)
        {
    		String hex=Integer.toHexString(0xff & mdbytes[i]);
   	     	if(hex.length()==1) hexString.append('0');
   	     	hexString.append(hex);
    	}
    	return hexString.toString();
    }
       
    public static void deleteFile(String filename)
    {
   	try
        {
    		File file = new File(filename);
        	
    		if(file.delete())
                {
                    System.out.println(file.getName() + " is deleted!");
    		}
                else
                {
                    System.out.println("Delete operation is failed.");
    		}   	   
    	}
        catch(Exception e)
        {    		
            e.printStackTrace();
    	}        
    }
    
    public static void storeFile(InputStream inputStream, String filename)
    {
	OutputStream outputStream = null;        
	try {
		outputStream = 
                    new FileOutputStream(new File(filename));
 
		int read = 0;
		byte[] bytes = new byte[1024];
 
		while ((read = inputStream.read(bytes)) != -1) {
			outputStream.write(bytes, 0, read);
		}
 
		System.out.println("Done!");
 
	} catch (IOException e) {
		e.printStackTrace();
	} finally {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
 
		}
	}        
    }
    
    public static void main(String[] args)
    {
        FileManagement f = new FileManagement();
        try {
            System.out.println(f.getFileMD5Hash("test"));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FileManagement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
