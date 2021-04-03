import java.util.*;
import java.net.*;
import java.io.*;

public class Peer {

    String directory,ip;
    int port; 

    public Peer(String ip, int port) {
        this.ip = ip;
        this.port = port;
        connect();
    }

    public void connect() {
        System.out.println("Welcome to peer. Register or Login?\n1) Register\n2) Login\n0) Exit");
        Scanner in = new Scanner(System.in);
        String user_input; int option = -1;
        //socket to connect to tracker
        Socket tracker;
        //input and output streams
        ObjectInputStream input; ObjectOutputStream output;
        String reply;

        while(option!= 1 && option!=2 ) {
            user_input = in.nextLine();
            try {
                option = Integer.parseInt(user_input);
            }catch(NumberFormatException e) { continue; }
            if(option == 0) break;
        }

        //REGISTER
        if(option == 1) {
            System.out.println("Registering new peer\nInsert username: ");
            user_input = in.nextLine();
            try {
                tracker = new Socket("192.168.2.2",6000);
                //initialize input and output streams to accept messages from peer and reply
                output = new ObjectOutputStream(tracker.getOutputStream());
                input = new ObjectInputStream(tracker.getInputStream());
                //send type 
                output.writeObject("REGISTER"); output.flush();
                //send username and receive reply
                output.writeObject(user_input); output.flush();
                reply = (String)input.readObject();
                if(reply.equals("DECLINED")) {
                    System.out.println("Username already exists...");
                    //TODO request new username
                }else if(reply.equals("ACCEPTED")) {
                    System.out.println("Insert password: ");
                    user_input = in.nextLine();
                    output.writeObject(user_input); output.flush();
                }
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        //LOGIN
        else if(option ==2) {
            System.out.println("Login\nInsert username: ");
            user_input = in.nextLine();
            try {
                tracker = new Socket("192.168.2.2",6000);
                //initialize input and output streams to accept messages from peer and reply
                output = new ObjectOutputStream(tracker.getOutputStream());
                input = new ObjectInputStream(tracker.getInputStream());
                //send type 
                output.writeObject("LOGIN"); output.flush();
                //send username and receive reply
                output.writeObject(user_input); output.flush();
                reply = (String)input.readObject();
                if(reply.equals("DECLINED")) {
                    System.out.println("Username doesn't exist...");
                    //TODO send new username
                }else if(reply.equals("ACCEPTED")) {
                    System.out.println("Insert password: ");
                    user_input = in.nextLine();
                    output.writeObject(user_input); output.flush();
                    //read reply (if password is correct)
                    reply = (String)input.readObject();
                    if(reply.equals("ACCEPTED")) {
                        System.out.println("Password correct!");
                    }else{
                        System.out.println("Wrong Password! Try again");
                        //TODO send new password
                    }
                }
            }catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }

    }


    public static void main(String[] args) {
        //start a new peer
        new Peer("192.168.2.2",6001);
    }

}