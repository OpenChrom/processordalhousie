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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.chemclipse.logging.core.Logger;

public class UdpCommandClientTx implements Runnable
{
	final static Logger logger = Logger.getLogger(UdpCommandClientTx.class);
	
	private BufferedReader userInput;
	private OutputStreamWriter userOutput;
	
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	private int port;
	
	private byte[] outData;

	public UdpCommandClientTx(DatagramSocket socket, String address, int port) throws SocketException, UnknownHostException
	{
		this.clientSocket 	= socket;
		this.IPAddress 		= InetAddress.getByName(address);
		this.port 			= port;
		
		this.userInput 	= new BufferedReader(new InputStreamReader(System.in));
		this.userOutput = new OutputStreamWriter(System.out);
	}
	
	public void shutdown()
	{
		clientSocket.close();
	}
	
	public void run() 
	{
		while(true)
		{
			outData = new byte[1024];
			try 
			{
				userOutput.write("\r\n> ");
				String sentence = userInput.readLine() + '\n';
				outData = sentence.getBytes();

				DatagramPacket out = new DatagramPacket(outData, outData.length, IPAddress, port);
				clientSocket.send(out);
			}
			catch (IOException e)
			{
				logger.warn(e);
			}
		}
	}
	
}
