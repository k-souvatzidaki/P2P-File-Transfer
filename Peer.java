import java.util.*;
import java.net.*;
import java.io.*;

public class Peer {
    String directory,ip;
    int port; 
    File[] files; ArrayList<String> file_names;
    int token_id;
    boolean loggedin;
    ArrayList<String> all_files;

    /** Constructor */
    public Peer(String ip, int port, String directory) {
        this.ip = ip;
        this.port = port;
        this.directory = directory;
        loggedin = false;
        token_id = 0;
        init();
        connect();
        if(loggedin) {
            start();
        }
    }//Peer

    /** Initialize files of peer */
    public void init() {
        file_names = new ArrayList<String>();
        File dir = new File(directory);
        File[] files = dir.listFiles();
        for (File f : files) {
            file_names.add(f.getName());
        }                                                 
    }//init

    /** Connect to the system: Register and Login */
    public void connect() {
        System.out.println("Welcome to peer. Register or Login?\n1) Register\n2) Login\n0) Exit");
        Scanner in = new Scanner(System.in);
        String user_input; int option = -1;
        while(option!= 1 && option!=2 ) {
            user_input = in.nextLine();
            try {
                option = Integer.parseInt(user_input);
            }catch(NumberFormatException e) { continue; }
           if(option == 0) break;
        }
        //REGISTER
        if(option == 1) {
            register();
        }
        //LOGIN
        else if(option ==2) {
            login(); 
        }
    }//connect

    /** Start 2 threads - one to receive requests and one to send requests */
    public void start() {
        //ACCEPT REQUESTS
        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket; ServerSocket requests;
                try {
                    requests = new ServerSocket(port,20);
                    System.out.println("start accepting requests");
                    while(true) {
                        socket = requests.accept();
                        System.out.println("New request!");

                        //new thread for each request
                        new Thread(new Runnable(){
                            Socket socket;
                            String request_type; 

                            //initialize thread
                            public Runnable init(Socket socket) {
                                this.socket = socket;
                                return this;
                            }

                            @Override 
                            public void run() {
                                ObjectInputStream input; ObjectOutputStream output;
                                try{
                                    //initialize input and output streams to accept messages from peer and reply
                                    output = new ObjectOutputStream(socket.getOutputStream());
                                    input = new ObjectInputStream(socket.getInputStream());
                                    //get message type (CHECKACTIVE, .. )
                                    String type = (String)input.readObject();
                                    System.out.println("Received a new request from a peer or Tracker : "+type);

                                    //CHECKACTIVE
                                    if(type.equals("CHECKACTIVE")) {

                                    }
                                    
                                }catch(IOException | ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.init(socket)).start();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }}).start();
        
        //SEND REQUESTS
        new Thread(new Runnable() {
            @Override
            public void run() {
                list();
                while(true) {
                    System.out.println("Choose: \n1) Select file\n0) Logout");
                    Scanner in = new Scanner(System.in);
                    String user_input; int option = -1;

                    while(option!= 1 && option!=0) {
                        user_input = in.nextLine();
                        try {
                            option = Integer.parseInt(user_input);
                        }catch(NumberFormatException e) { continue; }
                    }

                    if(option == 0) {
                        //logout
                        break;
                    } 
                    else {
                        for(int i=1; i <= all_files.size(); i++) {
                            System.out.println(i+") "+all_files.get(i-1));
                        }
                        System.out.println("Choose a file. Press any other key to exit. . . ");
                        user_input = in.nextLine();
                        while(true) {
                            try {
                                option = Integer.parseInt(user_input);
                            }catch(NumberFormatException e) { continue; }
                            break;
                        }
                        if(option <= 0 || option > all_files.size()) {
                            continue;
                        }else {
                            details(all_files.get(option-1));
                        }
                    }
                }   
        }}).start();
    }//start

    /** Register to the system */
    public void register() {
        Scanner in = new Scanner(System.in);
        String user_input; 
        Socket tracker; //socket to connect to tracker
        ObjectInputStream input; ObjectOutputStream output; //input and output streams
        String reply;
        try {
            tracker = new Socket("192.168.2.2",6000);
            //initialize input and output streams to accept messages from peer and reply
            output = new ObjectOutputStream(tracker.getOutputStream());
            input = new ObjectInputStream(tracker.getInputStream());
            output.writeObject("REGISTER"); output.flush(); //send type 

            System.out.println("Registering new peer\nInsert username: ");
            while(true) {
                user_input = in.nextLine();
                output.writeObject(user_input); output.flush();  //send username and receive reply
                reply = (String)input.readObject();
                if(reply.equals("DECLINED")) {
                    System.out.println("Username already exists...");
                }else if(reply.equals("ACCEPTED")) {
                    System.out.println("Username accepted");
                    break;
                }
            }
            System.out.println("Insert password: ");
            user_input = in.nextLine();
            output.writeObject(user_input); output.flush();
            //login
            login();
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }//register

    /** Login to the system */
    public void login() {
        Scanner in = new Scanner(System.in);
        String user_input; 
        Socket tracker; //socket to connect to tracker
        ObjectInputStream input; ObjectOutputStream output; //input and output streams
        String reply;
        try {
            tracker = new Socket("192.168.2.2",6000);
            //initialize input and output streams to accept messages from peer and reply
            output = new ObjectOutputStream(tracker.getOutputStream());
            input = new ObjectInputStream(tracker.getInputStream());
            output.writeObject("LOGIN"); output.flush(); //send type
            System.out.println("Login\nInsert username: ");
            while(true) {  
                user_input = in.nextLine();
                //send username and receive reply
                output.writeObject(user_input); output.flush();
                reply = (String)input.readObject();
                if(reply.equals("DECLINED")) {
                    System.out.println("Username doesn't exist. Try again.");
                }else if(reply.equals("ACCEPTED")) {
                    System.out.println("Username accepted");
                    break;
                }
            }
            System.out.println("Insert password: ");
            while(true) {
                user_input = in.nextLine();
                output.writeObject(user_input); output.flush();
                //read reply (if password is correct)
                reply = (String)input.readObject();
                if(reply.equals("DECLINED")) {
                    System.out.println("Wrong Password! Try again");
                }else{
                    System.out.println("Password correct!");
                    token_id = Integer.parseInt(reply);
                    System.out.println(token_id);
                    loggedin = true;
                    break;
                }
            }

            //INFORM 
            System.out.println("Informing tracker about ip,port and files on this peer");
            output.writeObject(ip); output.flush();
            output.writeObject(String.valueOf(port)); output.flush();
            output.writeObject(file_names); output.flush();
            reply = (String)input.readObject();
            if(reply.equals("OK")) System.out.println("Login completed");

        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }//login

    /** Request list of all files in p2p system **/
    public void list() {
        System.out.println("Getting all file names in p2p system. . .");
        Socket tracker; //socket to connect to tracker
        ObjectInputStream input; ObjectOutputStream output; //input and output streams
        try {
            tracker = new Socket("192.168.2.2",6000);
            //initialize input and output streams to accept messages from peer and reply
            output = new ObjectOutputStream(tracker.getOutputStream());
            input = new ObjectInputStream(tracker.getInputStream());
            output.writeObject("LIST"); output.flush(); //send type
            all_files = (ArrayList<String>)input.readObject();
            System.out.println("ok");
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }//list

    /** Request details of specific file */
    public void details(String filename) {
        System.out.println("Requesting details for file "+filename+". . .");
        Socket tracker; //socket to connect to tracker
        ObjectInputStream input; ObjectOutputStream output; //input and output streams
        String reply;
        try {
            tracker = new Socket("192.168.2.2",6000);
            //initialize input and output streams to accept messages from peer and reply
            output = new ObjectOutputStream(tracker.getOutputStream());
            input = new ObjectInputStream(tracker.getInputStream());

            output.writeObject("DETAILS"); output.flush(); //send type
            output.writeObject(filename); output.flush(); //send type
            reply = (String)input.readObject();
            if(reply.equals("OK")) System.out.println("Request sent succesfully");
            
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }
    }//details


    /** MAIN APP */
    public static void main(String[] args) {
        String path = "";
        int port = 0;
        try {
            path = args[0];
            port = Integer.parseInt(args[1]);
        }catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.out.println("Run: java Peer path_name port");
            return;
        }
        //start a new peer
        new Peer("192.168.2.2",port,path);
    }//main

}