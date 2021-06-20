import java.util.HashMap;
import java.util.Map;

public class Router extends SR {

    String addr = "127.0.0.1";
    HashMap<Short, Route> routeMap = new HashMap<Short, Route>();
    int routeSize = 0;
    boolean hasChanged = true;

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
            
            if (r.mode == 'd'){
                sendMessage(byteRoutes, r.dest, addr);
            }
        }

    }


    public void recvMessageDone(Link l){
        // b/len gives us # of total routes
        byte[] b = l.recvData;
        int len = l.recvIndex;
        try{

            switch (b[0]){

                case 'r':

                    printMessage(localPort + " received routes");
                    //rRemote is origin of routing table
                    Route rRemote = routeMap.get((short)(l.remotePort));

                   
                    //figuring out if the remote router has a better route to us(current node)
                    for (int i = 1; i < len; i+=8){
                        Route rTemp = new Route(b, i);
                        if (rTemp.dest == localPort && rTemp.dist < rRemote.dist){
                            rRemote = rTemp;
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
                        printMessage("Route: " + r.toString());

                        if (r.dest == localPort || r.next == localPort){
                            continue;
                        }

                        if (rLocal == null){
                            rLocal = new Route(r.dest, (short) (r.dist + rRemote.dist), 
                                    (short) l.remotePort,(short) localPort, 'x');
                            addRoute(r.dest, rLocal);
                            hasChanged = true;
                        }
                        else if(rLocal.dist > r.dist + rRemote.dist){
                            rLocal.dest = r.dest; 
                            rLocal.next = rRemote.next;
                            rLocal.dist = (short) (r.dist + rRemote.dist);
                            hasChanged = true;
                        }
                    }
                    if (hasChanged){
                        sendRoutes();
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
        return;
    }
}
