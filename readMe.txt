Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Highlights: 
    1. 3 main programs, built on top of each other, starting with SRNode, run through 3 separate CLIs. 
    2. Can test each program on a single instance using specialized commandline arguments. This is helfpul for analyzing each program quickly. 
    3. Leverages Mulithreading and HashMaps.

Known issues: 
    1. At times I have messy code when it comes to converting between ints and shorts. Apologies. 
    2. For CNNode where link distances are dynamic, I Initialize node distances to 1. This can lead to some odd initial results but eventually the link weights converge to what is expected.  


Installing the project:
    1. Download zip file 
    2. Run <make> in folder

Running:
    1. See separate readMes below on how to run the programs SRNode, DVNode, & CNNode


SRNode readMe: 

    Running the project: 
        1. SRNode on multiple instances: 
            java -cp "./" SRNode <self-port> <peer-port> <window-size> [ -d <value-of-n> | -p <value-of-p>]
        2. SRNode on one instance: 
            java -cp "./" SRNode <self-port> <self-port> <window-size> [ -d <value-of-n> | -p <value-of-p>]
        Note: Simply use same port for sending on receiving to test SRNode on a single instance

    Commandline options SRNode: 
        1. Send <message>, where message is any list of characters that the program splits into single byte packets
        2. sendtest <# of characters> this allows the user to run an emulation test where they send a randomly generated string message (with ACK failures turned off) of n number of characters  
        3. ctrl+C exits the program

    SRNode architecture:

        SRNode: 
            SRNode is the CLI for the selective repeat program. This class allows the user to utilize two send one-off messages to a receiver (on either 1 instance or 2) as well as a larger quantity of characters for testing. The class also validates port numbers and interprets whether the user would like to use deterministic or probabilistic packet dropping.

        SR: 
            The SR class is where the vast majority of the selective repeat logic is implemented. The class contains a constructor and implements send thread/class, receive thread/class, and sendHelper thread/class. The class has several printing methods for errors, strings, and packets, a checkPort validator method, sender and receiver "end-of-message" logic handlers, and an ACK sending method. 

            SR has two sendMessage methods, one for bytes and one for strings. 
            Each message data gets packaged into a send thread. These messages (in the Send class) are then queued on a per-link basis and get started sequentially, with the help of the sendHelper method. Each message is packed into its own thread but each link has its own threadQueue (referred to as a sendQueue in the code). The sendHelper class/thread has two primary jobs: resending timed-out packets and starting the next thread queue once the previous message thread has been handled. Once a send thread gets started, it breaks messages into packets and sends them to the receiver.

            For receiving, all messages are received by the receive thread/class. There, the packets are acknowledged and then recompiled into complete messages. Once a message is received, the receive thread calls the recvMessageDone method to notify the receiver. 


        Link: 
            Link class represents the link between the local port and a specific remote port for sending and receiving messages. Each remote port has its own link object. The class is very short and simply has a constructor that initializes the link's receive and sender windows, the remote port, the loss probability, and the sendQueue, which is an arraylist of type Thread. The link class also contains a number of instance variables that are used in other classes for handling link-level logic. 

        Packet: 
            The packet class represents a single SR packet that is sent or received over the wire. Each packet has a data field, sequence number field, and a status flag. The class contains different types of packet constructors, several byte conversion methods, a copy method that copies the data out of a packet, and a toString method. 



DVNode readMe:

    Running the project: 
        1. DVNode on multiple instances: 
            java -cp "./" DVNode <local-port> <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> ...[next/last]

        2. DVNode testing on one instance: 
            java -cp "./" DVNode <local-port> <neighbor1-port> <loss-rate-1> next <neighbor2-port> <loss-rate-2> next <neighbor3-port> <loss-rate-3> ... [next/last]

        Note: simply type "next" when you want to establish a new link to a node on one instance

        Commandline options DVNode: 
            1. <show> prints out routing table for easier viewing
            2. ctrl+C exits the program

    DVNode architecture: 

        DVNode: 
            DVNode is the CLI for the distance vector algorithm program. It establishes nodes on the network with user-defined distances and links between them and, once all router nodes have been created, propagates the routing tables between all nodes in the network. The CLI allows users to display a node's routing table (or all nodes if running on a single instance) with the command <show>. 

        Router:
            The router class handles all logic for individual nodes for both DVNode and CNNode. The class extends the SR class. The class has methods for constructing router nodes, adding routes to local routing tables, and a sendRoutes method for sending DV tables to neighboring nodes. recvMsgDone overrides the same method in SR. It receives routing tables from neighboring nodes. The class also has several helper methods for converting routing tables to strings, handling byte conversion for sending and receiving, print messages, as well as a probing thread/class for CNNnode. The class also leverages a separate Route class that represents a single route. 

        Route: 
            The route class represents a route between 2 nodes in the network. The constructor creates a route by taking into consideration the route's source port, destination port, next hop port, the route distance, and the route "mode" (sender, receiver etc. primarily for CNNode). The class also maintains an incoming route table for storing routes received from other nodes so we can calculate routes before adding them to the authoritative routing table. The class has a "normal" constructor as well as a byte array constructor for sending and receiving and a few helper methods. 



CNNode readMe: 

    Running the project: 
        1. CNNode on multiple instances:   
            java -cp "./" CNNode <local-port> receive <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> ... <neighborM-port> <loss-rate-M> send <neighbor(M+1)-port> <neighbor(M+2)-port> ... <neighborN-port> [next/last]

        2. CNNnode on one instance: 
            java -cp "./" CNNode <local-port> receive <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> next <neighborM-port> <loss-rate-M> send <neighbor(M+1)-port> <neighbor(M+2)-port> ... <neighborN-port> [next/last]

        Note: simply type "next" when you want to establish a new link to a node on one instance

        One instance testing example: 
            java -cp "./" CNNode 1111 receive send 2222 3333 next 2222 receive 1111 .1 send 3333 4444 next 3333 receive 1111 .5 2222 .2 send 4444 next 4444 receive 2222 .8 3333 .5 send last

        Multiple instances testing example: 
            java -cp "./" CNNode 1111 receive send 2222 3333
            java -cp "./" CNNode 2222 receive 1111 .1 send 3333 4444
            java -cp "./" CNNode 3333 receive 1111 .5 2222 .2 send 4444
            java -cp "./" CNNode 4444 receive 2222 .8 3333 .5 send last

        Commandline options CNNode: 
            1. <show> prints out routing table for easier viewing
            2. ctrl+C exits the program
    
    CNNode architecture: 
        
        CNNode: 
            CNNode is the CLI for the dynamic distance vector program, in which probes are sent out and routing tables are adjusted in real time to respond to lossed packages ocurring over links. The program functions very much like DVNode with the exception of the introduction of probing messages and dyanmic link distance changes (based on lost packets). CNNode utilizes SR infrastructure to exchange routes in addition to the probing packages. The program allows a user to display routing tables, like in DVNode, with a <show> command.

        Router:
             The probe thread/class inside the router class sends out periodic probes to user-designated neighbors. These probes get received in the recvMessageDone method and the newly discovered loss ratio gets replaced in the respective routing table. The calcRoutes function, calculates routes based on external routing table updates and probe results. The function takes a number of factors into consideration before updating a specific route. Unlike probes which are broken into single byte packets, the routes are sent in a single SR packet and are exempt from all loss probabilities and lost ACKs. During my implementation, I noticed that the SR send threads got flooded by probing packets. I then added a sendReady method that drops probe packets if the sendQueue has more than 3 pending messages (I reserve two spots for DV routing messages). 

        Additional classes: 
            CNNode, since the architecture of the project builds on top of each other, also relies on the previously described classes of Link, Packet, SR, and Route. 
        
    


