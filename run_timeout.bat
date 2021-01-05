start java -Dfile.encoding=UTF-8 -classpath "source/out" skj.pro2.server.Server 3002 3004 3003 3000 3001
timeout 3
java -Dfile.encoding=UTF-8 -classpath "source/out" skj.pro2.client.Client 127.0.0.1 3002 3004 3003
pause
