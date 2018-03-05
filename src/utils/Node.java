package utils;

import model.player.Player;

import java.net.InetAddress;

public class Node extends Player{

    private int id;
    private InetAddress host;
    private int port;

    // il nodo successivo in una rete ad anello.
    private Node successivo;

    public Node(InetAddress host, int port, int id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public InetAddress getHost() {
        return host;
    }

    public void setHost(InetAddress host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Node getSuccessivo() {
        return successivo;
    }

    public void setSuccessivo(Node successivo) {
        this.successivo = successivo;
    }
}