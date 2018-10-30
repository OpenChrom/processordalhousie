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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.eclipse.chemclipse.logging.core.Logger;

public class UdpCommandClientRx implements Runnable
{
final static Logger logger = Logger.getLogger(UdpCommandClientTx.class);

	private DatagramSocket clientSocket;
	
	private OutputStreamWriter userOutput;

    private byte[] inData;
	
    /* constructor */
	public UdpCommandClientRx(DatagramSocket socket) throws SocketException, UnknownHostException
	{
		this.clientSocket = socket;
		this.userOutput = new OutputStreamWriter(System.out);
	}
	
	public void shutdown()
	{
		clientSocket.close();
	}
	
	public void run() 
	{
		String modifiedSentence;
		DatagramPacket in;

		while(true)
		{
			inData = new byte[1024];
			in = new DatagramPacket(inData, inData.length);
			try
			{
				clientSocket.receive(in);
				modifiedSentence = new String( in.getData() );
				userOutput.write("\n\rServer >\n" + modifiedSentence);
			}
			catch (IOException e)
			{
				logger.warn(e);
			}
		}
	}
}
