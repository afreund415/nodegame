import java.util.HashMap;
import java.util.Map;

public class Router extends SR {

    String addr = "127.0.0.1";
    HashMap<Short, Route> routeMap = new HashMap<Short, Route>();
    int routeSize = 0;
    boolean hasChanged = true;
    Probe probe;
    Boolean routeRouter = false;

    public Router(int localPort) throws Exception{
        super(localPort, 5, 0, 0);
    }

    public void addRoute(short remotePort, Route route){
        routeMap.put(remotePort, route);
    }

    public void sendRoutes() throws Exception{
        hasChanged = false;
        byte[] byteRoutes = toBytes();
        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            Route r = entry.getValue();
            
            if (r.mode != 'x'){
                sendMessage(byteRoutes, r.dest, addr);
                printMessage("Message was sent from Node " + localPort +
                            " to Node " + r.dest);
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
                    printMessage("received probe message from " + l.remotePort  + " to " + localPort);
                    //Route routeRemote = routeMap.get((short) l.remotePort);
                    //remove later
                    int distance = (l.recvLoss * 100) / l.recvCount; 
                    for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
                        Route routeRemote = entry.getValue();

                        if (routeRemote.next == l.remotePort){
                            routeRemote.dist = (short) (routeRemote.dist - routeRemote.nextDist + distance);
                            routeRemote.nextDist = (short) distance;
                            hasChanged = true;
                            printMessage(localPort + routeRemote.toString() + " probe patch");
                        }
                    }
                    // routeRemote.next = (short) l.remotePort;
                    // routeRemote.dist = (short) distance;
                    
                    startProbing();
                    break;

                case 'r':
                    printMessage("Message received at Node " + localPort + 
                                " from Node " + l.remotePort);
                    //rRemote is origin of routing table
                    Route rRemote = routeMap.get((short)(l.remotePort));
                    //figuring out if the remote router has a better route to us(current node)
                    for (int i = 1; i < len; i+=8){
                        Route rTemp = new Route(b, i);
                        if (rTemp.dest == localPort && rTemp.dist < rRemote.dist){
                            rRemote = rTemp;
                            rRemote.next = (short) l.remotePort; 
                            break;
                        } 
                    }
                    if (rRemote == null){
                        break;
                    }
                    for (int i = 1; i < len; i+=8){
                        Route r = new Route(b, i);
                        //distance to remote port
                        Route rLocal = routeMap.get(r.dest);
                        //printMessage("Route: " + r.toString());
                        if (r.dest == localPort){
                            rLocal = routeMap.get((short) l.remotePort);
                            //checking if we have indirect route from incoming node probe
                            if (rLocal.dest == rLocal.next && r.dist < rLocal.dist){
                                //rLocal.next = r.next;
                                rLocal.dist = r.dist;
                                printMessage(localPort + rLocal.toString() + " reverse");
                                hasChanged = true;
                            }
                            else{
                                continue;
                            }
                        }
                        else if (r.next == localPort){
                            continue;
                        }
                        else if (rLocal == null){
                            rLocal = new Route(r.dest, (short) (r.dist + rRemote.dist), 
                                    rRemote.next, (short) localPort, rRemote.dist, 'x');
                            addRoute(r.dest, rLocal);
                            printMessage(localPort + rLocal.toString() + " new");
                            hasChanged = true;
                        }
                        else if(rLocal.dist > r.dist + rRemote.dist){
                            rLocal.dest = r.dest; 
                            //rLocal.next = (short) l.remotePort;
                            rLocal.next = rRemote.next;
                            rLocal.dist = (short) (r.dist + rRemote.dist);
                            rLocal.nextDist = rRemote.dist;
                            hasChanged = true;
                            printMessage(localPort + rLocal.toString() + " change");
                        }
                    }
                    if (hasChanged){
                        sendRoutes();
                        startProbing();
                        printRouter();
                    }
                    break;
            }
        }
        catch(Exception e){
            printError(e.getMessage());
        }
        finally{
            l.recvLoss = 0;
            l.recvCount = 0;    
        }
    }   
    
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

    private byte[] toBytes(){
        byte[] bytes = new byte[routeMap.size() * 8 + 1];
        bytes[0] = 'r';
        int i = 1;

        for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
            i = entry.getValue().toBytes(bytes, i);
        }
        return bytes;
    }

    public void printRouter(){
        printMessage(toString());
    }

    public void printPacket(Packet p, String leading, String trailing){
        //super.printPacket(p, leading, trailing);
    }
    
    public void sendMessageDone(Link l){}

    public void startProbing(){
        if (probe == null){
            probe = new Probe();
            probe.start();
        }
    }

    class Probe extends Thread{

        public void run(){
            printMessage("Probe thread running " + localPort);
            long millis = System.currentTimeMillis();
        
            try{
                while (running){
                    for (Map.Entry<Short, Route> entry:routeMap.entrySet()){
                        Route r = entry.getValue();

                        if (r.mode == 's'){
                            sendMessage("probe test from " + localPort + " to " + r.dest, r.dest, addr);
                        }                            
                    }
                    sleepMs(100);

                    //send updated routing tables every 5 seconds
                    if (routeRouter && System.currentTimeMillis() - millis > 5000){
                        sendRoutes();
                        millis = System.currentTimeMillis();
                    }
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
