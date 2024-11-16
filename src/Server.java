import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{

    private ServerSocket Elysium;
    private ArrayList<ConnectionHandler> peopleOnline;
    private ExecutorService threadPool;
    //https://www.geeksforgeeks.org/java-util-concurrent-executorservice-interface-with-examples/
    private boolean b;


    public Server(){
        peopleOnline = new ArrayList<>();
        b = true;
    }

    @Override
    public void run() {

        try{
            Elysium = new ServerSocket(29991);
            threadPool= Executors.newCachedThreadPool();
            while(b){
                Socket client=Elysium.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                //peopleOnline.add(new ConnectionHandler(client));
                peopleOnline.add(handler);
                threadPool.execute(handler);
            }
        }
        catch(IOException e){killSwitch();}

    }

    public void broadcast(String message){
        for(ConnectionHandler c : peopleOnline){
            c.sendMessage(message);
        }
    }

    public void killSwitch(){
        try{
            b=false;
            threadPool.shutdown();
            if(!Elysium.isClosed()){Elysium.close();}
            for(ConnectionHandler c : peopleOnline){
                c.killSwitch();
            }
        }
        catch (IOException e){e.printStackTrace();} //maybe add a proper handle if u can??
    }

    class ConnectionHandler implements Runnable{

        private Socket client;
        private String nick;
        private BufferedReader in;
        //https://www.geeksforgeeks.org/java-io-bufferedreader-class-java/
        private PrintWriter out;
        //https://www.geeksforgeeks.org/java-io-printwriter-class-java-set-1/

        public ConnectionHandler(Socket client){
            this.client = client;
        }

        @Override
        public void run() {
                    //drink water
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //out.println("stuff");  //how to speak to a client
                //in.readLine();         //how to get message from the client
                out.println("Enter a ncikname: ");
                System.out.println("Connection received, awaiting nickname...");
                nick = in.readLine();//play around with this part(put rules to creating nicks)
                //maybe create a file with user data like nick and password
                System.out.println(nick+ " is in!"); //this one is for server
                //maybe add a file for records??
                broadcast(nick+" is online!!!");
                String message=null;
                while((message=in.readLine())!=null){
                    if(message.startsWith("*quit*")){
                        broadcast(nick+" is offline!!!");
                        System.out.println(nick+" is offline!");
                        killSwitch();
                    }
                    else{//check here if the message includes banned words
                    broadcast(nick+": "+message);}
                    //add stuff here so that message can have other functions
                    // like talking to the server to make adjustments
                    // like changing the room
                }
            } catch (IOException e) {
                killSwitch();
            }

        }

        public void sendMessage(String message){
            out.println(message);
        }

        public void killSwitch(){
            try {
                in.close();
                out.close();
                client.close();
            } catch (IOException e) {}
        }
    }

    public static void main(String[] args) {
        Server Elysium = new Server();
        Elysium.run();
    }
}
