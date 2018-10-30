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
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.ux.extension.ui.provider.ISupplierEditorSupport;
import org.eclipse.swt.widgets.Display;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileHelper;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.UdpCommandClient;

public class FileObserver {

	private static final Logger logger = Logger.getLogger(MeasurementObserverUI.class);
	//
	private ISupplierEditorSupport supplierEditorSupport;
	private Thread thread;
	
	private FtpObserver ftpObserver;
	
	private File currentChrom;
	
	private UdpCommandClient udpClient;

	public FileObserver(ISupplierEditorSupport supplierEditorSupport) {
		this.supplierEditorSupport = supplierEditorSupport;
		this.ftpObserver = new FtpObserver();
		
		/* Start the UDP thread */
		try
		{
			udpClient = new UdpCommandClient();
			udpClient.start();
		}
		catch(SocketException | UnknownHostException e)
		{
			logger.warn(e);
		}
	}

	public boolean isObservationRunning()
	{
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
			//TODO: warn user
			return false;
		}
		
		/* open the FTP connection */
		try
		{
			ftpObserver.open();
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
			}
		});
		
		/* Start the thread */
		thread.start();
		
		/* return a pass */
		return true;
	}
	
	/* 
	 * Runs the file observer:
	 * - download any newer chromatogram from the FTP server to the local directory
	 * - check for any new chromatogram in the local file direcrtory, 
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
			/* download any newer files from the FTP server */
			try 
			{
				ftpObserver.downloadNewFtpFiles(localFileDir, currentChrom);
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
				
				//TODO close old chromatogram
				
				/* set the current chromatogram to the one just opened */
				currentChrom = newChrom;
			}
			
			/* sleep for specified amount of time */
			Thread.sleep( (long) refreshRate );
			
		}
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
				if( newestFile == null || FileHelper.isNewerFile( newestFile, file) )
				{
					newestFile = file;
				}
			}
		}
		
		/* return what was found, if anything */
		return newestFile;
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
