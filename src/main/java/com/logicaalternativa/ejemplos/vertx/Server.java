/*
 *      Server.java
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

import java.net.URL;

import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.platform.Verticle;

/**
 *  Verticle que levanta el servidor web, el de sockets y la conexión a Gtalk
 * @author miguel
 *
 */
public class Server extends Verticle {
	
	public void start() {
		
		// Se crea el servidor
		HttpServer server = vertx.createHttpServer();
		
		// Se defiene el manejador de websockets
		server.websocketHandler( new Handler<ServerWebSocket>( ){ 
			
			@Override
			public void handle( ServerWebSocket ws ) {
				
				if ( container.logger().isInfoEnabled() ) {
					
					container.logger().info( "[HandlerWebSocket] se crea un ws: " + ws.path() );
					
				}
				 
				// El websocket estará levantado en la siguiente ruta
				if (ws.path().equals("/services/gtalk")) {
		    		
		    		ws.dataHandler( new HandlerBuffer( ws ) ); // Manejador de los mensajes recibidos
		    		
		    		ws.endHandler( new HandlerVoid( ws) ); // Manejador de las desconexiones de websockets
		    		
		    		Broadcast.getInstance().addWebSocket( ws ); // Se añade el websocket al broadcast
		    		
		        } else {
		        	
		            ws.reject();
		        } 
				
			}
		});
		
		
		// Definición de rutas para el servidor web
		RouteMatcher routeMatcher = new RouteMatcher();
		
		// En el raiz se sirve el fichero index.html que está dentro del módulo
		routeMatcher.get("/", new Handler<HttpServerRequest>() {
		   
			public void handle(HttpServerRequest request) {
		    	
		    	URL url = this.getClass().getClassLoader().getResource("html/index.html");
		    	
		    	if ( container.logger().isInfoEnabled() ) {
					
					getContainer().logger().info(" file: " + url.getFile() );
					
		    	}
		    	
		    	request.response().sendFile (url.getFile() ).close();
		    }
		});
		
		// Se lenvata el servidor web en cualquier interfaz en el puerto 8888 
		server.requestHandler( routeMatcher).listen( 8888, "0.0.0.0", new AsyncResultHandler<HttpServer>() {
			
			@Override
			public void handle(AsyncResult<HttpServer> arg0) { 
				
				if (  arg0.succeeded() ) {
					
					getContainer().logger().info("Started");
					
					// Se inicia la conexión al Gtalk
					XMPPConnection connection = new XMPPConnection("gmail.com");
							
					// Se necesita para la conexión a Gtalk
					SASLAuthentication.supportSASLMechanism("PLAIN", 0);
					
					try {
						
						connection.connect();
						
						// Se obtienen el usuario y la contraseña del archivo de configuración
						String user = container.config().getObject("gtalk").getString("user");
						String password = container.config().getObject("gtalk").getString("password");
						
						// Se hace login
						connection.login( user, password );
						getContainer().logger().info( "Conectado a " + connection.getUser() );
			            
			            // Se establece el estado del usuario
						Presence presence = new Presence( Presence.Type.available );
			            connection.sendPacket(presence);
			            
			            // Se asigna el listener para nuevos chats
			            connection.getChatManager().addChatListener( new ChatManager() );
			            
						
					} catch (XMPPException e) {
						
						e.printStackTrace();
						throw new RuntimeException(e);
						
					}
					
				}
				
			}
			
		});

	  }

}
