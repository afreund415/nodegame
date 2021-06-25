Andreas Carlos Freund
Acf2175
CSEE-4119 Computer Networks
Programming Assignment #2


Highlights: 
    1. 3 main programs, building on one another starting with SRNode, run through 3 separate CLIs. 
    2. Can test each program on a single instance using specialized commandline arguments. This is helfpul for analyzing DVNode and CNNode.
    3. Leverages Mulithreading, HashMaps, ArrayLists, and native java packages for all handling

Known issues: 
    1. At times I have messy code when it comes to converting between ints and shorts. Apologies. 
    2. 


Installing the project:
    1. Download zip file 
    2. Run <make> in folder

Running:
    1. See separate readMes below on the programs: SRNode, DVNode, & CNNode


SRNode readMe: 

    Running the project: 
        1. SRNode on multiple instances: 
            java -cp "./" SRNode <self-port> <peer-port> <window-size> [ -d <value-of-n> | -p <value-of-p>]
        2. SRNode on one instance: 
            java -cp "./" SRNode <self-port> <self-port> <window-size> [ -d <value-of-n> | -p <value-of-p>]
        Note: simply use your own port twice to test SRNode on 1 instance

    Commandline options SRNode: 
        1. Send <message>, where message is any list of characters that the program can split into packets
        2. sendtest <# of characters> this allows the user to run an emulation test where they send a message (with ACK failures turned off) of however many characters they want  
        3. ctrl+C exits the program

    SRNode architecture:

        SRNode: 

        SR: 

        Link: 

        Packet: 







DVNode readMe:

    Running the project: 
        1. DVNode on multiple instances: 
            java -cp "./" DVNode <local-port> <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> ... [last]

        2. DVNode testing on one instance: 
            java -cp "./" DVNode <local-port> <neighbor1-port> <loss-rate-1> next <neighbor2-port> <loss-rate-2> next <neighbor3-port> <loss-rate-3> ... [last]

        Note: simply type "next" when you want to establish a new link to a node on one instance

        Commandline options DVNode: 
            1. <show> prints out routing table for easier viewing
            2. ctrl+C exits the program

    DVNode architecture: 

        DVNode: 

        SR: 

        Link: 

        Packet: 

        Route: 

        Router:



CNNode readMe: 

    Running the project: 
        1. CNNode on multiple instances:   
            java -cp "./" CNNode <local-port> receive <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> ... <neighborM-port> <loss-rate-M> send <neighbor(M+1)-port> <neighbor(M+2)-port> ... <neighborN-port> [last]

        2. CNNnode on one instance: 
            java -cp "./" CNNode <local-port> receive <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> next <neighborM-port> <loss-rate-M> send <neighbor(M+1)-port> <neighbor(M+2)-port> ... <neighborN-port> [last]

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
        CNNode utilizes SR infrastructure to exchange routes in addition to the probing packages. However, these routes and probes are sent in one SR packet and are exempt from all loss probabilities and lost ACKs. 

        CNNode: 

        SR: 

        Link: 

        Packet: 

        Route: 

        Router:
        
    


