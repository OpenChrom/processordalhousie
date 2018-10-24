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

	public void stopObservation() {

		thread.interrupt();
		thread = null;
	}

	private void startObservation() {

		File directory = new File(PreferenceSupplier.getPathFiles());
		if(directory.exists()) {
			thread = new Thread(new Runnable() {

				@Override
				public void run() {

					try {
						/*
						 * Liam
						 * Make your checks here if there's a new file.
						 * Also use the preferences to allow the user to define an interval to update the list and search for new files.
						 */
						while(true) {
							Thread.sleep(2000);
							exitloop:
							for(File file : directory.listFiles()) {
								if(supplierEditorSupport.isMatchMagicNumber(file)) {
									if(supplierEditorSupport.isSupplierFile(file) || supplierEditorSupport.isSupplierFileDirectory(file)) {
										/*
										 * Open Chromatogram
										 */
										Display.getDefault().asyncExec(new Runnable() {

											@Override
											public void run() {

												supplierEditorSupport.openEditor(file);
											}
										});
										break exitloop;
									}
								}
							}
						}
					} catch(InterruptedException e) {
						logger.warn(e);
					}
				}
			});
			thread.start();
		}
	}
}
