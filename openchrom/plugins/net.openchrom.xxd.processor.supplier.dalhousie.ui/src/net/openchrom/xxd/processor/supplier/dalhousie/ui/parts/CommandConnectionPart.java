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
package net.openchrom.xxd.processor.supplier.dalhousie.ui.parts;

import javax.inject.Inject;

import org.eclipse.swt.widgets.Composite;

import net.openchrom.xxd.processor.supplier.dalhousie.ui.swt.CommandConnectionUI;

public class CommandConnectionPart {
	
	@SuppressWarnings("unused")
	private CommandConnectionUI commandConnectionUI;

	@Inject
	public CommandConnectionPart(Composite parent) {
		commandConnectionUI = new CommandConnectionUI(parent);
	}
}
