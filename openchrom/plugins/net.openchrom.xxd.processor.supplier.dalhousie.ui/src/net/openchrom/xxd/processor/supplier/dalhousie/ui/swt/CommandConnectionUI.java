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
package net.openchrom.xxd.processor.supplier.dalhousie.ui.swt;

import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.chemclipse.logging.core.Logger;
import org.eclipse.chemclipse.model.types.DataType;
import org.eclipse.chemclipse.ux.extension.ui.provider.ISupplierEditorSupport;
import org.eclipse.chemclipse.ux.extension.xxd.ui.part.support.SupplierEditorSupport;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.UdpCommandClientRx;

public class CommandConnectionUI
{
	private static final Logger logger = Logger.getLogger(CommandConnectionUI.class);
	
	private Text responseBox;
	
	private Text commandEntry;
	
	@SuppressWarnings("unused")
	private ISupplierEditorSupport supplierEditorSupport = new SupplierEditorSupport(DataType.CSD);
	
	private UdpCommandConnectionObserver commandConnection;

	public CommandConnectionUI(Composite parent)
	{
		initialize(parent);
	}

	private void initialize(Composite parent)
	{
		parent.setLayout(new GridLayout(1, true));
		
		/* Set the command buttons */
		createCommandButtons(parent);
		
		responseBox = createResponseBox(parent);
		
		
		/* Start the UDP thread */
		try
		{
			commandConnection = new UdpCommandConnectionObserver(writeText);
			commandConnection.start();
		}
		catch(SocketException | UnknownHostException e)
		{
			logger.warn(e); // TODO: warn the user
		}
	}
	
	private void createCommandButtons(Composite parent)
	{
		/* set the grid layout */
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalAlignment = SWT.END;
		
		/* create a child */
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(gridData);
		composite.setLayout(new GridLayout(4, true));
		
		/* Go button */
		createStandardButton("Go", 			()->commandConnection.sendGo(), 		composite);
		/* Ip-config */
		createStandardButton("Stop", 		()->commandConnection.sendStop(), 		composite);
		/* Help button */
		createStandardButton("Help", 		()->commandConnection.sendHelp(), 		composite);
		/* status button */
		createStandardButton("Status", 		()->commandConnection.sendStatus(), 	composite);
		/* Add a custom command section */
		createCustomCommandSection(composite);
	}
	
	private void createCustomCommandSection(Composite parent)
	{
		/* Add a label */
		Label l = new Label(parent, SWT.NONE);
		l.setText("Custom command: ");
		/* Add the text field */
		commandEntry = new Text(parent, SWT.SINGLE | SWT.BORDER);
		/* Set the background color */
		commandEntry.setBackground(new Color (Display.getCurrent(), 255, 255, 255));
		/* Add a key listener to send the command when enter is pressed */
		commandEntry.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent event)
			{
				if(event.keyCode == SWT.CR)
				{
					sendCustomCommand();
				}
			}
		});
		
		createStandardButton("Send", ()->sendCustomCommand(), parent);
		
	}
	
	private void sendCustomCommand()
	{
		commandConnection.sendCustomCommand(commandEntry.getText());
		commandEntry.setText("");
	}
	
	private Button createStandardButton(String btnTxt, ButtonAction action, Composite parent)
	{
		/* Create the button */
		Button b = new Button(parent, SWT.PUSH);
		/* Add the text */
		b.setText(btnTxt);
		/* Set the layout */
		b.setLayoutData(new GridData(GridData.CENTER));
		/* add the button listener */
		b.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				action.performAction();
			}
		});
		
		return b;
	}
	

	private Text createResponseBox(Composite parent)
	{
		Text t = new Text(parent, SWT.MULTI | SWT.V_SCROLL);
		
		/* make it read only */
		t.setEditable(false);
		
		/* fill all available space */
		t.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return t;
	}
	
	UdpCommandClientRx.I writeText = (str) -> 
	{
		Display.getDefault().syncExec(new AddCommandTxt(str));	
	};
	
	private interface ButtonAction
	{
		public void performAction();
	}
	
	private class AddCommandTxt implements Runnable
	{
        String str;
        AddCommandTxt(String s)
        {
        	str = s;
        }
        public void run()
        {
        	str.replaceAll("\r\n", responseBox.getLineDelimiter());
    		responseBox.append(str);
        }
    }
	
}
