import java.net.*;
import java.io.*;
import java.util.*;

public class Tracker {

    ServerSocket socket;
    String ip;
    int port;
    HashMap<String,PeerInfo> peers;

    //constructor
    public Tracker(String ip, int port) {
        this.ip = ip;
        this.port = port;
        peers = new HashMap<String,PeerInfo>();
        accept_requests();
        
    }

    //ServerSocket accepting requests from Peers
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
                                username = (String)input.readObject();
                                if(peers.containsKey(username)) {
                                    System.out.println("Username Declined");
                                    //peer with sent username already exists
                                    output.writeObject("DECLINED"); output.flush();
                                }else {
                                    System.out.println("Username Accepted");
                                    output.writeObject("ACCEPTED"); output.flush();
                                    //read the password
                                    password = (String)input.readObject();
                                    //register new peer
                                    peers.put(username, new PeerInfo(username,password,0,0));
                                    System.out.println("Successfully registered peer with username "+username+" and password "+password);
                                }
                            }
                            //LOGIN
                            else if(type.equals("LOGIN")) {
                                username = (String)input.readObject();
                                if(peers.containsKey(username)) {
                                    System.out.println("Request Password");
                                    output.writeObject("ACCEPTED"); output.flush();
                                    password = (String)input.readObject();
                                    if(password.equals(peers.get(username).getPassword())) {
                                        System.out.println("Password accepted! Login Successful");
                                        output.writeObject("ACCEPTED"); output.flush();
                                    }else{
                                        System.out.println("Wrong Password");
                                        output.writeObject("DECLINED"); output.flush();
                                        //TODO receive new password
                                    }
                                }else {
                                    System.out.println("Username doesn't exist");
                                    output.writeObject("DECLINED"); output.flush();
                                    //TODO receive new username
                                }

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


    public void register() {

    }

    public void login() {

    }

    public void logout() {

    }

    public static void main(String[] args) {
        new Tracker("192.168.2.2",6000);
    }

}