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

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileHelper;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FtpClient;

public class FtpObserver 
{	
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
				ftpConnection.downloadFile( newestFtpFile, localDir.getPath() + '\\' + newestFtpFile );
				/* indicate a file was downloaded */
				returnVal = true;
			}
		}
		
		return returnVal;
	}
	
	/*  
	 * Get the file list on the FTP server and return the newest file name
	 */
	private String getNewestFileOnFTP() throws IOException
	{
		Collection<String> files;
		String newestFileName = null;
		
		/* get the file list of the current directory */
		files = ftpConnection.listFiles("");
		
		/* check all the files */
		for( String str: files )
		{
			/* check the file name is in the correct format */
			if( FileHelper.isValidChromName(str) )
			{
				/* check if its a newer file */
				if( newestFileName == null || FileHelper.isNewerFileName(newestFileName, str) )
				{
					newestFileName = str;
				}
			}
		}
		
		return newestFileName;
	}
}
