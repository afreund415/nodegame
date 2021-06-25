/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Route class creates a single route object between 2 nodes that 
router nodes can use to update their tables
*/

import java.util.*;

public class Route {

    short dest; 
    short dist;
    short next; 
    short port; 
    int nextDist; 
    char mode;
    HashMap<Short, Route> incomingRoutes = new HashMap<Short, Route>();

    //short route constructor  
    public Route(short dest, short dist, short next, short port, char mode){
        this.dest = dest;
        this.dist = (dest != port && dist == 0)?100: dist;
        this.next = next; 
        this.port = port;
        this.mode = mode;
    }

    //byte route constructor 
    public Route(byte[] byteRoutes, int index){
        dest = fromByteArray(byteRoutes, index + 0);
        dist = fromByteArray(byteRoutes, index + 2);
        next = fromByteArray(byteRoutes, index + 4);
        port = fromByteArray(byteRoutes, index + 6);
    }

    //adds route from probe and neighboring node DV updates to hashmap 
    public void addExternalRoute(Route newRoute){
        if (incomingRoutes != null){
            incomingRoutes.put(newRoute.dest, newRoute);
        }
    }

    //route to string helper method
    public String toString(){
        String outStr;
        float fDist = (float) dist / 100; 

        outStr = "- (" + String.format("%.2f", fDist) + ") —> " + "Node " + dest; 
        if (next != dest){
            outStr += "; Next hop —> Node " + next;
        }
        
        return outStr; 
    }

    //clones route to avoid recursion issues 
    public Route clone(){
        byte[] b = new byte[8]; 
        toBytes(b, 0);
        return new Route(b, 0);
    }

    //converts shorts to bytes for router threads
    public int toBytes(byte[] bytes, int start){
        shortToByteArray(dest, bytes, start);
        shortToByteArray(dist, bytes, start + 2);
        shortToByteArray(next, bytes, start + 4);
        shortToByteArray(port, bytes, start + 6);
        return start + 8; 
    }

    //converts shorts to byte
    private void shortToByteArray(short value, byte[] bytes, int start){
        bytes[0 + start] = (byte) (value >> 8); 
        bytes[1 + start] = (byte) (value);
    }

    //converts from byte array
    private short fromByteArray(byte[] bytes, int start) {
        return (short) (((bytes[start + 0] & 0xFF) << 8 ) | 
               ((bytes[start + 1] & 0xFF) << 0 ));
    }
}
