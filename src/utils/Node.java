package utils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @desc Class representing the network node
 */

public class Node implements Serializable{

    protected String host; /** the host of the node*/
    protected int port; /** the port of the node*/

    /** NON SONO SICURO DI QUESTO */
    // il nodo successivo in una rete ad anello.
    private Node successivo;
       
    /**
     * @desc build object by passing host and port
     * @param String $host, int $port
   */
	public Node(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	/**
     * @desc build the node using the host string
     * @param string $host, int $port
   */
    public void buildNodeByString(String host, int port) throws UnknownHostException {
		this.host = host;
    	this.port = port;
	}
    
	/**
     * @desc get the host
     * @return InetAddress $host
   */
    public String getHost() {
        return host;
    }
    
    /**
     * @desc set host of the node
     * @param InetAddress $host
     * @return void
   */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * @desc get the port
     * @param int $port
   */
    public int getPort() {
        return port;
    }
    
    /**
     * @desc set port of the node
     * @param int $port
     * @return void
   */
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