/*******************************************************************************
 * Copyright (c) 2018 oceancerc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * oceancerc - initial API and implementation
*******************************************************************************/
package net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.chemclipse.logging.core.Logger;

public class FileHelper
{
	private static final Logger logger = Logger.getLogger(FileHelper.class);
	
	private static final String FILE_REGEX = 	"\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_chromatogram.xy";
	private static final String FOLDER_REGEX = 	"\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}";
	
	private static final String DATE_FORMAT = 	"yyyy_MM_dd_HH_mm_ss";
	
	/*  
	 * checks whether newFile is newer then oldFile based on the file name
	 * 
	 * oldFile: the file to check against (null permitted)
	 * newFile: the file being checked against (null permitted)
	 * 
	 * return: true if newFile is newer then oldFile, false otherwise
	 */
	public static boolean isNewerFile(File oldFile, File newFile)
	{		
		boolean returnVal;
		
		/* check if old file is null */
		if(oldFile == null)
		{
			returnVal = false;
		}
		/* check if new file is null */
		else if(newFile == null)
		{
			returnVal = true;
		}
		/* check if new file is newer then old file based on the file name */
		else
		{
			returnVal = isNewerFileName(oldFile.getName(), newFile.getName());
		}
		
		/* return the results */
		return returnVal;
	}
	
	public static boolean isNewerFileName(String oldFile, String newFile)
	{
		return isNewerString(oldFile, newFile, FILE_REGEX);
	}
	
	public static boolean isNewerFolderName(String oldFolder, String newFolder)
	{
		return isNewerString(oldFolder, newFolder, FOLDER_REGEX);
	}
	
	private static boolean isNewerString(String oldStr, String newStr, String regexCheck)
	{
		boolean returnVal;
		
		/* move both string to the last '/' */
		oldStr = getFileNameFromPathName(oldStr);
		newStr = getFileNameFromPathName(newStr);

		/* new string is valid */
		if( newStr == null || !newStr.matches(regexCheck) )
		{
			returnVal = false;
		}
		/* check if old string is valid */
		else if( oldStr == null || !oldStr.matches(regexCheck) )
		{
			returnVal = true;
		}
		/* check if new string is newer then old string */
		else
		{
			returnVal = isNewerDateString(oldStr, newStr);
		}
		
		/* return the results */
		return returnVal;
	}
	
	private static boolean isNewerDateString(String oldDateStr, String newDateStr )
	{
		SimpleDateFormat parser;
		Date oldDate, newDate;
		
		boolean returnVal = false;
		
		/* get the date strings */
		oldDateStr = oldDateStr.substring(0,19);
		newDateStr = newDateStr.substring(0,19);
		
		/* initialize the date parser */
		parser = new SimpleDateFormat(DATE_FORMAT);
		
		try 
		{
			/* parse the strings */
			oldDate = parser.parse(oldDateStr);
			newDate = parser.parse(newDateStr);
			
			/* check if it is newer */
			returnVal = newDate.after(oldDate);
		}
		catch(ParseException e)
		{
			logger.warn(e);
		}
		
		return returnVal;
	}
	
	public static String getFileNameFromPathName(String pathName)
	{
		String fileName = null;
		
		if( pathName != null )
		{
			fileName = Paths.get(pathName).getFileName().toString();
		}
		
		return fileName;
	}
	
	public static String getFolderNameFromPath(String pathName)
	{
		String folderName = null;
		
		if(pathName != null)
		{
			folderName = Paths.get(pathName).getParent().getFileName().toString();
		}

		return folderName;
	}
	
	public static boolean isValidChromName(String str)
	{
		return str.matches(FILE_REGEX);
	}
	
	public static boolean isValidChromFolderName(String str)
	{
		return str.matches(FOLDER_REGEX);
	}
	
	public static boolean isValidFile(String file)
	{
		String ext = getExtension(file);
		
		return ext.equals("txt") || ext.equals("xy") || ext.equals("ini");
	}
	
	public static File getChromFromFolder(String chromDirStr)
	{
		File chromDir;
		File chrom = null;
		
		chromDir = new File(chromDirStr);
		
		for( File file : chromDir.listFiles() )
		{
			if(isValidChromName(file.getName()))
			{
				chrom = file;
				break;
			}
		}
		
			
		return chrom;
	}
	
	
	private static String getExtension(String fileName)
	{
		int i;
		String ext = "";
		
		i = fileName.lastIndexOf('.');
		
		if (i > 0)
		{
		    ext = fileName.substring(i+1);
		}
		
		return ext;
	}
	
}
