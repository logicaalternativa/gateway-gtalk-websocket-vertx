/*
 *      Broadcast.java
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;


/**
 * Clase Singlenton que hace de Broadcast entre todos los socket y chat.<br>
 * Se encarga de mantener una referencia de todos los sockets y chats levantados
 * y de reenviar los mensajes al resto cuando se envía un mensaje desde un
 * punto final (socket o chat)
 * @author miguel
 *
 */
public class Broadcast {
	
	/**
	 *Logger de la clase 
	 */
	protected Logger logger = LoggerFactory.getLogger( Broadcast.class ); 
	
    /**
     * {@link Enum} para diferenciar el tipo de socket (web o chat)
     * @author miguel
     *
     */
    public static enum Type {
    	
    	/**
    	 * Tipo de socket de gtalk
    	 */
    	GTALK,
    	/**
    	 * Tipo websocket
    	 */
    	WEBSOCKET
    	
    }
	
	/**
	 * Clase privada estática para crear el singlenton
	 */
	private static Broadcast instance;
	
	/**
	 * Almacena las referencias a los chat. La clave es el thread ID del chat
	 */
	private Map<String,Chat> chats;
	
	/**
	 * Almacena las referencias a los websocket. La clave es el binary Handler
	 * ID del websocket
	 */
	private Map<String,ServerWebSocket> webSockets;	
	
	/**
	 * Constructor privado
	 */
	private Broadcast(){
		
		this.chats = new HashMap<String,Chat>();
		
		this.webSockets = new HashMap<String,ServerWebSocket>();
		
	}
	
	/**
	 * Método que devuelve la instancia privada de la clase (Singlenton)
	 * @return
	 */
	public static Broadcast getInstance() {
		
		if( instance == null) {
			
			instance = new Broadcast();
			
		}
		
		return instance;
		
	}
	
	
	/**
	 * Método que añade un chat (GTALK) al broadcast. <br>
	 * Se envia un mensaje de conexión al resto de sockets
	 * @param chat
	 */
	public void addChat( Chat chat ){
		
		if ( logger.isInfoEnabled() ) {
			
			logger.info( "Se añade el chat al Broadcast : " + chat.getThreadID() );
			
		}
		
		
		String idChat = chat != null ? chat.getThreadID() : null;
		
		if ( idChat == null ) {
			
			return;
		}
		
		if ( ! this.chats.containsKey(idChat) ) {
			
			this.chats.put( idChat, chat);
			
			sendMessageAll("Se une desde gtalk: " + idChat , idChat , Type.GTALK);
			
			
		}
		
		if ( logger.isInfoEnabled() ) {
			
			logger.info( "Lista de chats (" + this.chats.size() + "): " + this.chats.keySet() );
			
		}
	}
	

	
	/**
	 * Método que añade un chat (WebSocket) al broadcast. <br>
	 * Se envia un mensaje de conexión al resto de sockets
	 * @param socket
	 */
	public void addWebSocket( ServerWebSocket socket ){
		
		if ( logger.isInfoEnabled() ) {
			
			logger.info("Se añade el socket al Broadcast : " + socket.binaryHandlerID()  );
			
		}
		
		String idSocket = socket != null ? socket.binaryHandlerID() : null;
		
		if ( idSocket == null ) {
			
			return;
		}
		
		if ( ! this.webSockets.containsKey(idSocket) ) {
			
			this.webSockets.put( idSocket, socket );
			
			sendMessageAll("Se une desde la web: " + idSocket , idSocket , Type.WEBSOCKET);
		}
		
		if ( logger.isInfoEnabled() ) {
			
			logger.info("Lista de socket (" + this.webSockets.size() + "): " + this.webSockets.keySet() );
			
		}
		
	}
	
	/**
	 * Elnvial los mensajes tanto a los chats como a los sockets
	 * @param messageText
	 * @param id
	 * @param type
	 */
	public void sendMessageAll( String messageText, String id, Type type ) {
		
		sendMessageAllChats( messageText, id, type );
		
		sendMessageAllSocket( messageText, id, type );
		
		
	}
	
	/**
	 * Envía los mensajes a los sockets de gtalk.<br/>
	 * Si el tipo es del tipo GTALK y tiene el mismo id, no se enviará a ese 
	 * socket (el mensaje ha sido enviado por este socket)<br/>
	 * Si ocurre un error es porque el chat se ha caido. Entonces se da de baja
	 * de la lista
	 * @param messageText
	 * @param id
	 * @param type
	 */
	private void sendMessageAllChats( String messageText, String id, Type type ) {
		
		Set<String> keyChats = this.chats.keySet();
		
		for (String key : keyChats) {
			
			if ( key.equals(id) && type.equals( Type.GTALK ) ) {
				
				continue;
				
			}
				
			Chat chat = this.chats.get(key);
			
			try {
				
				if ( logger.isInfoEnabled() ) {
					
					logger.info("Se envia mensaje Chat (" + key + ") :" +  messageText  );
				
				}
				
				chat.sendMessage( "[" + type +  "] "+ messageText );
				
			} catch (XMPPException e) {
				
				deleteChat(chat);
				
			}
			
		}
		
	}
	
	/**
	 * Envía los mensajes a los sockets web<br/>
	 * Si el tipo es del tipo WEBSOCHT y tiene el mismo id, no se enviará a ese 
	 * socket (el mensaje ha sido enviado por este socket)
	 * @param messageText
	 * @param id
	 * @param type
	 */
	private void sendMessageAllSocket( String messageText, String id, Type type ) {
		
		Set<String> keyWs = this.webSockets.keySet();
		
		for (String key : keyWs) {
			
			if ( key.equals( id ) && type.equals(Type.WEBSOCKET) ) {
				
				continue;
				
			}
			
			if ( logger.isInfoEnabled() ) {
				
				logger.info("Se envia mensaje socket (" + key + ") :" +  messageText  );
			}
				
			ServerWebSocket ws = this.webSockets.get( key );
			
			ws.writeTextFrame( "[" + type +  "] "+ messageText );
			
			
		}
		
	}
	
	/**
	 * Borra el socket web de la lista. Después envia un mensaje de despedida
	 * al resto que permanece conectado
	 * @param socket
	 */
	public void deleteSocket( ServerWebSocket socket ){
		
		String idSocket = socket != null ? socket.binaryHandlerID() : null;
		
		if ( idSocket != null ) {
			
			this.webSockets.remove( idSocket );
			
			sendMessageAll("Nos abandona desde la web: " + idSocket , idSocket , Type.WEBSOCKET);
			
		}
		
	}
	

	
	/**
	 * Borra el socket g de la lista. Después envia un mensaje de despedida
	 * al resto que permanece conectado
	 * @param chat
	 */
	public void deleteChat( Chat  chat ){
		
		String idChat = chat != null ? chat.getThreadID() : null;
		
		if ( idChat != null ) {
			
			this.chats.remove( idChat );
			
			sendMessageAll("Nos abandona desde el gtalk: " + idChat , idChat , Type.GTALK);
			
		}
		
	}

}
