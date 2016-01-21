/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * Copyright (C) 2003-2014 E.R.P. Consultores y Asociados, C.A.               *
 * All Rights Reserved.                                                       *
 * Contributor(s): Raul Muñoz www.erpcya.com					              *
 *****************************************************************************/

package org.adempiere.pos.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JTextArea;


/**
 * @author Mario Calderon, mario.calderon@westfalia-it.com, Systemhaus Westfalia, http://www.westfalia-it.com
 * @author Yamel Senih, ysenih@erpcya.com, ERPCyA http://www.erpcya.com
 * @author Raul Muñoz, rmunoz@erpcya.com, ERPCYA http://www.erpcya.com
 * @author victor.perez@e-evolution.com , http://www.e-evolution.com
 **/

public class POSClientSide extends Thread {  

	public POSClientSide(String p_Host, String p_Print, JTextArea m_Terminal) {
		m_Host = p_Host;
		m_Print = p_Print;
		fTerminal = m_Terminal;

		if(connect())		
			this.start();
	}
	
	/** Socket Client 			*/
	private Socket 				socketClient = null;
	/** Host Name 				*/
	private String 				m_Host = null;
	/** Print Name 				*/
	private String 				m_Print = null;
	/** Is Stop 				*/
	private boolean 			isStopped;
	/** Data Input Stream 		*/
	private DataInputStream 	dis = null;
	/** Field Terminal    		*/
	private JTextArea 			fTerminal = null;
	
	/**
	 * Connect with Server
	 * @return
	 * @return boolean
	 */
	private boolean connect() {
		try {
			socketClient = new Socket(m_Host, 5444);
			socketClient.setKeepAlive(true);
			isStopped = false;
	    	setText("Connected");
			return isStopped;
		} catch (IOException e) {
	    	setText("Error Connecting: "+e.getMessage());
	    	return isStopped = true;
		}
	}
		
	public void run(){
		
	    try {
	    	 
	      while(!isStopped) {
			 connect();
	    	 dis = new DataInputStream(socketClient.getInputStream());
	    	 // Name File
             String name = "zk"+dis.readUTF().toString(); 

              // Size File
              int tam = dis.readInt(); 

              String path = System.getProperty("user.home")+File.separator+name ;
              FileOutputStream fos = new FileOutputStream(path);
              BufferedOutputStream out = new BufferedOutputStream( fos );
              BufferedInputStream in = new BufferedInputStream( socketClient.getInputStream() );

              byte[] buffer = new byte[tam];
              for( int i = 0; i < buffer.length; i++ ) {
                 buffer[ i ] = ( byte )in.read( ); 
              }
              
              out.write( buffer );
			  setText("File Received");
              
              out.flush(); 
    		  out.close();
    		  try{
    			  String[] cmd = new String[] { "lp" , "-d", m_Print, path};
    			  Runtime.getRuntime().exec(cmd);
    			  setText("Printing File");
    		  }catch(Exception a){
    			  setText("Error Printing: "+a.getMessage());
    		  }

	       }     
	      	
	    } catch (IOException e) {
	    	isStopped=true;
	    	setText(e.getLocalizedMessage());
	    }
	}
	
	/**
	 * Set Text
	 * @param m_Text
	 * @return void
	 */
	private void setText(String m_Text) {
		fTerminal.setText(getText()+m_Text+"\n");
	}
	
	/**
	 * Get Text
	 * @return String
	 */
	private String getText() {
		return fTerminal.getText();
	}
	
	/**
	 * IsStopped
	 * @return boolean
	 */
	public boolean isStopped() {
		return isStopped;
	}
	
	/**
	 * Close Connection 
	 * @return void
	 */
	public void closeConnect(){
		try {
			isStopped = true;
			socketClient.close();
			this.interrupt();
			setText("Disconnected");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}