import java.util.*;
import java.net.*;
import java.io.*;

public class Peer {
    String directory,ip;
    int port; 
    File[] files; ArrayList<String> file_names;

    public Peer(String ip, int port, String directory) {
        this.ip = ip;
        this.port = port;
        this.directory = directory;
        init();
        connect();
    }

    public void init() {
        file_names = new ArrayList<String>();
        File dir = new File(directory);
        File[] files = dir.listFiles();
        for (File f : files) {
            System.out.println(f.getName());
            file_names.add(f.getName());
        }                                                 
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
            } catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        //LOGIN
        else if(option ==2) {
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
                        int token = Integer.parseInt(reply);
                        System.out.println(token);
                        break;
                    }
                }

                //INFORM 
                System.out.println("Informing tracker about ip,port and files on this peer");
                output.writeObject(ip); output.flush();
                output.writeObject(String.valueOf(port)); output.flush();
                output.writeObject(file_names); output.flush();



            } catch(IOException | ClassNotFoundException e){
                e.printStackTrace();
            }
        }
    }



    public static void main(String[] args) {
        String path = "";
        try {
            path = args[0];
        }catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("Run: java Peer path_name");
            return;
        }
        //start a new peer
        new Peer("192.168.2.2",6001,path);
    }

}