start java -Dfile.encoding=UTF-8 -classpath "out" skj.pro2.server.Server 3002 3004 3003 3000 3001 3001
timeout 3
cd multipleclients
start run_client1.bat
start run_client2.bat
start run_client3.bat
start run_client4.bat
