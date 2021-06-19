/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2
*/


//class represents link between local port and remote port for sending and receiving

//link class should also include IP addr but we have hardcoded this 

//


public class Link {
    
    int remotePort;
    int rBase; 
    int rNext;
    Packet[] rWindow;
    int sBase; 
    int sNext;
    Packet[] sWindow; 
    int lostP; 
    byte[] recvData = new byte[1024];
    String addr = "127.0.0.1";
    int recvIndex = 0;
    int recvCount; 
    int recvLoss; 
    int sendCount; 
    int sendLoss;
    

    public Link(int remotePort, int windowSize){
        rWindow = new Packet[windowSize];
        sWindow = new Packet[windowSize];
        this.remotePort = remotePort; 
    }
    
}
