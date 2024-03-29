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

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.ux.extension.ui.provider.ISupplierEditorSupport;
import org.eclipse.swt.widgets.Display;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.FileHelper;

public class FileObserver {

	private static final Logger logger = Logger.getLogger(MeasurementObserverUI.class);
	//
	private ISupplierEditorSupport supplierEditorSupport;
	private Thread thread;
	
	private FtpObserver ftpObserver;
	
	private File currentChrom;

	public FileObserver(ISupplierEditorSupport supplierEditorSupport) {
		this.supplierEditorSupport = supplierEditorSupport;
		this.ftpObserver = new FtpObserver();
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
	 * stops the thread running the file observer and closes the FTP connection
	 */
	public void stopObservation() 
	{
		if( thread != null )
		{	
			/* stop the thread */
			thread.interrupt();
			thread = null;
			
			/* close the connection */
			try
			{
				ftpObserver.close();
			}
			catch(IOException e)
			{
				logger.warn(e);
			}
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
	 * - check for any new chromatogram in the local file directory, 
	 * - if there is one display it,
	 * - sleep for specified amount of time
	 * - repeat
	 */
	private void runFileObserver( File localFileDir, int refreshRate ) throws InterruptedException
	{
		File newChrom;
		String newChromDir = null;
		
		currentChrom = null;
	
		while(true)
		{	
			/* download any newer files from the FTP server */
			try 
			{
				newChromDir = ftpObserver.downloadNewFtpFiles(localFileDir, currentChrom);
			}
			catch(IOException e) 
			{
				/* try to reconnect */
				try
				{
					ftpObserver.open();
				}
				catch(IOException e1)
				{
					logger.warn(e);
					logger.warn(e1);
					// TODO: user alert 
				}
			}
			
			if(newChromDir != null)
			{
				
				newChrom = FileHelper.getChromFromFolder(newChromDir);

				if( newChrom != null )
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
