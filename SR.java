
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2
*/
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class SR{


    int localPort; 
    int windw; 
    int lossProb; 
    int dLoss; 
    Receive receive; 
    SendHelper sendHelper;
    public Random random = new Random();
    int deterCount = 0;
    DatagramSocket ds;
    boolean running;
    HashMap<Integer, Link> links = new HashMap<Integer, Link>();
    boolean noDropACK = false;
    static boolean debug = false;
    static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    final static byte STATUS_MSG = 0x01; 
    final static byte STATUS_EOM = 0x02; 
    final static byte STATUS_ACK = 0x04; 
    //constant for checking ACK
    final static byte STATUS_MOK = 0x08; 



    public SR(int lPort, int windw, int dLoss, int pLoss) throws Exception{

        this.localPort = lPort;
        this.windw = windw; 
        this.dLoss = dLoss;
        this.lossProb = pLoss; 
        
        receive = new Receive();
        sendHelper = new SendHelper();
        ds = new DatagramSocket(lPort);
        running = true;
        ds.setSoTimeout(500);
        receive.start();
        sendHelper.start();
    }

    public Link getLink(int remotePort){
        Link l = links.get(remotePort);

        if (l == null){
            l = new Link(remotePort, windw, lossProb);
            links.put(remotePort, l);
        }
        return l;
    }

    public void sendMessage(String message, int remotePort, String addr) throws Exception{

        Send send = new Send(message.getBytes(), message.length(), remotePort, addr, false);
        //send.start();
        send.l.sendQueue.add(send);
    }

    public void sendMessage(byte[] message, int remotePort, String addr) throws Exception{

        Send send = new Send(message, message.length, remotePort, addr, true);
        //send.start();
        send.l.sendQueue.add(send);
    }

    public void sendDatagram(Packet p, int remotePort, String addr) throws Exception{
        InetAddress ip = InetAddress.getByName(addr);
        DatagramPacket dp = new DatagramPacket(p.toBytes(), 
                                    p.length(), ip, remotePort);
        ds.send(dp);
    }

    public boolean dropPacket(Packet p, Link l){
       
        ///allows us to toggle dropped acks off and on
        if (noDropACK && (p.status & STATUS_ACK) != 0){
            return false;
        }
        l.recvCount++;
        
        if (l.lossProb != 0 && random.nextInt(100) <= l.lossProb) {
            l.recvLoss++;
            return true;
        }
        deterCount ++;
        if (dLoss != 0 && ((deterCount % dLoss) == 0)){
            l.recvLoss++;
            return true;
        }
        return false;
    }

    class Send extends Thread{
        int remotePort; 
        byte[] data; 
        String addr; 
        Link l;
        int len;
        boolean sendSinglePacket = false; 

        public Send(byte[] data, int len, int remotePort, String addr,boolean sendSinglePacket){
            this.data = data; 
            this.len = len;
            this.remotePort = remotePort; 
            this.addr = addr;  
            this.sendSinglePacket = sendSinglePacket;
            l = getLink(remotePort);    
        }
        public void run(){

            try{
                if (sendSinglePacket){
                    while(l.sNext-l.sBase >= windw){
                        sleepMs(100);
                    }
                    Packet p = new Packet(data, l.sNext, (byte) (STATUS_EOM | STATUS_MSG));
                    sendPacket(p);
                }
                else{
                    for (int i = 0; i < len; i++){
                        while(l.sNext-l.sBase >= windw){
                            sleepMs(100);
                        }
                        byte status = (i == len - 1) ? STATUS_MSG | STATUS_EOM:STATUS_MSG;
                        byte[] b = new byte[1];
                        b[0] = data[i];
                        Packet p = new Packet(b,l.sNext, status);
                        sendPacket(p);
                    }
                }
            }

            catch(Exception e){
                printError(e.getMessage() + " Send Thread");
            }
            l.sending = null;
            sendMessageDone(l);
        }

        private void sendPacket(Packet p) throws Exception{
            p.millis = System.currentTimeMillis();
            l.sWindow[l.sNext++ % windw] = p;
            sendDatagram(p, remotePort, addr);
            l.sendCount++;
            printPacket(p, "", "sent");
        }
    }
    

    //resending thread for when packets timeout
    class SendHelper extends Thread{

        public void run(){

            try{
                while(running){
                    long millis = System.currentTimeMillis();
                    for (Map.Entry<Integer, Link> entry:links.entrySet()){
                        Link l = entry.getValue();

                        for (int i = l.sBase; i < l.sNext; i++){
                            Packet p = l.sWindow[i % windw];

                            if ((p != null) && (p.status & STATUS_MOK) == 0 && (millis - p.millis > 500)){
                                p.millis = System.currentTimeMillis();
                                sendDatagram(p, l.remotePort, l.addr);
                                printPacket(p, "", "timeout, resending");
                                l.sendCount++;
                                l.sendLoss++;
                            }
                        }
                        if (l.sending == null && l.sendQueue.size() > 0){
                            l.sending = l.sendQueue.get(0); 
                            if (l.sending != null){
                                try{
                                    l.sending.start();
                                    l.sendQueue.remove(0);
                                }
                                catch(Exception e){
                                    printError("Couldn't access sendQueue " + e.getMessage());
                                    l.sending = null; 
                                }
                            }
                        }
                    }
                    sleepMs(100);
                }
            }
            catch(Exception e){
                printMessage(e.getMessage() + " SendHelper Thread");
            }
        }
    }


    class Receive extends Thread{

        public void run(){
            //debug statement
            printMessage("Receive Thread started " + localPort);
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

                        if (dropPacket(p, l)){
                            printPacket(p, "", "dropped");
                            continue;
                        }    

                        if ((p.status & STATUS_MSG) != 0){
                            sendACK(p, remotePort, addr, l);
                            Packet pRecv = l.rWindow[p.seq % windw];
                           
                            if (pRecv != null && pRecv.seq == p.seq){
                                printPacket(p, "duplicate ", "received, discarded");
                                continue;
                            }
                            l.rWindow[p.seq % windw] = p;
                            if (p.seq >= l.rNext){
                                l.rNext = p.seq + 1;
                                printPacket(p, "", "received");
                            }
                            else{
                                printPacket(p, "", "received out of order, buffered");
                            }

                            for (int i = l.rBase; i < l.rNext; i++){
                                pRecv = l.rWindow[i % windw];

                                if (pRecv != null && pRecv.seq == i){

                                    //get rid of data that exceeds internal buffer length 
                                    if (l.recvIndex + p.length() <= l.recvData.length){
                                        l.recvIndex += pRecv.copy(l.recvData, l.recvIndex);
                                    }       
                                    l.rBase = i + 1;


                                    if ((pRecv.status & STATUS_EOM) !=0){
                                        recvMessageDone(l);
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
                            printPacket(p, "", "received, sender window starts at " + l.sBase);
                        }    
                    }
                }
                catch(SocketTimeoutException e){}
                catch(SocketException e){}
                catch(Exception e){
                    printError("receive thread: " + e.getMessage());
                }
            }
            //debug statement
            printMessage("Receive Thread ending " + localPort);
            printDebug("Receive Thread stopped ");
        } 
    }

    private void sendACK(Packet p, int remotePort, String addr, Link l) throws Exception{
        Packet pACK = new Packet(p.seq,STATUS_ACK);
        sendDatagram(pACK, remotePort, addr);

        if (!noDropACK){
            l.sendCount++;
        }
        printPacket(pACK, "", "sent, window starts at " +  l.rBase);
    }


    public void recvMessageDone(Link l){
        String s = new String(l.recvData);
        s = s.substring(0, l.recvIndex);
        int lossRate =  100 * l.recvLoss / l.recvCount;
        printMessage("Message received: \"" + s + "\"");
        printMessage("Summary (Receiver): " + l.recvLoss + "/" + l.recvCount + 
            " packets dropped, loss rate = " + lossRate + "%");
        l.recvLoss = 0;
        l.recvCount = 0;
    }

    public void sendMessageDone(Link l){

        int lossRate = 100 * l.sendLoss / l.sendCount;
        printMessage("Summary (Sender): " + l.sendLoss + "/" + l.sendCount + 
        " packets dropped, loss rate = " + lossRate + "%");
        l.sendCount = 0;
        l.sendLoss = 0;

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
        Date date = new Date();
        System.out.println(formatter.format(date) + " " + s);
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

    //print packet
    public void printPacket(Packet p, String leading, String trailing){
        String out; 
        out = " "  + leading + (p==null?"":p.toString()) + " " + trailing;         
        printMessage(out);
    }
}