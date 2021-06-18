
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2
*/

import java.util.Scanner;

public class SRNode {

    static Boolean running = false;
    static int remotePort;
    static SR node;
    static String addr = "127.0.0.1"; 

    public static void main(String[] args) throws Exception {
        // System.out.println("Hello, World!");
        argParse(args);

        Scanner input = new Scanner(System.in);

        while (running){

            String s = input.nextLine().trim();

            if (s.indexOf("send") == 0){
                s = s.substring(5);
                node.sendMessage(s, remotePort, addr);  
            }
        }

        input.close();
    }

    private static void argParse(String[] args) {

        try {
            int lPort = Integer.parseInt(args[0]);
            remotePort = Integer.parseInt(args[1]);
            int windw = Integer.parseInt(args[2]);
            int dLoss = 0;
            int pLoss = 0;

            switch (args[3]) {

                case "-d":
                    dLoss = Integer.parseInt(args[4]);

                    break;

                case "-p":
                    pLoss = (Math.round(Float.parseFloat(args[4])) * 100);
                    break;
                default:
                    System.out.println(("nada given"));
            }

            node = new SR(lPort, windw, dLoss, pLoss);
            running = true; 

        }

        catch (Exception E) {
        }
        ;

    }

}