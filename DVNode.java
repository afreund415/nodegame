
/* 
Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2
*/


import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;



public class DVNode {
    static Boolean running = false;
    static String addr = "127.0.0.1"; 
    static HashMap<Short, Router> routers = new HashMap<Short, Router>();

    //DVNode
    public static void main(String[] args) throws Exception {
        System.out.println("DV Node started");
        argParse(args);
        Scanner input = new Scanner(System.in);
        
        while (running){
            String s = input.nextLine().trim();
            String[] newArgs = s.split(" ");
            
            switch (newArgs[0]){

                //allows user to display node routing tables 
                case "show":   
                    for (Map.Entry<Short, Router> entry:routers.entrySet()){
                        entry.getValue().printRouter();
                    }
                    break;

                default:
                    SR.printError("Unknown command");
            }     
        }
        input.close();
    }

    private static void argParse(String[] args) {
      
        try { 
            int pos = 0;
            short lPort; 
            Router router; 

            do{
                //getting local port #
                if (args.length > pos){
                    lPort = Short.parseShort(args[pos++]);
                    router = new Router(lPort);
                    routers.put(lPort, router);
                    running = true;
                }
                
                else {
                    return;
                }
                //gets remote port and distance
                while(args.length > pos + 1 && args[pos].charAt(0) >= '0' &&  args[pos].charAt(0) <= '9'){
                    short remotePort = Short.parseShort(args[pos++]);
                    short dist = (short) (Math.round(Float.parseFloat(args[pos++]) * 100));
                    Route r = new Route(remotePort, dist, remotePort, lPort, 'd');
                    router.addRoute(remotePort, r);
                }
                router.printRouter();

                if (args.length > pos){

                    switch(args[pos++]){

                        case "last":
                            router.sendRoutes();
                            return;
                        
                        case "next":
                            break;

                        default: 
                            return;
                    }
                }
            } while(true);
        }
        catch (Exception e) {
            running = false;
            SR.printError(e.getMessage());
        }
    }
}
