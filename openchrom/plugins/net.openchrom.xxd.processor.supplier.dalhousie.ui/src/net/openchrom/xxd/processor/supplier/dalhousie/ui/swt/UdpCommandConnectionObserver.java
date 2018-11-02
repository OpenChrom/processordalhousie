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

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.chemclipse.logging.core.Logger;

import net.openchrom.xxd.processor.supplier.dalhousie.preferences.PreferenceSupplier;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.UdpCommandClientRx;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.UdpCommandClientRx.I;
import net.openchrom.xxd.processor.supplier.dalhousie.ui.internal.provider.UdpCommandClientTx;

public class UdpCommandConnectionObserver
{
	private static final Logger logger = Logger.getLogger(UdpCommandConnectionObserver.class);
	
	private static final String GO			= "go";
	private static final String HELP 		= "help";
	private static final String STATUS 		= "status";
	private static final String IP_CONFIG 	= "ip-config";
	
	private static final String DELIM		= "\n";
	
	private Thread rxClientThread;
	private Thread txClientThread;
	
	private DatagramSocket clientSocket;
	
	private BlockingQueue<String> sendQueue;
	
	private I writer;
	
	public UdpCommandConnectionObserver(I writer) throws SocketException, UnknownHostException
	{
		this.writer = writer;
		
		/* Create the socket */
		clientSocket = new DatagramSocket();
		/* Create the receiving client */
		rxClientThread = new Thread(	new UdpCommandClientRx(	clientSocket,
																writer) );
		sendQueue = new LinkedBlockingQueue<>();
		/* Create the sending client */
		txClientThread = new Thread( 	new UdpCommandClientTx(	clientSocket,
																PreferenceSupplier.getServer(),
																PreferenceSupplier.getUdpCliPort(),
																sendQueue) );
	}

	public void start()
	{
		rxClientThread.start();
		txClientThread.start();
	}
	
	public void stop()
	{
		
	}
	
	
	public void sendHelp()
	{
		sendCommand(HELP);
	}
	
	public void sendStatus()
	{
		sendCommand(STATUS);
	}
	
	public void sendGo()
	{
		sendCommand(GO);
	}
	
	public void sendIpConfig()
	{
		sendCommand(IP_CONFIG);
	}
	
	public void sendCustomCommand(String str)
	{
		sendCommand(str);
	}
	
	private void sendCommand(String str)
	{
		try
		{
			writer.writeStr(">sending: " + str);
			sendQueue.put(str + DELIM);
		}
		catch(InterruptedException e)
		{
			logger.warn(e);
		}
	}
	
	
	
	
}
