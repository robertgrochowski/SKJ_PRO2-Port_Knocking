start java -Dfile.encoding=UTF-8 -classpath "out" skj.pro2.server.Server 3002 3004 3003 3000 3001 3001
timeout 3
java -Dfile.encoding=UTF-8 -classpath "out" skj.pro2.client.Client 127.0.0.1 3001 3003 3002 3000 3004
pause
