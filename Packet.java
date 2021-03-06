/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Packet class represents a single packet that is sent or received over the wire
Each packet has data, a sequence number, and a status flag 
*/

import java.util.Arrays;

public class Packet {

    int seq; 
    byte status; 
    byte[] data;
    long millis; 
    
    
    //packet constructor 
    public Packet(byte[] data, int seq, byte status){

        this.data = new byte[5+ data.length];
        this.seq = seq;
        this.status = status;
        System.arraycopy(data, 0, this.data, 5, data.length);
    }
    
    //ACK packet constructor 
    public Packet(int seq, byte status){
        //dataless packet for acks
        this.data = new byte[5];
        this.seq = seq;
        this.status = status;
    }

    //receiving packet
    public Packet(byte[] data, int len){
        this.data = new byte[len];
        System.arraycopy(data, 0, this.data, 0, len);
        this.status = this.data[0];
        this.seq = fromByteArray(this.data, 1);
    }

    //converts to bytes
    public byte[] toBytes(){
        data[0] = status; 
        toByteArray(seq, data, 1);
        return data;
    }

    //copies packet 
    public int copy(byte[] bytes, int index){
        int len = data.length - 5;
        System.arraycopy(data, 5, bytes, index, len);        
         return len;
    }

    //gets packet data length 
    public int length(){
        return data.length;
    }

    //packet to string method 
    public String toString(){
        String out = (status & SR.STATUS_ACK) !=0 ? "ACK ":"packet ";
        out += seq;
        if (data.length > 5){
            byte[] msg = Arrays.copyOfRange(data, 5, data.length);
            out += " \"" + new String(msg) + "\"";
        }
        return out;
    }
    
    //converts from byte array 
    private int fromByteArray(byte[] bytes, int start) {
        return ((bytes[start] & 0xFF) << 24) | 
               ((bytes[start + 1] & 0xFF) << 16) | 
               ((bytes[start + 2] & 0xFF) << 8 ) | 
               ((bytes[start + 3] & 0xFF) << 0 );
    }

    //converts to byte array 
    private void toByteArray(int value, byte[] bytes, int start) {
        bytes[0+start] = (byte) (value >> 24); 
        bytes[1+start] = (byte) (value >> 16);
        bytes[2+start] = (byte) (value >> 8); 
        bytes[3 + start] = (byte) (value);
    }
}
