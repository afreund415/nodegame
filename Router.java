/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Router (with an R) class includes individual router node behavior and network logic
*/


import java.util.HashMap;
import java.util.Map;

public class Router extends SR {

    String addr = "127.0.0.1";
    HashMap<Short, Route> routeMap = new HashMap<Short, Route>();
    int routeSize = 0;
    boolean hasChanged = true;
    Probe probe;
    Boolean routeRouter = false;
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
    
    public void sendRoutes() throws Exception{
        byte[] byteRoutes = toBytes();
        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            Route r = entry.getValue();
            
            if (r.mode != 'x' && r.dest != localPort){
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
                if (r.dest != r.next && r.dest != localPort){
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

                        if (currentRoute != null){
                            if (currentRoute.dist > rCandidate.dist + distance || currentRoute.update){
                                currentRoute.dist = (short) (rCandidate.dist + distance);
                                currentRoute.next = rCandidate.port;
                                currentRoute.update = false;
                                hasChanged = true;
                            }
                        }
                    }
                }
            }
        }
    }


    public void recvMessageDone(Link l){
        // b/len gives us # of total routes
        byte[] b = l.recvData;
        int len = l.recvIndex;

        try{

            switch (b[0]){

                case 'p':
                    int dist = l.recvLoss * 100 / l.recvCount;
                    printMessage("Received probe message from " + l.remotePort  + 
                                " to " + localPort + " " + dist);
                    Route rProbe = new Route((short) l.remotePort,(short) dist,
                                            (short) l.remotePort, localPort, 'x');
                    //adding probe route to externalRoute map for processing 
                    //routeMap.get(localPort).addExternalRoute(rProbe);
                    localRoute.addExternalRoute(rProbe);
                    calcRoutes();
                    break;

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
            long millis = System.currentTimeMillis();
        
            try{

                while (running){
                    //send routing tables every 5 seconds
                    if (hasChanged && System.currentTimeMillis() - millis > 5000){
                        sendRoutes();
                        millis = System.currentTimeMillis();
                    }
                    //else{ 
                    for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
                        Route r = entry.getValue();

                        if (r.mode == 's'){
                            sendMessage("probe test from " + localPort + 
                                        " to " + r.dest, r.dest, addr);
                            //sleepMs(10);
                        }                            
                    }
                    //}
                    sleepMs(250);
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
