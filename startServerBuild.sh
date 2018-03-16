fuser -k 1099/tcp
java -Djava.rmi.server.hostname=localhost -Djava.security.policy=file:./security.policy -classpath out/production/memory -Djava.rmi.server.codebase=file:out/production/memory server.RegistrationServiceServer 10 &
