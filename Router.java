import java.util.HashMap;

public class Router extends SR {

    HashMap<Integer, Route> routeMap = new HashMap<Integer, Route>();

    public Router(int localPort) throws Exception{
        super(localPort, 5, 0, 0);
    }


    public void addRoute(int remotePort, Route route){

        routeMap.put(remotePort, route);
    }
    
}
