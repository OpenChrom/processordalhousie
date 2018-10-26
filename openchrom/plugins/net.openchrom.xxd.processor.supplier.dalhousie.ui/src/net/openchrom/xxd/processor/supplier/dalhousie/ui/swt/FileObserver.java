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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.ux.extension.ui.provider.ISupplierEditorSupport;
import org.eclipse.swt.widgets.Display;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;

public class FileObserver {

	private static final Logger logger = Logger.getLogger(MeasurementObserverUI.class);
	//
	private ISupplierEditorSupport supplierEditorSupport;
	private Thread thread;

	public FileObserver(ISupplierEditorSupport supplierEditorSupport) {
		this.supplierEditorSupport = supplierEditorSupport;
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
		/* Open the file specified in settings */
		File directory = new File(PreferenceSupplier.getPathFiles());
		
		/* Check the file exists */
		if(!directory.exists())
		{
			/* return a fail */ //TODO: warn user or throw an exception
			return false;
		}
		
		/* Create the thread to run the file observer */
		thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				runFileObserver( directory, PreferenceSupplier.getRefreshRate() );	
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
	 * 
	 * 	directory: the directory to check for new chromatograms in
	 */
	private void runFileObserver( File directory, int refreshRate )
	{
		File newChrom;
		File currentChrom = null;
	
		while(true)
		{	
			/* get the newest chromatogram in the directory, checked against the current one */
			newChrom = getNewestFile(directory, currentChrom);
			
			/* check if a newer chromatogram was found */
			if( newChrom != null && newChrom != currentChrom )
			{
				/* open the newest chromatogram */
				openNewChrom(newChrom);
				
				/* set the current chromatogram to the one just opened */
				currentChrom = newChrom;
			}
			
			/* sleep for specified amount of time TODO: make a user setting for refresh frequency*/
			try 
			{
				Thread.sleep( (long) refreshRate );
			} 
			catch(InterruptedException e)
			{
				logger.warn(e);
			}
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
		String oldDateStr, newDateStr;
		SimpleDateFormat parser;
		Date oldDate, newDate;
		
		boolean returnVal = false;
		
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
		/* check if new file is newer then old file based on the file name */
		else
		{
			// TODO: check file format
			/* get the date strings */
			oldDateStr = oldFile.getName().substring(0,20);
			newDateStr = newFile.getName().substring(0,20);
			
			/* initialize the date parser */
			parser = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			
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
		}
		
		/* return the resutls */
		return returnVal;
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
