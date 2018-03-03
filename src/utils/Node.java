package utils;

import model.player.Player;

import java.net.InetAddress;

public class Node extends Player{

    private InetAddress host;
    private int port;

    // il nodo successivo in una rete ad anello.
    private Node successivo;

    public Node(InetAddress host, int port) {
        this.host = host;
        this.port = port;
    }
}