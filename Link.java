/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Link class represents link between local port and remote port for sending 
and receiving messages. Each remote port has its own link. 
*/

import java.util.ArrayList;

public class Link {
    
    //hardcoded IP for local use
    String addr = "127.0.0.1"; 
    int remotePort;
    int lossProb;  
    Packet[] rWindow;
    Packet[] sWindow; 
    byte[] recvData = new byte[1024];
    int rBase; 
    int rNext;
    int recvIndex = 0;
    int recvCount; 
    int recvLoss; 
    int sBase; 
    int sNext;   
    int sendCount; 
    int sendLoss;
    ArrayList<Thread> sendQueue; 
    Thread sending = null; 
    

    public Link(int remotePort, int windowSize, int lossProb){
        rWindow = new Packet[windowSize];
        sWindow = new Packet[windowSize];
        this.remotePort = remotePort; 
        this.lossProb = lossProb;
        sendQueue = new ArrayList<Thread>();
    }  
}
