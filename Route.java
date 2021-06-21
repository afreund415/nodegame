

public class Route {

    short dest; 
    short dist;
    short next; 
    short port; 
    short nextDist;
    char mode;


    public Route(short dest, short dist, short next, short port, short nextDist, char mode){
        this.dest = dest;
        this.dist = dist;
        this.next = next; 
        this.port = port;
        this.nextDist = nextDist; 
        this.mode = mode;
    }


    public Route(byte[] byteRoutes, int index){
        dest = fromByteArray(byteRoutes, index + 0);
        dist = fromByteArray(byteRoutes, index + 2);
        next = fromByteArray(byteRoutes, index + 4);
        port = fromByteArray(byteRoutes, index + 6);
    }

  
    public String toString(){
        String outStr;
        float fDist = (float) dist / 100; 

        outStr = "- (" + String.format("%.2f", fDist) + ") —> " + "Node " + dest; 
        outStr += "[" + nextDist + "] ";
        if (next != dest){
            outStr += "; Next hop —> Node " + next;
        }
        return outStr; 
    }

    public int toBytes(byte[] bytes, int start){
        shortToByteArray(dest, bytes, start);
        shortToByteArray(dist, bytes, start + 2);
        shortToByteArray(next, bytes, start + 4);
        shortToByteArray(port, bytes, start + 6);
        return start + 8; 
    }

    private void shortToByteArray(short value, byte[] bytes, int start){
        bytes[0 + start] = (byte) (value >> 8); 
        bytes[1 + start] = (byte) (value);
    }

    private short fromByteArray(byte[] bytes, int start) {
        return (short) (((bytes[start + 0] & 0xFF) << 8 ) | 
               ((bytes[start + 1] & 0xFF) << 0 ));
    }


    
}
