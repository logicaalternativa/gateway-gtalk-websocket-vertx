# Prueba de concepto

# Gateway WebSockets y Gtalk utilizando Vert.x
==============================================

## Introducción
Proyecto maven utilizando **Vert.x**. Creado para trastear un poco y ver 
como se trabaja con esta herramienta.

El proyecto es una prueba de concepto que conecta el servicio de Gtalk 
con Websocket. Una utilidad que se me ocurre es la de soporte a usuario 
en tiempo real, desde la web en la que el soporte se conecta mediante 
el servicio de Gtalk incluso desde un dispositivo móvil.

La idea es utilizar una cuenta de Gtalk que sirva de Gateway y de 
reenvío de mensajes y que funcione como sala de conferencia entre los 
usuarios conectados a la web mediante un websoscket y también con los 
que lo hacen mediante Gtalk.

La aplicación se conecta al arrancar al servicio de Gtalk gracias a la 
cuenta que se configura (Boot). Para conectarse, los usuarios de Gtalk, 
establecen  un chat con esta cuenta y todo los que escriban a este 
usuario Boot la aplicación reenviará su mensaje al resto de usuarios 
que tengan un chat abierto con Boot y a los usuarios web conectados por 
websocket. 

Lo mismo pasará entre los usuarios conectados mediante websocket(Web). 
Sus mensajes serán reenviados por la aplicación  al resto de websocket 
y mediante el Boot a los usuarios conectados por Gtalk.

El esquema es el siguiente:

```
  +·····+         +·····+
  |Gtalk| <-+ +-> |Gtalk|
  +·····+   | |   +·····+
            v v
         +······+     +············+    
         | Boot | <-> | Aplicación |
         +······+     +············+    
                           ^ ^
            +·········+    | |   +·········+
            |Websocket|  <-+ +-> |Websocket| 
            +·········+          +·········+             
```
Los usuarios que se conecten por la web lo harán accediendo una URL que 
se inicia en el puerto **8888**. Este servidor web y el de websocket lo 
inicia la aplicación gracias a **Vert.x**

##¿Cómo se compila?

Es un proyecto maven java. Después de bajarte el proyecto para generar 
el modulo hay que ejecutar el siguiente comando

 mvn install

Con su ejecución se creará el el directorio target el zip del módulo:

 target/modulouno-1.0-SNAPSHOT-mod.zip

##¿Cómo se ejecuta?

Para poder ejecutar el módulo es necesario tener instalado antes 
**Vert.x.** Las instrucciones de instalación las puedes encontrar 
[aquí] (http://vertx.io/install.html).

Comando para iniciar el modulo:

 vertx runzip modulouno.0-SNAPSHOT-mod.zip -conf <archivoConfiguracion.json>

Donde *<archivoConfiguracion.json>* se indica el usuario y la 
contraseña de la cuenta Boot de Gtalk

### Configuración
Existe un ejemplo de este archivo en el directorio *[etc/conf.json] 
(https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/etc/conf.json)*. 

{

        "gtalk": 
                {
                        "user":"<usuario>@gmail.com",
                        "password" : "<contraseña>" 
                }

}

Cambiar *<usuario>* y *<contraseña>* por los datos de vuestra cuenta.

## Cliente WebSocket

Una vez arrancado el modulo para poder acceder al cliente web de 
websocket hay que acceder a la siguiente dirección

 **http://localhost:8888**

Se descargará el fichero index.html ubicado 
en (*[src/main/resources/html/index.html] (https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/src/main/resources/html/index.html)*). El index.html contiene el 
código **Javascript** necesario para conectarse al servidor de 
websocket (en la dirección http://localhost:8888/services/gtalk) y 
enviar y recibir los mensajes.

Screenshot en: *[etc/screenshots/cliente_web.jpg] (https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/etc/screenshots/cliente_web.jpg)*

## Principales clases
---------------------

### [Server] (https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/src/main/java/com/logicaalternativa/ejemplos/vertx/Server.java)
Verticle que levanta el servidor web, el de sockets y la conexión a 
Gtalk

## [Broadcast] (https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/src/main/java/com/logicaalternativa/ejemplos/vertx/Broadcast.java)
Clase Singlenton que hace de Broadcast entre todos los socket y chat.

Se encarga de mantener una referencia de todos los sockets y chats 
levantadosy de reenviar los mensajes al resto cuando se envía un 
mensaje desde un punto final (socket o chat)

## [ChatManager] (https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/src/main/java/com/logicaalternativa/ejemplos/vertx/ChatManager.java)
se encarga de la gestión de los mensajes que llegan desde la cuenta Boot

## [HandlerBuffer] (https://github.com/logicaalternativa/gateway-gtalk-websocket-vertx/blob/master/src/main/java/com/logicaalternativa/ejemplos/vertx/HandlerBuffer)
Se encarga de la gestión de los mensajes que llegan desde los websocket

M.E.

**[LogicaAlternativa.com] (http://www.logicaalternativa.com)**

