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

import java.util.HashMap;
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
	public static final int FILE_REFRESH_RATE_MAX 			= 10000; 	/* maximum 10s for now */
	/* FTP Server */
	public static final String FTP_SERVER_NAME		= "FtpServerName";
	public static final String FTP_SERVER_LABEL		= "FTP Server";
	public static final String FTP_SERVER_DEFAULT	= "192.168.4.73";
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
		/* FTP server */
		defaultValues.put(FTP_SERVER_NAME, FTP_SERVER_DEFAULT);
		/* FTP User */
		defaultValues.put(FTP_USER_NAME, FTP_USER_DEFAULT);
		/* FTP Port */
		defaultValues.put(FTP_PORT_NAME, Integer.toString(FTP_PORT_DEFAULT));
		/* FTP Password */
		defaultValues.put(FTP_PASS_NAME, FTP_PASS_DEFAULT);
		/* FTP Directory */
		defaultValues.put(FTP_DIR_NAME, FTP_DIR_DEFAULT);
		
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
	
	public static String getFtpServer()
	{
		return getFilterPath(FTP_SERVER_NAME, FTP_SERVER_DEFAULT);
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

	public static void setPathFiles(String pathFiles) {

		setFilterPath(P_PATH_FILES, pathFiles);
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
