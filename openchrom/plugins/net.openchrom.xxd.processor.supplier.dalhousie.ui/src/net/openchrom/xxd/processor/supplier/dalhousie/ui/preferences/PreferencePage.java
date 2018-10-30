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
package net.openchrom.xxd.processor.supplier.dalhousie.ui.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.Activator;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Dalhousie Underwater Experiments");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() 
	{
		/* local file path */
		addField(new DirectoryFieldEditor(PreferenceSupplier.P_PATH_FILES, "Path Files", getFieldEditorParent()));
		
		/* Refresh time */
		IntegerFieldEditor fileRefreshRate = 
				new IntegerFieldEditor(
										PreferenceSupplier.FILE_REFRESH_RATE_NAME, 			/* name */
										PreferenceSupplier.FILE_REFRESH_RATE_LABEL_TEXT, 	/* label */
										getFieldEditorParent());
		
		fileRefreshRate.setValidRange(	PreferenceSupplier.FILE_REFRESH_RATE_MIN, 			/* minimum */
										PreferenceSupplier.FILE_REFRESH_RATE_MAX);			/* maximum */
		
		addField(fileRefreshRate);
		
		/* FTP Server */
		addField(new StringFieldEditor(	PreferenceSupplier.FTP_SERVER_NAME, 
										PreferenceSupplier.FTP_SERVER_LABEL, 
										getFieldEditorParent() ));
		/* FTP User */
		addField(new StringFieldEditor(	PreferenceSupplier.FTP_USER_NAME, 
										PreferenceSupplier.FTP_USER_LABEL, 
										getFieldEditorParent() ));
		
		/* FTP Port */
		addField(new IntegerFieldEditor(PreferenceSupplier.FTP_PORT_NAME, 
										PreferenceSupplier.FTP_PORT_LABEL, 
										getFieldEditorParent() ));
		
		/* FTP Password */
		addField(new StringFieldEditor(	PreferenceSupplier.FTP_PASS_NAME, 
										PreferenceSupplier.FTP_PASS_LABEL, 
										getFieldEditorParent() ));
		
		/* FTP Directory */
		addField(new StringFieldEditor(	PreferenceSupplier.FTP_DIR_NAME, 
										PreferenceSupplier.FTP_DIR_LABEL, 
										getFieldEditorParent() ));
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {

	}
}
