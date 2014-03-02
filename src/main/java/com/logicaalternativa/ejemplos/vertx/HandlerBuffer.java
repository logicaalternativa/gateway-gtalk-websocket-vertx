/*
 *      HandlerBuffer.java
 *      
 *      Copyright 2014 Miguel Rafael Esteban Martín (www.logicaalternativa.com) <miguel.esteban@logicaalternativa.com>
 *      
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *      
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *      
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 *      MA 02110-1301, USA.
 */
package com.logicaalternativa.ejemplos.vertx;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * Manejador que procesa cuando se recibe un mensaje en el websocket.<br/>
 * Se envia el mensaje al broadcast
 * @author miguel
 *
 */
public class HandlerBuffer implements Handler<Buffer>{
	
	/**
	 *Logger de la clase 
	 */
	protected Logger logger = LoggerFactory.getLogger( HandlerBuffer.class ); 
	
	/**
	 *Web socket asociado. 
	 */
	private ServerWebSocket webSocket;
	
	public HandlerBuffer( ServerWebSocket webSocket ){
		
		this.webSocket = webSocket;
		
	}

	@Override
	public void handle( Buffer arg0 ) {
		
		if ( logger.isInfoEnabled() ) {
			
			logger.info( "Mensaje recibido del ws: " + arg0.toString() );
			
		}

		Broadcast.getInstance().sendMessageAll( arg0.toString(), this.webSocket.binaryHandlerID(), Broadcast.Type.WEBSOCKET );
		
	
	}
	
	
	
}
