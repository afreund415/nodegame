



//represents a single packet that is sent or received over the wire

public class Packet {

    int seq; 
    byte status; 
    byte[] data;
    
    //sending packet
    public Packet(byte[] data, int seq, byte status){

        this.data = new byte[5+ data.length];
        this.seq = seq;
        this.status = status;
        System.arraycopy(data, 0, this.data, 5, data.length);
    }

    //receiving packet
    public Packet(byte[] data, int len){
        this.data = new byte[len];
        System.arraycopy(data, 0, this.data, 0, len);
        this.status = this.data[0];
        this.seq = fromByteArray(this.data, 1);
    }

    public byte[] toBytes(){
        data[0] = status; 
        toByteArray(seq, data, 1);
        return data;
    }

    public int copy(byte[] bytes, int index){
        int len = data.length - index;

        System.arraycopy(data, 5, bytes, index, len);
        
         return len;
    }
    
    
    private int fromByteArray(byte[] bytes, int start) {
        return ((bytes[start] & 0xFF) << 24) | 
               ((bytes[start + 1] & 0xFF) << 16) | 
               ((bytes[start + 2] & 0xFF) << 8 ) | 
               ((bytes[start + 3] & 0xFF) << 0 );
   }

   private void toByteArray(int value, byte[] bytes, int start) {
        bytes[0+start] = (byte) (value >> 24); 
        bytes[1+start] = (byte) (value >> 16);
        bytes[2+start] = (byte) (value >> 8); 
        bytes[3 + start] = (byte) (value);
   }


}
