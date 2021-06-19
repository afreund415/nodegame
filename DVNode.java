



import java.util.HashMap;
import java.util.Scanner;



public class DVNode {
    static Boolean running = false;
    static String addr = "127.0.0.1"; 
    static HashMap<Integer, Router> routers = new HashMap<Integer, Router>();

    //DVNode
    public static void main(String[] args) throws Exception {
        System.out.println("DV Node started");
        argParse(args);
        Scanner input = new Scanner(System.in);

        while (running){
            String s = input.nextLine().trim();
            String[] newArgs = s.split(" ");
            
            switch (newArgs[0]){

                case "show":   
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
            int lPort; 
            Router router; 

            do{
                if (args.length > pos){
                    lPort = Integer.parseInt(args[pos++]);
                    router = new Router(lPort);
                    routers.put(lPort, router);
                    running = true;
                }
                
                else {
                    return;
                }
                       
                while(args.length > pos + 1 && args[pos].charAt(0) >= '0' &&  args[pos].charAt(0) <= '9'){
                    int remotePort = Integer.parseInt(args[pos++]);
                    int dist = (Math.round(Float.parseFloat(args[pos++]) * 100));
                    Route r = new Route(remotePort, dist, remotePort, lPort);
                    router.addRoute(remotePort, r);
                }

                if (args.length > pos){

                    switch(args[pos++]){

                        case "last":
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
