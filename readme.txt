in: C:\Users\alice\IdeaProjects\memory\src

(start java -classpath classDir -Djava.rmi.server.codebase=file:classDir/ example.hello.Server)

(java  -classpath classDir example.hello.Client)

Su windows:

Per compilare e lanciare il registro RMI:
in \src:
javac -d C:\Users\alice\IdeaProjects\memory\classes *.java
cd ..
rmiregistry &

cambiando terminale:
se c'è bisogno:
netstat -ano | findstr 1099
taskkill /pid /F

start server:
java -classpath C:\Users\alice\IdeaProjects\memory\classes -Djava.rmi.server.codebase=file:C:\Users\alice\IdeaProjects\memory\classes\ RegistrationServiceServer 30 &

start client:
java -classpath C:\Users\alice\IdeaProjects\memory\classes ClientRegistration