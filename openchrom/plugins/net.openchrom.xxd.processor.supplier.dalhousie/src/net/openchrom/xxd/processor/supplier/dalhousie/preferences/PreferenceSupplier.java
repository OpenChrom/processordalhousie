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
package net.openchrom.xxd.processor.supplier.dalhousie.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.support.preferences.IPreferenceSupplier;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import net.openchrom.xxd.processor.supplier.dalhousie.Activator;

public class PreferenceSupplier implements IPreferenceSupplier {

	private static final Logger logger = Logger.getLogger(PreferenceSupplier.class);
	/* File path */
	public static final String P_PATH_FILES = "pathFiles";
	public static final String DEF_PATH_FILES = "";
	/* refresh rate */
	public static final String FILE_REFRESH_RATE_NAME 		= "FileRefreshRate";
	public static final String FILE_REFRESH_RATE_LABEL_TEXT = "File Refresh Rate (ms)";
	public static final int FILE_REFRESH_RATE_DEFAULT		= 5000; 	/* default 5s */
	public static final int FILE_REFRESH_RATE_MIN 			= 1000; 	/* minimum 1s for now */
	public static final int FILE_REFRESH_RATE_MAX 			= 100000; 	/* maximum 100s for now */
	/* Server */
	public static final String SERVER_NAME			= "FtpServerName";
	public static final String SERVER_LABEL			= "FTP Server";
	public static final String SERVER_DEFAULT		= "192.168.4.73";
	/* FTP User */
	public static final String FTP_USER_NAME		= "FtpUser";
	public static final String FTP_USER_LABEL		= "FTP User";
	public static final String FTP_USER_DEFAULT		= "anonymous";
	/* FTP Port */
	public static final String FTP_PORT_NAME 		= "FtpPort";
	public static final String FTP_PORT_LABEL		= "FTP Port";
	public static final int FTP_PORT_DEFAULT		= 21;
	/* FTP Password */
	public static final String FTP_PASS_NAME		= "FtpPass";
	public static final String FTP_PASS_LABEL		= "FTP Password";
	public static final String FTP_PASS_DEFAULT		= "";
	/* FTP Directory */
	public static final String FTP_DIR_NAME			= "FtpDir";
	public static final String FTP_DIR_LABEL		= "FTP Directory";
	public static final String FTP_DIR_DEFAULT		= "/";
	/* UDP CLI port */
	public static final String UDP_CLI_PORT_NAME	= "UdpCliPort";
	public static final String UDP_CLI_PORT_LABEL	= "UDP CLI Port";
	public static final int UDP_CLI_PORT_DEFAULT	= 5001;
	
	public static final String CHROMATOGRAM_TIME_NAME	= "ChromatogramTime";
	public static final String CHROMATOGRAM_TIME_LABEL 	= "Chromatogram Time (s)";
	public static final int CHROMATOGRAM_TIME_DEFAULT 	= 30;
	
	
	private static IPreferenceSupplier preferenceSupplier;

	public static IPreferenceSupplier INSTANCE() {

		if(preferenceSupplier == null) {
			preferenceSupplier = new PreferenceSupplier();
		}
		return preferenceSupplier;
	}

	@Override
	public IScopeContext getScopeContext() {

		return InstanceScope.INSTANCE;
	}

	@Override
	public String getPreferenceNode() {

		return Activator.getContext().getBundle().getSymbolicName();
	}

	@Override
	public Map<String, String> getDefaultValues() {

		Map<String, String> defaultValues = new HashMap<String, String>();
		/* local file path */
		defaultValues.put(P_PATH_FILES, DEF_PATH_FILES);
		/* file refresh rate */
		defaultValues.put(FILE_REFRESH_RATE_NAME, Integer.toString(FILE_REFRESH_RATE_DEFAULT));
		/* server */
		defaultValues.put(SERVER_NAME, SERVER_DEFAULT);
		/* FTP User */
		defaultValues.put(FTP_USER_NAME, FTP_USER_DEFAULT);
		/* FTP Port */
		defaultValues.put(FTP_PORT_NAME, Integer.toString(FTP_PORT_DEFAULT));
		/* FTP Password */
		defaultValues.put(FTP_PASS_NAME, FTP_PASS_DEFAULT);
		/* FTP Directory */
		defaultValues.put(FTP_DIR_NAME, FTP_DIR_DEFAULT);
		/* UDP CLI Port */
		defaultValues.put(UDP_CLI_PORT_NAME, Integer.toString(UDP_CLI_PORT_DEFAULT));
		/* Chromatogram time */
		defaultValues.put(CHROMATOGRAM_TIME_NAME, Integer.toString(CHROMATOGRAM_TIME_DEFAULT));
		
		return defaultValues;
	}

	@Override
	public IEclipsePreferences getPreferences() {

		return getScopeContext().getNode(getPreferenceNode());
	}

	public static String getPathFiles() {

		return getFilterPath(P_PATH_FILES, DEF_PATH_FILES);
	}
	
	public static int getRefreshRate()
	{
		return Integer.parseInt( getFilterPath(FILE_REFRESH_RATE_NAME, Integer.toString(FILE_REFRESH_RATE_DEFAULT)) );
	}
	
	public static String getServer()
	{
		return getFilterPath(SERVER_NAME, SERVER_DEFAULT);
	}
	
	public static String getFtpUser()
	{
		return getFilterPath(FTP_USER_NAME, FTP_USER_DEFAULT);
	}
	
	public static int getFtpPort()
	{
		return Integer.parseInt( getFilterPath(FTP_PORT_NAME, Integer.toString(FTP_PORT_DEFAULT)) );
	}
	
	public static String getFtpPass()
	{
		return getFilterPath(FTP_PASS_NAME, FTP_PASS_DEFAULT);
	}
	
	public static String getFtpDir()
	{
		return getFilterPath(FTP_DIR_NAME, FTP_DIR_DEFAULT);
	}
	
	public static int getUdpCliPort()
	{
		return Integer.parseInt( getFilterPath(UDP_CLI_PORT_NAME, Integer.toString(UDP_CLI_PORT_DEFAULT)) );
	}
	
	public static int getChromatogramTime()
	{
		return Integer.parseInt( getFilterPath(CHROMATOGRAM_TIME_NAME, Integer.toString(CHROMATOGRAM_TIME_DEFAULT)) );
	}

	public static void setPathFiles(String pathFiles) {

		setFilterPath(P_PATH_FILES, pathFiles);
	}
	
	public static List<String> getAllSettings()
	{
		List<String> lines = new ArrayList<>();

		lines.add( "#REFRESH_RATE=" 	+ getRefreshRate() );
		lines.add( "#IP_ADDR=" 			+ getServer() );
		lines.add( "#FTP_USER=" 		+ getFtpUser() );
		lines.add( "#FTP_PORT=" 		+ getFtpPort() );
		lines.add( "#FTP_PASSWORD=" 	+ getFtpPass() );
		lines.add( "#FTP_DIRECTORY=" 	+ getFtpDir() );
		lines.add( "#UDP_CLI_PORT=" 	+ getUdpCliPort() );
		lines.add( "#CHROMATOGRAM_TIME="+ getChromatogramTime() );
		
		return lines;
	}

	private static String getFilterPath(String key, String def) {

		IEclipsePreferences eclipsePreferences = INSTANCE().getPreferences();
		return eclipsePreferences.get(key, def);
	}

	private static void setFilterPath(String key, String filterPath) {

		try {
			IEclipsePreferences eclipsePreferences = INSTANCE().getPreferences();
			eclipsePreferences.put(key, filterPath);
			eclipsePreferences.flush();
		} catch(BackingStoreException e) {
			logger.warn(e);
		}
	}
}
