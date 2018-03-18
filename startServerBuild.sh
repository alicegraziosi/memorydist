fuser -k 1099/tcp
java -classpath out/production/memory -Djava.rmi.server.codebase=file:out/production/memory server.RegistrationServiceServer 10 &
