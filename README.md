# Port Knocking
## Course
Computer Networks in Java
# Goal
Implement Port Knocking in Java
https://en.wikipedia.org/wiki/Port_knocking
# 
The project include compile and run scripts
* `compile.bat` - compiles source files into `out` directory
* `run_success.bat` - runs server and client; client is knocking with correct port sequence and the connection is established
* `run_badseq.bat` - runs server and client; client is knocking with incorrect port sequence. Server does not reply therefore the connection is not established
* `run_multipleClients.bat` - runs server and 5 clients; One of them knows the correct sequence; One of them does not knock enough ports.
# Program arguments
### Client Class
* 1st argument: server IP
* n+1 argument: port to knock sequence
### Server Class:
* n argument(s): correct UDP ports sequence

(arguments are separated by a space)

## Documentation
File `dokumentacja.pdf` is a polish documentation of the program performed

# Example
## Success

### Server
```
Starting the server
Start listening on 3001 UDP port
Start listening on 3000 UDP port
Start listening on 3003 UDP port
Start listening on 3004 UDP port
Start listening on 3002 UDP port
RECEIVED MESSAGE [AUTH_REQ:0] from [/127.0.0.1:56724] on port [3002]
RECEIVED MESSAGE [AUTH_REQ:1] from [/127.0.0.1:56724] on port [3004]
RECEIVED MESSAGE [AUTH_REQ:2] from [/127.0.0.1:56724] on port [3003]
RECEIVED MESSAGE [AUTH_REQ:4] from [/127.0.0.1:56724] on port [3001]
RECEIVED MESSAGE [AUTH_REQ:3] from [/127.0.0.1:56724] on port [3000]
UDP Port [3003] has approved the client's seq?:true
UDP Port [3000] has approved the client's seq?:true
UDP Port [3001] has approved the client's seq?:true
UDP Port [3004] has approved the client's seq?:true
UDP Port [3002] has approved the client's seq?:true
Granting the access for [/127.0.0.1:56724]
Starting TCP socket on port: 55418
TCP port has been sent by UDP packet
Client [/127.0.0.1] has established TCP connection
Sending authorization key
Client received key successfully
Closing TCP connection
```
### Client
```
Start knocking
Message [AUTH_REQ:0] has been sent on UDP port [3002]
Message [AUTH_REQ:1] has been sent on UDP port [3004]
Message [AUTH_REQ:2] has been sent on UDP port [3003]
Message [AUTH_REQ:3] has been sent on UDP port [3000]
Message [AUTH_REQ:4] has been sent on UDP port [3001]
Knocking finished - waiting for UDP message
I have received message with port! msg:[PORT:55418]
Trying to establish TCP connection with given port: [55418]
Connection established!
Incoming message: WELCOME
Requesting auth key
Received auth key! :[G3G5EGH26166SHH5525]
Connection closed
```
## Falilure
(in this case, the wrong knocking sequence)
### Server
```
Starting the server
Start listening on 3002 UDP port
Start listening on 3000 UDP port
Start listening on 3003 UDP port
Start listening on 3001 UDP port
Start listening on 3004 UDP port
RECEIVED MESSAGE [AUTH_REQ:1] from [/127.0.0.1:52617] on port [3004]
RECEIVED MESSAGE [AUTH_REQ:0] from [/127.0.0.1:52617] on port [3001]
RECEIVED MESSAGE [AUTH_REQ:2] from [/127.0.0.1:52617] on port [3003]
RECEIVED MESSAGE [AUTH_REQ:4] from [/127.0.0.1:52617] on port [3002]
RECEIVED MESSAGE [AUTH_REQ:3] from [/127.0.0.1:52617] on port [3000]
UDP Port [3000] has approved the client's seq?:true
UDP Port [3002] has approved the client's seq?:false
UDP Port [3003] has approved the client's seq?:true
UDP Port [3004] has approved the client's seq?:true
UDP Port [3001] has approved the client's seq?:false
Client [/127.0.0.1:52617] is timeout..
```
### Client
```
Start knocking
Message [AUTH_REQ:0] has been sent on UDP port [3002]
Message [AUTH_REQ:1] has been sent on UDP port [3004]
Message [AUTH_REQ:2] has been sent on UDP port [3003]
Message [AUTH_REQ:3] has been sent on UDP port [3000]
Message [AUTH_REQ:4] has been sent on UDP port [3005]
Knocking finished - waiting for UDP message
Did not received back message from server!
(probably bad UDP sequence or invalid ports..)
```

