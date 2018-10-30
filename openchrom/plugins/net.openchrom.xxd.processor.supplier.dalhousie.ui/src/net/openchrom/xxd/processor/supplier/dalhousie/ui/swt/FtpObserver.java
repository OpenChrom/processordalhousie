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
package net.openchrom.xxd.processor.supplier.dalhousie.ui.swt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.chemclipse.logging.core.Logger;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileHelper;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FtpClient;

public class FtpObserver 
{	
	private static final Logger logger = Logger.getLogger(FtpObserver.class);
	
	private FtpClient ftpConnection;
	
	/* constructor */
	public FtpObserver()
	{
		this.ftpConnection = new FtpClient();
	}
	
	public void open() throws IOException
	{
		/* Setup the FTP configuration */
		ftpConnection.setServer(PreferenceSupplier.getFtpServer());
		ftpConnection.setUser(PreferenceSupplier.getFtpUser());
		ftpConnection.setPort(PreferenceSupplier.getFtpPort());
		ftpConnection.setPassword(PreferenceSupplier.getFtpPass());
		
		/* open the FTP connection and move to the directory */
		ftpConnection.open();
		ftpConnection.changeDir(PreferenceSupplier.getFtpDir());
		
	}
	
	public void close() throws IOException
	{
		/* close the FTP connection */
		ftpConnection.close();
	}
	
	
	/*
	 * checks if there is a newer file on the FTP server, if there is it downloads it
	 */
	public boolean downloadNewFtpFiles(File localDir, File currentChrom) throws IOException
	{
		String newestFtpFile;
		boolean returnVal = false;

		/* get the newest file name from FTP */
		newestFtpFile = getNewestFileOnFTP();
		
		/* check if there is a file */
		if(newestFtpFile != null)
		{			
			/* check if the server has a newer file */
			if( currentChrom == null  || FileHelper.isNewerFileName( currentChrom.getName(), newestFtpFile ) )
			{
				/* if it does download it */
				ftpConnection.downloadFile( newestFtpFile, localDir.getPath() + '\\' + FileHelper.getFileNameFromPathName(newestFtpFile) );
				/* indicate a file was downloaded */
				returnVal = true;
			}
		}
		
		return returnVal;
	}
	
	/*  
	 * Get the file list on the FTP server and return the newest file name
	 * The Chromatogram files on the FTP server are all in a folder with a date stamp as the tile
	 */
	private String getNewestFileOnFTP() throws IOException
	{
		Collection<String> files;
		String newestFolderName = null;
		String newestChromName = null, chromInFolder;
		
		/* get the file list of the current directory */
		files = ftpConnection.listFiles();
		
		/* check all the files and folders */
		for( String str: files )
		{
			/* check the file or folder name is a valid chromatogram folder */
			if( FileHelper.isValidChromFolderName(str) )
			{
				/* check if its a newer folder */
				if( newestFolderName == null || FileHelper.isNewerFolderName(newestFolderName, str) )
				{
					/* get the chromatogram file in the folder */
					chromInFolder = getChromFromFolder(str);
					
					/* check if it had a valid chromatogram in it */
					if( chromInFolder != null )
					{
						newestFolderName = str;
						newestChromName = newestFolderName + "/" + chromInFolder;
					}
				}
			}
		}
		
		return newestChromName;
	}
	
	private String getChromFromFolder(String folderName)
	{
		Collection<String> files;
		String chromName = null;

		try 
		{
			/* move into directory */
			ftpConnection.changeDir(folderName);
			/* list all files in the directory */
			files = ftpConnection.listFiles();
			
			/* check all files for a valid chromatogram */
			for(String file : files)
			{
				/* check if file is valid */
				if( FileHelper.isValidChromName(file) )
				{
					/* If there is one return it */
					chromName = file;
					break;
				}
			}
			
			/* move back out of the directory */
			ftpConnection.changeDir("..");
		}
		catch(IOException e)
		{
			logger.warn(e);
		}
		
		return chromName;
	}
}
