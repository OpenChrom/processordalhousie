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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.eclipse.chemclipse.logging.core.Logger;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileHelper;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FtpClient;

public class FtpObserver 
{	
	private static final String OC_SETTINGS_FILE_NAME = "oc_settings.txt";
	
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
		ftpConnection.setServer(PreferenceSupplier.getServer());
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
	public String downloadNewFtpFiles(File localDir, File currentChrom) throws IOException
	{
		String newestFtpFile;
		String localChromDir = null;

		/* get the newest file name from FTP */
		newestFtpFile = getNewestFileOnFTP();
		
		/* check if there is a file */
		if(newestFtpFile != null)
		{
			/* check if the server has a newer file */
			if( currentChrom == null  || FileHelper.isNewerFileName( currentChrom.getName(), newestFtpFile ) )
			{
				/* set the local directory to download files into */
				localChromDir = localDir.getPath() + "\\" + FileHelper.getFolderNameFromPath(newestFtpFile);
				
				/* if it does download it */
				downloadFtpDirectory( FileHelper.getFolderNameFromPath(newestFtpFile),  localChromDir);
				
				/* Save the local setting to the directory */
				saveOcSettings( localChromDir );
			}
		}

		return localChromDir;
	}
	
	private void downloadFtpDirectory(String ftpDir, String localDir) throws IOException
	{
		Collection<String> fileList;
		
		/* move into directory */
		ftpConnection.changeDir(ftpDir);
		
		/* list files in the directory */
		fileList = ftpConnection.listFiles();
		
		/* create the directory in the local filesystem */
		new File(localDir).mkdirs();
		
		/* download all the files in the directory */
		for(String file : fileList)
		{
			if(FileHelper.isValidFile(file))
			{
				ftpConnection.downloadFile(file, localDir + "\\" + file);
			}
		}
		
		ftpConnection.changeDir("..");
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
		
		boolean doneFileFound = false;

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
				}
				else if(file.equals(".done"))
				{
					doneFileFound = true;
				}
			}
			
			/* move back out of the directory */
			ftpConnection.changeDir("..");
		}
		catch(IOException e)
		{
			logger.warn(e);
		}
		
		/* check if a done file was found to validate the chromatogram */
		if(!doneFileFound)
		{
			chromName = null;
		}
		
		return chromName;
	}
	
	private void saveOcSettings(String dirPath)
	{
		List<String> lines;
		Path file;

		lines = PreferenceSupplier.getAllSettings();
		
		file = Paths.get(dirPath + "\\" + OC_SETTINGS_FILE_NAME);
		
		try
		{
			Files.write(file, lines, Charset.forName("UTF-8"));
		}
		catch(IOException e)
		{
			logger.warn(e);
		}
	}
}
