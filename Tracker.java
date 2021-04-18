import java.net.*;
import java.io.*;
import java.util.*;

public class Tracker {

    ServerSocket socket;
    String ip;
    int port;
    HashMap<String,ArrayList<String>> registered_peers; //key: username, value: (user_name,	password, count_downloads, count_failures)
    HashMap<Integer,ArrayList<String>> loggedin_peers; //key: token_id, value: 	(ip_address, port,	user_name)
    ArrayList<String> file_names; //list of all file names
    HashMap<String,ArrayList<Integer>> files_peers; //key: file name, value: list of token_ids with this file

    //constructor
    public Tracker(String ip, int port) {
        this.ip = ip;
        this.port = port;
        registered_peers = new HashMap<String,ArrayList<String>>();
        loggedin_peers = new HashMap<Integer,ArrayList<String>>();
        file_names = new ArrayList<String>();
        files_peers = new HashMap<String,ArrayList<Integer>>();

        accept_requests();
    }

    //ServerSocket accepting login, logout and register requests from Peers
    public void accept_requests() {
        try {
            socket = new ServerSocket(port,100);
            System.out.println("Accepting requests from peers. . .");
            Socket peer; 
            while(true) {
                peer = socket.accept();
                System.out.println("New request!");

                //new thread for each request
                new Thread(new Runnable(){
                    Socket peer;
                    String request_type; 

                    //initialize thread
                    public Runnable init(Socket peer) {
                        this.peer = peer;
                        return this;
                    }

                    @Override 
                    public void run() {
                        ObjectInputStream input; ObjectOutputStream output;
                        try{
                            //initialize input and output streams to accept messages from peer and reply
                            output = new ObjectOutputStream(peer.getOutputStream());
                            input = new ObjectInputStream(peer.getInputStream());
                            //get message type (REGISTER, LOGIN, .. )
                            String type = (String)input.readObject(); String username,password;
                            System.out.println("Received a new request from a peer : "+type);

                            //REGISTER
                            if(type.equals("REGISTER")) {
                                while(true) {
                                    username = (String)input.readObject();
                                    if(registered_peers.containsKey(username)) {
                                        System.out.println("Username Already Exists. Request new.");
                                        output.writeObject("DECLINED"); output.flush();
                                    }else {
                                        System.out.println("Username Accepted");
                                        output.writeObject("ACCEPTED"); output.flush();
                                        break;
                                    }
                                }
                                password = (String)input.readObject(); //read the password
                                registered_peers.put(username, new ArrayList<String>(Arrays.asList(username,password,"0","0"))); //register new peer
                                System.out.println("Successfully registered peer with username "+username+" and password "+password);
                            }
                            //LOGIN
                            else if(type.equals("LOGIN")) {
                                int token = 0;
                                while(true) {
                                    username = (String)input.readObject();
                                    if(registered_peers.containsKey(username)) {
                                        System.out.println("Request Password");
                                        output.writeObject("ACCEPTED"); output.flush();
                                        break;
                                    }else {
                                        System.out.println("Username doesn't exist");
                                        output.writeObject("DECLINED"); output.flush();
                                    }
                                }
                                while(true) {
                                    password = (String)input.readObject();
                                    if(password.equals(registered_peers.get(username).get(1))) {
                                        System.out.println("Password accepted! Login Successful");
                                        token = new Random().nextInt();
                                        System.out.println(token);
                                        output.writeObject(String.valueOf(token)); output.flush();
                                        break;
                                    }else{
                                        System.out.println("Wrong Password");
                                        output.writeObject("DECLINED"); output.flush();
                                    }
                                }

                                System.out.println("Getting peer information");
                                String ip = (String)input.readObject();
                                String port = (String)input.readObject();
                                ArrayList<String> peer_files = (ArrayList<String>)input.readObject();
                                System.out.println("Peer ip: "+ip+", port = "+port);
                                for(String s: peer_files) System.out.println(s);
                                //add info to hashmaps
                                loggedin_peers.put(token,new ArrayList<String>(Arrays.asList(ip,port,username)));
                                for(String s: peer_files){
                                    if(!file_names.contains(s)) file_names.add(s);
                                    if(!files_peers.containsKey(s)) files_peers.put(s,new ArrayList<Integer>(Arrays.asList(token)));
                                    else files_peers.get(s).add(token);
                                }

                                //check
                                System.out.println(files_peers);
                                System.out.println(loggedin_peers);

                                //confirm
                                output.writeObject("OK"); output.flush();
                                
                            }
                            //REPLY LIST
                            else if(type.equals("LIST")) {
                                output.writeObject(file_names); output.flush();
                            }
                            else if(type.equals("DETAILS")) {
                                //reply_details()
                                String filename = (String)input.readObject();
                                output.writeObject("OK"); output.flush(); //confirm
                                System.out.println("Requested details for file "+filename);
                                //find peers with requested file
                                ArrayList<Integer> peers_tokens = files_peers.get(filename);
                                for(int k: peers_tokens) {
                                    //checkactive 
                                    System.out.println(k);
                                }
                                //output.writeObject(file_names); output.flush();
                            }

                        }catch(IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }.init(peer)).start();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        new Tracker("192.168.2.2",6000);
    }

}