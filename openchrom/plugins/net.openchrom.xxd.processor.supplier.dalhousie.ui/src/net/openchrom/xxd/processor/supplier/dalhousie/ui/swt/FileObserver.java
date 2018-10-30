/*******************************************************************************
 * Copyright (c) 2018 Lablicate GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 *
 * Dr. Philip Wenig - initial API and implementation
 *******************************************************************************/
package net.openchrom.xxd.processor.supplier.dalhousie.ui.swt;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.ux.extension.ui.provider.ISupplierEditorSupport;
import org.eclipse.swt.widgets.Display;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ftp.FtpClient;

public class FileObserver {

	private static final Logger logger = Logger.getLogger(MeasurementObserverUI.class);
	//
	private ISupplierEditorSupport supplierEditorSupport;
	private Thread thread;
	private FtpClient ftpConnection;
	
	private File currentChrom;

	public FileObserver(ISupplierEditorSupport supplierEditorSupport) {
		this.supplierEditorSupport = supplierEditorSupport;
		this.ftpConnection = new FtpClient();
	}

	public boolean isObservationRunning() {

		return thread != null;
	}

	public void toggleObservation() {

		if(isObservationRunning()) {
			stopObservation();
		} else {
			startObservation();
		}
	}
	
	/*
	 * stops the thread running the file observer
	 */
	public void stopObservation() 
	{
		if( thread != null )
		{
			/* close the FTP connection */
			try 
			{
				ftpConnection.close();
			} 
			catch(IOException e) 
			{
				logger.warn(e);
			}
			
			/* stop the thread */
			thread.interrupt();
			thread = null;
		}
	}

	/*
	 * Starts a thread that runs the file observer
	 * 
	 * returns: false if the directory in settings does not exist, true otherwise
	 */
	private boolean startObservation() 
	{
		File localFileDir;
		
		/* Open the file specified in settings */
		localFileDir = new File(PreferenceSupplier.getPathFiles());
		
		/* Check the file exists */
		if(!localFileDir.exists())
		{
			/* return a fail */ //TODO: warn user or throw an exception
			return false;
		}
		
		/* Setup the FTP configuration */
		ftpConnection.setServer(PreferenceSupplier.getFtpServer());
		ftpConnection.setUser(PreferenceSupplier.getFtpUser());
		ftpConnection.setPort(PreferenceSupplier.getFtpPort());
		ftpConnection.setPassword(PreferenceSupplier.getFtpPass());
		
		/* open the FTP connection and move to the directory */
		try 
		{
			ftpConnection.open();
			ftpConnection.changeDir(PreferenceSupplier.getFtpDir());
		}
		catch(IOException e) 
		{
			logger.warn(e); // TODO: alert user
			return false;
		}
		
		/* Create the thread to run the file observer */
		thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try 
				{
					runFileObserver( localFileDir, PreferenceSupplier.getRefreshRate() );
				} 
				catch(InterruptedException e) 
				{
					logger.warn(e);
				}
				
				try 
				{
					ftpConnection.close();
				}
				catch(IOException e)
				{
					logger.warn(e);
				}
			}
		});
		
		/* Start the thread */
		thread.start();
		
		/* return a pass */
		return true;
	}
	
	/* 
	 * Runs the file observer:
	 * - check for a new chromatogram, 
	 * - if there is one display it,
	 * - sleep for specified amount of time
	 * - repeat
	 */
	private void runFileObserver( File localFileDir, int refreshRate ) throws InterruptedException
	{
		File newChrom;
		currentChrom = null;
	
		while(true)
		{	
			/* if there is a newer file in the FTP server download it to the directory */
			try 
			{
				downloadNewFtpFiles(localFileDir);
			}
			catch(IOException e) 
			{
				logger.warn(e);// TODO: warn user, probably end thread or something
			}
			
			/* get the newest chromatogram in the directory, checked against the current one */
			newChrom = getNewestFile( localFileDir, currentChrom );
			
			/* check if a newer chromatogram was found */
			if( newChrom != null && newChrom != currentChrom )
			{
				/* open the newest chromatogram */
				openNewChrom(newChrom);
				
				/* set the current chromatogram to the one just opened */
				currentChrom = newChrom;
			}
			
			/* sleep for specified amount of time TODO: make a user setting for refresh frequency*/
			
			Thread.sleep( (long) refreshRate );
			
		}
	}
	
	/*
	 * checks if there is a newer file on the FTP server, if there is it downloads it
	 */
	private boolean downloadNewFtpFiles(File localDir) throws IOException
	{
		String newestFtpFile;
		File newestLocalFile;
		boolean returnVal = false;

		/* get the newest file name from FTP */
		newestFtpFile = getNewestFileOnFTP();
		
		/* check if there is a file */
		if(newestFtpFile != null)
		{
			/* get the newest file in the directory */
			newestLocalFile = getNewestFile( localDir, currentChrom );
			
			/* check if the server has a newer file */
			if( newestLocalFile == null  || isNewerFileName( newestLocalFile.getName(), newestFtpFile ) )
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
			if( isCorrectChromFileFormat(str) )
			{
				/* check if its a newer file */
				if( newestFileName == null || isNewerFileName(newestFileName, str) )
				{
					newestFileName = str;
				}
			}
		}
		
		return newestFileName;
	}
	
	/*
	 * Return the newest file in the directory
	 * 
	 * directory: the directory to search in
	 * newestFile: the current newest file (null permitted)
	 * 
	 * returns: the newest file in the directory (null if no file was found)
	 */
	private File getNewestFile(File directory, File newestFile)
	{
		/* check all the files in the directory */
		for(File file : directory.listFiles()) 
		{
			//TODO: check for directories, supplierEditorSupport.isSupplierFileDirectory(file)
			
			/* check the file is acceptable */
			if(supplierEditorSupport.isMatchMagicNumber(file) && supplierEditorSupport.isSupplierFile(file))
			{
				/* check if the file in newer */
				if(newestFile == null || isNewerFile( newestFile, file) )
				{
					newestFile = file;
				}
			}
		}
		
		/* return what was found */
		return newestFile;
	}
	
	/*  
	 * checks whether newFile is newer then oldFile based on the file name
	 * File name should be in format: yyyy_MM_dd_HH_mm_ss_chromatogram.xy
	 * 
	 * oldFile: the file to check against (null permitted)
	 * newFile: the file being checked against (null permitted)
	 * 
	 * return: true if newFile is newer then oldFile, false otherwise
	 */
	private boolean isNewerFile(File oldFile, File newFile)
	{		
		boolean returnVal;
		
		/* check if they are both null */
		if(oldFile == null && newFile == null)
		{
			/* its not newer */
			returnVal = false;
		}
		/* check if only the old file is null */
		else if(oldFile == null)
		{
			/* it is newer */
			returnVal = true;
		}
		/* if the new file is null */
		else if(newFile == null)
		{
			/* its not newer */
			returnVal = false;
		}
		/* check if new file is newer then old file based on the file name */
		else
		{
			returnVal = isNewerFileName(oldFile.getName(), newFile.getName());
		}
		
		/* return the results */
		return returnVal;
	}
	
	/*
	 * Checks if newDateStr is newer then oldDateStr
	 */
	private boolean isNewerFileName(String oldFileName, String newFileName)
	{
		SimpleDateFormat parser;
		Date oldDate, newDate;
		
		boolean returnVal = false;
		
		/* check the format of the new file is correct */
		if( !isCorrectChromFileFormat( newFileName ) )
		{
			returnVal = false;
		}
		/* check the format of the old file is correct */
		else if( !isCorrectChromFileFormat( oldFileName ) )
		{
			returnVal = true;
		}
		/* get the dates in the file names */
		else
		{
			/* get the date strings */
			oldFileName = oldFileName.substring(0,20);
			newFileName = newFileName.substring(0,20);
			
			/* initialize the date parser */
			parser = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			
			try 
			{
				/* parse the strings */
				oldDate = parser.parse(oldFileName);
				newDate = parser.parse(newFileName);
				
				/* check if it is newer */
				returnVal = newDate.after(oldDate);
			}
			catch(ParseException e)
			{
				logger.warn(e);
			}
		}
		
		return returnVal;
	}
	
	/* 
	 * Checks file format to be yyyy_MM_dd_HH_mm_ss_chromatogram.xy
	 */
	private boolean isCorrectChromFileFormat(String str)
	{
		return str != null && str.matches("\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_chromatogram.xy");
	}
	
	/*
	 * opens the file as a chromatogram
	 */
	private void openNewChrom(File file)
	{
		Display.getDefault().asyncExec(new Runnable()
		{
			@Override
			public void run() 
			{
				supplierEditorSupport.openEditor(file);
			}
		});
	}
	
}
