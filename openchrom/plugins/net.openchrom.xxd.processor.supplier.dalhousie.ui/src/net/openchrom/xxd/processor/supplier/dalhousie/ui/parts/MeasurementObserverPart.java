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
package net.openchrom.xxd.processor.supplier.dalhousie.ui.parts;

import javax.inject.Inject;

import org.eclipse.chemclipse.support.ui.workbench.DisplayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class MeasurementObserverPart {

	@Inject
	public MeasurementObserverPart(Composite parent) {
		initialize(parent);
	}

	private void initialize(Composite parent) {

		parent.setLayout(new GridLayout(1, true));
		parent.setBackground(DisplayUtils.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		//
		Label label = new Label(parent, SWT.NONE);
		label.setText("Hello Liam.");
	}
}
