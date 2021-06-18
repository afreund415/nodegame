
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2
*/
import java.net.*;
import java.util.*;



public class SR{


    int lPort; 
    int windw; 
    int pLoss; 
    int dLoss; 
    Receive receive; 
    //Send send; 
    DatagramSocket ds;
    boolean running;
    HashMap<Integer, Link> links = new HashMap<Integer, Link>();
    static boolean debug = false;
    final static byte STATUS_MSG = 0x01; 
    final static byte STATUS_EOM = 0x02; 
    final static byte STATUS_ACK = 0x04; 
    //constant for checking ACK
    final static byte STATUS_MOK = 0x08; 



    public SR(int lPort, int windw, int dLoss, int pLoss) throws Exception{

        this.lPort = lPort;
        this.windw = windw; 
        this.dLoss = dLoss;
        this.pLoss = pLoss; 
        
        receive = new Receive();
        ds = new DatagramSocket(lPort);
        running = true;
        ds.setSoTimeout(500);
        receive.start();
        

        
    }

    public Link getLink(int remotePort){
        Link l = links.get(remotePort);

        if (l == null){
            l = new Link(remotePort, windw);
            links.put(remotePort, l);
        }
        return l;
    }

    public void sendMessage(String message, int remotePort, String addr) throws Exception{

        Send send = new Send(message.getBytes(), remotePort, addr);
        send.start();
    }

    public void sendDatagram(Packet p, int remotePort, String addr)throws Exception{
        InetAddress ip = InetAddress.getByName(addr);
        DatagramPacket dp = new DatagramPacket(p.toBytes(), 
                                    p.length(), ip, remotePort);
        ds.send(dp);
    }

    class Send extends Thread{
        int remotePort; 
        byte[] data; 
        String addr; 
        Link l;
        public Send(byte[] data, int remotePort, String addr){
            this.data = data; 
            this.remotePort = remotePort; 
            this.addr = addr;  
            l = getLink(remotePort);    
        }
        public void run(){

            try{
                for (int i = 0; i < data.length; i++){
                    while(l.sNext-l.sBase > windw){
                        sleepMs(100);
                    }
                    byte status = (i == data.length - 1) ? STATUS_MSG | STATUS_EOM:STATUS_MSG;
                    byte[] b = new byte[1];
                    b[0] = data[i];
                    Packet p = new Packet(b,l.sNext, status);
                    l.sWindow[l.sNext++ % windw] = p;
                    sendDatagram(p, remotePort, addr);
                }
            }

            catch(Exception e){
                printError(e.getMessage());
            }
        }
    }


    class Receive extends Thread{

        public void run(){
            //debug statement
            printDebug("Receive Thread started ");
            //set max # of bytes receiver can receive
            byte[] buf = new byte[1024]; 
            
            while(running){
                try{
                    //create new empty packet and open socket for receiving
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);  
                    ds.receive(dp); 
                    if (dp.getLength() > 0){           
                        //set address and port 
                        String addr = dp.getAddress().getHostAddress();
                        int remotePort = dp.getPort();

                        Packet p = new Packet(dp.getData(), dp.getLength());

                        Link l = getLink(remotePort);
                        sendACK(p, remotePort, addr);

                        if ((p.status & STATUS_MSG) != 0){
                            Packet pOld = l.rWindow[p.seq % windw];

                            if (pOld != null && pOld.seq == p.seq){
                                printError("Duplicate packet received");
                            }
                            l.rWindow[p.seq % windw] = p;
                            //send ack

                            if (p.seq >= l.rNext){
                                l.rNext = p.seq + 1;
                            }

                            for (int i = l.rBase; i < l.rNext; i++){
                                pOld = l.rWindow[i % windw];

                                if (pOld != null && pOld.seq == i){
                                    l.recvIndex += pOld.copy(l.recvData, l.recvIndex);
                                    l.rBase = i + 1;

                                    if ((pOld.status & STATUS_EOM) !=0){
                                        recvMessage(l.recvData, l.recvIndex);
                                        l.recvIndex = 0;
                                    }
                                }
                                else{
                                    break;
                                }
                            }
                        }

                        else if((p.status & STATUS_ACK) != 0){
                            Packet pACK = l.sWindow[p.seq % windw];

                            if (pACK != null && pACK.seq == p.seq){
                                pACK.status = (byte) (pACK.status | STATUS_MOK);


                            for (int i = l.sBase; i < l.sNext; i++){
                                pACK = l.sWindow[i % windw];

                                if (pACK != null && (pACK.status & STATUS_MOK) !=0){
                                    l.sBase = i + 1;
                                }
                                else{
                                    break;
                                }
                            }
                            }


                        }    
                    }
                }
                catch(SocketTimeoutException e){}
                catch(SocketException e){}
                catch(Exception e){
                    printError("receive: " + e.getMessage());
                }
            }
            //debug statement
            printDebug("Receive Thread stopped ");
        } 
    }

    private void sendACK(Packet p, int remotePort, String addr) throws Exception{
        Packet pACK = new Packet(p.seq,STATUS_ACK);
        sendDatagram(pACK, remotePort, addr);
    }


    private void recvMessage(byte[] data, int len){
        String s = new String(data);
        s = s.substring(0, len);
        printMessage(s);
    }

    public void sleepMs(int i){
        try{
            Thread.sleep(i);
        }
        catch(Exception e){
        }
    }

    //prints messages
    public static void printMessage(String s){
    System.out.println(s);
    //System.out.print(">>> ");
    }
    //prints error messages
    public static void printError(String s){
        printMessage("Error " + s);
    }
    //print debug messages
    public static void printDebug(String s){
        if (debug){
            printMessage("Debug: " + s);
        }
    }
}