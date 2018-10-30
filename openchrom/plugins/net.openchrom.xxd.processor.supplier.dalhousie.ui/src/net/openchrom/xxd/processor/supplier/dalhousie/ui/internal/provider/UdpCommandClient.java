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
package net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;

public class UdpCommandClient
{
	private Thread rxClientThread;
	private Thread txClientThread;
	
	private DatagramSocket clientSocket;
	
	public UdpCommandClient() throws SocketException, UnknownHostException
	{
		clientSocket = new DatagramSocket();
		
		rxClientThread = new Thread(	new UdpCommandClientRx(clientSocket) );
		
		txClientThread = new Thread( 	new UdpCommandClientTx(clientSocket,
										PreferenceSupplier.getServer(),
										PreferenceSupplier.getUdpCliPort()) );
	}
	
	public void start()
	{
		rxClientThread.start();
		txClientThread.start();
	}
	
	public void stop()
	{
		
	}
	
}
