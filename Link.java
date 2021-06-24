import java.util.ArrayList;

/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Link class represents link between local port and remote port for sending and receiving

*/


public class Link {
    
    int remotePort;
    int rBase; 
    int rNext;
    Packet[] rWindow;
    int sBase; 
    int sNext;
    Packet[] sWindow; 
    int lossProb; 
    byte[] recvData = new byte[1024];
    String addr = "127.0.0.1";
    int recvIndex = 0;
    int recvCount; 
    int recvLoss; 
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
