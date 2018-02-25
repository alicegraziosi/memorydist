cd src
javac -d C:\Users\alice\IdeaProjects\memory\classes *.java
cd ..
java -classpath C:\Users\alice\IdeaProjects\memory\classes -Djava.rmi.server.codebase=file:C:\Users\alice\IdeaProjects\memory\classes\ RegistrationServiceServer 10 &
