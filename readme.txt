in: C:\Users\alice\IdeaProjects\memory\src

(start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ example.hello.Server)

(java  -classpath classDir example.hello.Client)

****************
Su windows:

Opzione 1:

Per compilare e lanciare il registro RMI:
in \src:
javac -d C:\Users\alice\IdeaProjects\memory\classes *.java
cd ..
rmiregistry &

cambiando terminale:
se c'è bisogno di fare kill del rmi registry sulla porta di default 1009 o su altra porta:
netstat -ano | findstr 1099
taskkill /pid <numero pid> /F

start server:
java -classpath C:\Users\alice\IdeaProjects\memory\classes -Djava.rmi.server.codebase=file:C:\Users\alice\IdeaProjects\memory\classes\ RegistrationServiceServer 30 &

start client:
java -classpath C:\Users\alice\IdeaProjects\memory\classes ClientGiocatore

Tutto questo equivale a:
compileAndStartServer.bat
startClient.bat

OPPURE

Opzione 2 

Per compilare:
Build project su IntelliJ IDEA

start server:
java -classpath C:\Users\alice\IdeaProjects\memory\out\production\memory -Djava.rmi.server.codebase=file:C:\Users\alice\IdeaProjects\memory\out\production\memory RegistrationServiceServer 10 &

start client:
java -classpath C:\Users\alice\IdeaProjects\memory\out\production\memory ClientGiocatore
cmd /k

Tutto questo equivale a:
Su Windows:
startServerBuild.bat
startClientBuild.bat


****************
Su Linux (ok per macchine del laboratorio)

Per compilare:
Build project su IntelliJ IDEA

start server:
fuser -k 1099/tcp
java -classpath out/production/memory -Djava.rmi.server.codebase=file:out/production/memory RegistrationServiceServer 10 &

start client:
java -classpath out/production/memory ClientGiocatore
cmd /k

startServerBuild.sh (./startServerBuild.sh)
startServerBuild.sh (./startServerBuild.sh)


****************
Note
Java version: 9
SDK: 1.8 (ok per macchine del laboratorio)
