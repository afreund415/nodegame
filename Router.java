/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Router (with an R) class includes individual router node behavior and network logic
*/

import java.util.*;

public class Router extends SR {

    String addr = "127.0.0.1";
    HashMap<Short, Route> routeMap = new HashMap<Short, Route>();
    int routeSize = 0;
    boolean hasChanged = true;
    Probe probe;
    short localPort;
    Route localRoute;
    public boolean probing = false; 
    final short infinity = 0x7fff;

    //router node constructor
    public Router(int localPort) throws Exception{
        super(localPort, 5, 0, 0);
        this.localPort = (short) super.localPort;
        localRoute = new Route((short) localPort, (short) 0, 
                    (short) localPort, (short) localPort, 'r');
        routeMap.put((short) localPort, localRoute);
    }

    //adds new route to node's router table
    public void addRoute(short remotePort, Route route){
        routeMap.put(remotePort, route);
        //initializes distances to direct neighbors 
        if (route.dest == route.next){
            localRoute.addExternalRoute(route.clone());
        }
    }
    
    //sends out new routes 
    public void sendRoutes() throws Exception{
        byte[] byteRoutes = toBytes();
        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            Route r = entry.getValue();
            
            if (r.mode != 'x' && r.dest != localPort && sendReady(r.dest, 5)){
                sendMessage(byteRoutes, r.dest, addr);
                printMessage("Route was sent from Node " + localPort +
                            " to Node " + r.dest);
            }
        }
        hasChanged = false;
        sleepMs(100);
    }

    //recalculates all node routes based on external routing table updates + probes
    private void calcRoutes(){
        if(probing){    
            for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
                Route r = entry.getValue();
                if (r.dest != localPort){
                    r.dist = infinity;
                }
            }
        }
        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            Route r = entry.getValue();

            if (r.incomingRoutes != null){
                Route rToUs = r.incomingRoutes.get(localPort);
                int distance = 0; 
                if (rToUs != null){
                    distance = rToUs.dist;
                }
                for (Map.Entry<Short, Route> rEntry:r.incomingRoutes.entrySet()){
                    Route rCandidate = rEntry.getValue();
                    
                    if (rCandidate.dest != localPort && rCandidate.next != localPort){
                        Route currentRoute = routeMap.get(rCandidate.dest); 
                        short next = rCandidate.port == localPort? rCandidate.next:rCandidate.port;

                        if (currentRoute != null){
                            if (currentRoute.dist > rCandidate.dist + distance){
                                currentRoute.dist = (short) (rCandidate.dist + distance);
                                currentRoute.next = next;
                                currentRoute.nextDist = distance;
                                hasChanged = true;
                            }
                            //tiebreaker for routes with equal distance 
                            else if (currentRoute.dist == rCandidate.dist + distance && 
                                    currentRoute.nextDist > distance){
                                // updates next hop
                                currentRoute.next = next;
                                //updates current route's next distance 
                                currentRoute.nextDist = distance;  
                            }
                        }
                    }
                }
            }
        }
    }

    //Once SR receives whole message, recvMessageDone handles logic 
    public void recvMessageDone(Link l){
        // b/len gives us # of total routes
        byte[] b = l.recvData;
        int len = l.recvIndex;

        try{

            switch (b[0]){

                //incoming probe case
                case 'p':
                    int dist = l.recvLoss * 100 / l.recvCount;
                    // dist = l.lossProb;
                    printMessage("Received probe message from " + l.remotePort  + 
                                " to " + localPort);
                    Route rProbe = new Route((short) l.remotePort,(short) dist,
                                            (short) l.remotePort, localPort, 'x');
                    //adding probe route to externalRoute map for processing 
                    localRoute.addExternalRoute(rProbe);
                    calcRoutes();
                    break;

                //incoming routing table case
                case 'r':
                    printMessage("Route received at Node " + localPort + 
                                " from Node " + l.remotePort);
                    //rRemote is origin of routing table
                    Route rRemote = routeMap.get((short)(l.remotePort));
                    //figuring out if the remote router has a better route to our node)
                    
                    if (rRemote == null){
                        break;
                    }
                    //adding external routing table to node routing table 
                    for (int i = 1; i < len; i+=8){
                        Route r = new Route(b, i);
                        rRemote.addExternalRoute(r);

                        //if route does not exist, we add the route w/ distance 
                        //infinity to enable calcRoutes to update it
                        if (routeMap.get(r.dest) == null){
                            routeMap.put(r.dest, new Route(r.dest, infinity,
                                                    r.dest, (short) localPort, 'x'));
                        }
                    }
                    calcRoutes();
                    break;
            }
            if (hasChanged){
                if (!probing){
                    sendRoutes();
                }
                startProbing();
                printRouter();
            }
        }
        catch(Exception e){
            printError(e.getMessage());
        }
    }   
    
    //converts routing table to string
    public String toString(){
        String outStr; 
        //Timestamp added via SR.printmessage
        outStr = " Node " + this.localPort + " Routing table \n"; 
        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            outStr += entry.getValue().toString(); 
            outStr += "\n";
        }
        return outStr;
    }

    //converts to bytes
    private byte[] toBytes(){
        byte[] bytes = new byte[routeMap.size() * 8 + 1];
        bytes[0] = 'r';
        int i = 1;

        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            i = entry.getValue().toBytes(bytes, i);
        }
        return bytes;
    }

    //prints router 
    public void printRouter(){
        printMessage(toString());
    }

    //overrides printPacket from SRNode
    public void printPacket(Packet p, String leading, String trailing){}
    
    //overriding sendMessageDone from SRNode
    public void sendMessageDone(Link l){}

    //starts probing 
    public void startProbing(){
        if (probe == null && probing){
            probe = new Probe();
            probe.start();
        }
    }

    //probe sending thread 
    class Probe extends Thread{

        public void run(){
            printMessage("Probe thread running " + localPort);
            long millis = 0;
            long sendMillis = 0; 
        
            try{

                while (running){
                    //send routing tables every 5 seconds
                    if (System.currentTimeMillis() - millis > 5000){
                        sendRoutes();
                        millis = System.currentTimeMillis();
                    }
                    else{
                        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
                            Route r = entry.getValue();

                            if (r.mode == 's'){
                                if (System.currentTimeMillis() - sendMillis > 1000){
                                    Link l = getLink(r.dest);
                                    printMessage("Link from " + localPort + " to " + l.remotePort + ": " + 
                                                    l.sendCount + " packets sent, " + l.sendLoss + 
                                                    " packets lost, loss rate " + 
                                    (l.sendLoss * 100 / l.sendCount) + "%");
                                    sendMillis = System.currentTimeMillis();
                                }
                                if (sendReady(r.dest, 3)){
                                    sendMessage("probe>" + localPort + 
                                                ">" + r.dest, r.dest, addr);
                                }  
                            }                          
                        }
                    }
                    sleepMs(500);
                }
            }
            catch (Exception e){
                printError("Probe Thread " + e.getMessage());
            }
            probe = null;
            printMessage("Probe thread ending " + localPort);
        }
    }
}
