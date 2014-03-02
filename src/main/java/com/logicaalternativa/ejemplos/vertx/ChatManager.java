/*
 *      ChatManager.java
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

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

/**
 * 
 * Implementación del interfaz {@link ChatManagerListener} que añade el listener
 * {@link MessageListener} para procesar el mensaje cada vez que se conecta
 * un usuario de Gtalk
 * @author miguel
 *
 */
public class ChatManager implements ChatManagerListener {
	
	/**
	 *Logger de la clase 
	 */
	protected Logger logger = LoggerFactory.getLogger( ChatManager.class ); 
	
	
	/**
	 * Clase privada que implementará el proceso de mensajes del chat de Gtalk
	 */
	MessageListener messageListener = new MessageListener() {
		
		@Override
		public void processMessage(Chat chat, Message message) {
			
			if(message.getType().equals(Message.Type.chat) && message.getBody() != null) {
				
				if ( logger.isInfoEnabled()  ) {
					
					logger.info("Recibido mensaje de chat: " + message.getBody() );
					
				}
				
				// Se obtiene el cuerpo del mensaje y quien lo ha enviado 				
				String text = message.getFrom() + ": " + message.getBody();  
				
				// Se envia al todos los usuarios conectados.
				Broadcast.getInstance().sendMessageAll( text, chat.getThreadID(), Broadcast.Type.GTALK );
					
			}
			
		}
	};		


	@Override
	public void chatCreated(Chat chat, boolean arg1) {
		
		if ( logger.isInfoEnabled()  ) {
			
			logger.info("Creado chat: " + chat.getThreadID() );
		
		}
		
		// Se añade el listener al nuevo chat
		chat.addMessageListener( messageListener );
		
		// Se añade el nuevo chat a la lista de broadcast
		Broadcast.getInstance().addChat( chat );
		
	}

}
