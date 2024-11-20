import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.FileWriter;

/*current banned words:
banned
word
say
hi */

public class Server implements Runnable{

    private ServerSocket Elysium;
    private ArrayList<ConnectionHandler> everyoneOnline;
    private ArrayList<ConnectionHandler> peopleOnline;
    private ArrayList<ConnectionHandler> comedyOnline;
    private ArrayList<ConnectionHandler> scienceOnline;
    private ArrayList<ConnectionHandler> politicsOnline;
    private ArrayList<ConnectionHandler> randomOnline;
    private ArrayList<ConnectionHandler> socialOnline;
    private ArrayList<ConnectionHandler> techOnline;
    private ArrayList<ConnectionHandler> cryptoOnline;
    private ExecutorService threadPool;
    //https://www.geeksforgeeks.org/java-util-concurrent-executorservice-interface-with-examples/
    private boolean b;
    Scanner scnn;

    public Server(){
        everyoneOnline = new ArrayList<>();
        peopleOnline = new ArrayList<>();
        comedyOnline = new ArrayList<>();
        scienceOnline = new ArrayList<>();
        politicsOnline = new ArrayList<>();
        randomOnline = new ArrayList<>();
        socialOnline = new ArrayList<>();
        techOnline = new ArrayList<>();
        cryptoOnline = new ArrayList<>();
        b = true;
        scnn=new Scanner(System.in);
    }

    @Override
    public void run() {

        try{
            Elysium = new ServerSocket(29991);
            threadPool= Executors.newCachedThreadPool();

            InputHandler serverTalks=new InputHandler();
            Thread serverTalksThread= new Thread(serverTalks);
            serverTalksThread.start();

            while(b){
                Socket client=Elysium.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                //peopleOnline.add(new ConnectionHandler(client));
                everyoneOnline.add(handler);
                peopleOnline.add(handler);
                threadPool.execute(handler);
            }
        }
        catch(IOException e){killSwitch();}

    }

    public void broadcast(String nick, String message,String room){
        ArrayList<ConnectionHandler> toSend = new ArrayList<>();

        switch (room){
            case "politics": toSend = politicsOnline;break;
            case "science": toSend = scienceOnline;break;
            case "tech": toSend = techOnline;break;
            case "crypto": toSend = cryptoOnline;break;
            case "social": toSend = socialOnline;break;
            case "random": toSend = randomOnline;break;
            case "comedy": toSend = comedyOnline;break;
            case "home": toSend = peopleOnline;break;
        }

        if(room.equals("home")){
            for(ConnectionHandler c : toSend){
                c.sendMessage(nick+": "+message);
            }
        } else{
            for(ConnectionHandler c : toSend){
                c.sendMessage(nick+"("+room+"): "+message);
            }
        }
    }

    public void broadcastBut(String nick, String message,String room,ArrayList<String> enemyList){
        ArrayList<ConnectionHandler> toSend = new ArrayList<>();

        switch (room){
            case "politics": toSend = politicsOnline;break;
            case "science": toSend = scienceOnline;break;
            case "tech": toSend = techOnline;break;
            case "crypto": toSend = cryptoOnline;break;
            case "social": toSend = socialOnline;break;
            case "random": toSend = randomOnline;break;
            case "comedy": toSend = comedyOnline;break;
            case "home": toSend = peopleOnline;break;
            case "all": toSend=everyoneOnline;break;
        }

        for (ConnectionHandler c : toSend) {
            // Check if the current user's nickname is NOT in the enemyList
            if (!enemyList.contains(c.nick)) {
                if (room.equals("home")) {
                    c.sendMessage(nick + "(secret): " + message);
                } else {
                    c.sendMessage(nick + "(" + room + ", secret): " + message);
                }
            }
        }

        /*if(room.equals("home")){
            for(ConnectionHandler c : toSend){
                int i=0;
                for(String s:enemyList){
                    if(c.nick.equals(s)){i++;}
                }
                if(i==0){
                    c.sendMessage(nick+"(secrect): "+message);
                }
            }
        }else{
            for(ConnectionHandler c : toSend){
                c.sendMessage(nick+"("+room+",secret): "+message);
            }
        }*/
    }

    public void announcement(String nick, String message){
        for(ConnectionHandler c : everyoneOnline){
            c.sendMessage(nick+message);
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
        private List<String> bannedWords;
        private String currentRoom;


        public ConnectionHandler(Socket client){
            this.client = client;
            this.currentRoom = "home";
            try {
                bannedWords=Files.readAllLines(Paths.get("C:\\Users\\lenovo\\IdeaProjects\\utp_project2git\\src\\banned_words_list.txt"));
                //bannedWords = Files.readAllLines(Paths.get("C:\\Users\\lenovo\\OneDrive\\Desktop\\banned_words_list.txt"));
            } catch (IOException e) {
                System.out.println("Error loading banned words: " + e.getMessage());
                bannedWords = List.of();
            }
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

                announcement(nick," is online!!!");
                out.println("Enter *help* inn order to get to the help desk");

                String message=null;
                while((message=in.readLine())!=null){
                    if(message.startsWith("*quit*")){
                        announcement(nick," is offline!!!");
                        System.out.println(nick+" is offline!");
                        killSwitch();
                    } else if (message.startsWith("*openhelpdesk*")) {
                        out.println("COMMAND LIST");
                        out.println("*exit*        exit");
                        out.println("*help*        shows command list");
                        out.println("*bList*       shows the banned words list");
                        out.println("*rooms*       shows room list");
                        out.println("*dm*          direct messages");
                        out.println("*mdm*         direct messages to multiple user");
                        out.println("*but*         send message to all but xxxx");  //TODO
                        out.println("*all/room*    shows the list of people online in the room");
                        out.println("*all*         shows the list of all people online");
                    }else if (message.startsWith("*all*")){
                        showEmAll(everyoneOnline,"Everyone Online: ");
                    }else if(message.startsWith("*all/room*")){
                        switch (currentRoom){
                            case "politics": showEmAll(politicsOnline,"Everyone in "+currentRoom+":");break;
                            case "science": showEmAll(scienceOnline,"Everyone in "+currentRoom+":");break;
                            case "tech": showEmAll(techOnline,"Everyone in "+currentRoom+":");break;
                            case "crypto": showEmAll(cryptoOnline,"Everyone in "+currentRoom+":");break;
                            case "social": showEmAll(socialOnline,"Everyone in "+currentRoom+":");break;
                            case "random": showEmAll(randomOnline,"Everyone in "+currentRoom+":");break;
                            case "comedy": showEmAll(comedyOnline,"Everyone in "+currentRoom+":");break;
                            case "home": showEmAll(peopleOnline,"Everyone in "+currentRoom+":");break;
                        }
                    }else if(message.startsWith("*bList*")){
                        out.println("List of banned words are gonna be visible! Are you sure?");
                        out.println("[0] for no , [1] for yes");
                        String answer=in.readLine();
                        if(answer.equals("1")){
                            out.println("Banned Words:");
                            for(String s: bannedWords){
                                out.println(s);
                            }
                        } else if (answer.equals("0")) {
                            out.println("Action canceled.");
                        }
                        else{
                            out.println("Undefined answer.");
                        }

                    }else if(message.startsWith("*but*")){
                        out.println("Enter your message: ");
                        String multiMessage = in.readLine();
                        if (containsBannedWords(multiMessage)) {
                            out.println("Message contains banned words and will not be sent.");
                        }else{
                            ArrayList<String> enemyList=new ArrayList<>();
                            boolean bb=true;
                            while(bb){
                                out.println("Enter the nickname of the reciver: \n Enter [0] to stop adding recivers");
                                String enemy = in.readLine();
                                if(enemy.equals("0")){
                                    bb=false;
                                } else{
                                    enemyList.add(enemy);
                                }
                            }
                            out.println("Enter [1] to set the reciver group as everybody online");
                            String reciverG=in.readLine();
                            if (reciverG.equals("1")){
                                broadcastBut(nick,multiMessage,"all",enemyList);
                            }
                            else{
                                broadcastBut(nick,multiMessage,currentRoom,enemyList);
                            }
                        }
                    }else if(message.startsWith("*mdm*")){
                        out.println("Enter your message: ");
                        String multiMessage = in.readLine();
                        if (containsBannedWords(multiMessage)) {
                            out.println("Message contains banned words and will not be sent.");
                        }else{
                            boolean bb=true;
                            boolean bbb=true;
                            while(bb){
                                out.println("Enter the nickname of the reciver: \n Enter [0] to stop adding recivers");
                                String reciver = in.readLine();
                                if(reciver.equals("0")){
                                    bb=false;
                                } else{
                                    for(ConnectionHandler c:peopleOnline){
                                        if(c.nick.equals(reciver)){
                                            c.sendMessage(nick+"(dm): "+multiMessage);
                                            bbb=false;
                                        }
                                    }
                                    if(bbb){
                                        out.println("No such user is online at the moment");
                                    }
                                }

                            }
                        }
                    }else if (message.startsWith("*dm*")) {
                        out.println("Enter the nickname of the reciver: ");
                        String reciver = in.readLine();
                        boolean bb=true;
                        for(ConnectionHandler c : peopleOnline){
                            if(c.nick.equals(reciver)){
                                out.println("Enter your message: ");
                                String dm=in.readLine();
                                if (containsBannedWords(message)) {
                                    out.println("Message contains banned words and will not be sent.");
                                }
                                else {c.sendMessage(nick+"(dm): "+dm);}
                                bb=false;
                                out.println("message sented succsessfully");
                            }
                        }
                        if(bb){
                            out.println("No such user with that nick is online at the moment");
                        }
                    }else if (message.startsWith("*rooms*")) {
                        out.println("//           home");
                        out.println("/comedy/     comedy room");
                        out.println("/random/     random stuff room");
                        out.println("/science/    science room");
                        out.println("/politics/   politics room");
                        out.println("/social/     social room");
                        out.println("/tech/       tech room");
                        out.println("/crypto/     crypto room");
                    } else if (message.startsWith("/")) {
                        switchRoom(message);
                    }
                    else if (containsBannedWords(message)) {
                        out.println("Message contains banned words and will not be sent.");
                    }
                    else{
                    broadcast(nick,message,currentRoom);}
                }
            } catch (IOException e) {
                killSwitch();
            }

        }

        private void switchRoom(String message){
            switch(currentRoom){
                case "comedy": comedyOnline.remove(this); break;
                case "science": scienceOnline.remove(this); break;
                case "politics": politicsOnline.remove(this); break;
                case "random": randomOnline.remove(this); break;
                case "social": socialOnline.remove(this); break;
                case "tech": techOnline.remove(this); break;
                case "crypto": cryptoOnline.remove(this); break;
                default: peopleOnline.remove(this); break;
            }
            out.println("You have left "+currentRoom+" room!");
            System.out.println(nick+" left "+currentRoom+" room");
            broadcast(nick," has left the room!",currentRoom);

            switch(message){
                case "/comedy/":
                    comedyOnline.add(this);
                    currentRoom="comedy";
                    break;
                case "/science/":
                    scienceOnline.add(this);
                    currentRoom="science";
                    break;
                case "//":
                    peopleOnline.add(this);
                    currentRoom="home";
                    break;
                case "/random/":
                    randomOnline.add(this);
                    currentRoom="random";
                    for(ConnectionHandler c : randomOnline){
                        c.sendMessage("Never gonna give you up\n" +
                                "Never gonna let you down\n" +
                                "Never gonna run around and desert you\n" +
                                "Never gonna make you cry\n" +
                                "Never gonna say goodbye\n" +
                                "Never gonna tell a lie and hurt you");
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        c.sendMessage("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣀⣀⣤⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢶⣾⣷⣾⣿⣿⣿⣿⣿⣿⣷⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⠿⠿⢿⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⠀⠁⠁⠈⠁⠀⠀⠀⠀⠀⢹⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⠀⢀⣀⣀⠀⠀⠀⠀⠀⠀⢸⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⢹⣿⣿⡆⠀⠈⠥⠀⠀⠀⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠂⢽⠗⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣶⣶⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⢿⡌⠀⠀⠰⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⡆⠀⣿⣾⣶⣆⠀⠀⢨⡄⠀⢀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣤⣶⣾⣿⣿⣿⡈⠛⢿⣿⣿⡄⠀⢸⢊⣀⠈⣿⣿⣶⣶⣤⣄⣀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⢀⣠⣴⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠈⠀⠉⠁⠀⠀⠄⠀⠀⣿⣿⣿⣿⣿⣿⣿⣷⣦⣄⠀\n" +
                                "⠀⠀⠀⠀⠀⣴⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇\n" +
                                "⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣤⡄⠲⠤⢤⣤⡄⠀⠀⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣧\n" +
                                "⠀⠀⠀⠀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣏⣉⣀⣐⠒⠒⠠⠰⢾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n" +
                                "⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠤⠤⠉⣉⣉⢸⣓⡲⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n" +
                                "⠀⠀⠀⢠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠒⠒⠒⠠⠤⢼⣭⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿\n" +
                                "⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣏⣉⣙⠛⠒⢸⠶⣦⣬⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿\n" +
                                "⠀⠀⠀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡧⠤⠬⢍⣉⣹⣛⣓⣲⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇\n" +
                                "⠀⠀⢠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡒⠲⠶⠶⡿⣽⣿⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇\n" +
                                "⠀⠀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣭⣍⣙⣛⣏⣷⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠏\n" +
                                "⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠦⠤⣬⣭⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣇⠀\n" +
                                "⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣙⣟⣿⣷⣷⣶⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡆\n" +
                                "⠀⣠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⠙⠃\n" +
                                "⠸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣿⣿⣿⣿⣿⣿⣿⣿⣿⠂⠀⠀⠀\n" +
                                "⠀⠘⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡟⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀\n" +
                                "⠀⠀⠈⢿⣿⣿⣿⣿⣿⣿⣿⣿⠿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠛⠛⠛⠁⡇⠟⢿⣿⣿⣿⣿⣿⣿⣿⣧⡀⠀⠀");
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        c.sendMessage("⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⠀⣘⣩⣅⣤⣤⣄⣠⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠄⢈⣻⣿⣿⢷⣾⣭⣯⣯⡳⣤⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣧⠻⠿⡻⢿⠿⡾⣽⣿⣳⣧⡷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢰⡶⢈⠐⡀⠀⠀⠁⠀⠀⠀⠈⢿⡽⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢫⢅⢠⣥⣐⡀⠀⠀⠀⠀⠀⠀⢸⢳⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠠⠆⠡⠱⠒⠖⣙⠂⠈⠵⣖⡂⠄⢸⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⠆⠀⠰⡈⢆⣑⠂⠀⠀⠀⠀⠀⠏⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢗⠀⠱⡈⢆⠙⠉⠃⠀⠀⠀⠀⠃⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠦⡡⢘⠩⠯⠒⠀⠀⠀⢀⠐⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡄⢔⡢⢡⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⢆⠸⡁⠋⠃⠁⠀⢀⢠⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⡰⠌⣒⠡⠄⠀⢀⠔⠁⣸⣿⣷⣤⣀⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⣐⣤⡄⠀⠀⠘⢚⣒⢂⠇⣜⠒⠉⠀⢀⣿⣿⣿⣿⣿⣿⣿⣷⣶⣶⣦⣔⣀⢄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⡀⢀⢠⣤⣶⣿⣿⣿⡆⠀⠀⠐⡂⠌⠐⠝⠀⠀⠀⢀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣤⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⢨⣶⣿⣿⣿⣿⣿⣿⣿⣿⣤⡶⢐⡑⣊⠀⡴⢤⣀⣀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⠀⠷⡈⠀⠶⢶⣰⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⢾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣉⠑⠚⣙⡒⠒⠲⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡁⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡷⠶⠀⠀⠤⣬⣍⣹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣄⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣛⣙⠀⢠⠲⠖⠶⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣭⣰⢘⣙⣛⣲⣿⣿⣿⣿⡿⡻⠿⠿⠿⠿⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣦⡀⠀⠀⠀⠀\n" +
                                "⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠶⢾⡠⢤⣭⣽⣿⣿⣿⣿⡟⣱⠦⠄⠤⠐⡄⠹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣶⣤⡀⠀\n" +
                                "⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡛⣻⡕⠶⠶⣿⣿⣿⣿⣿⣿⣗⣎⠒⣀⠃⡐⢀⠙⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠀\n" +
                                "⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣭⣹⣏⣛⣛⣿⣿⣿⣿⣿⣿⣿⣞⣍⣉⢉⠰⠀⠠⢹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠅\n" +
                                "⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠶⢼⡧⢤⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣣⣡⣛⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣅\n" +
                                "⡿⣷⣽⡿⠛⠋⠉⣉⡐⠶⣾⣾⣟⣻⡕⠶⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣹⣫⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠗\n" +
                                "⢸⣿⣟⣥⡶⢘⡻⢶⡹⣛⣼⣿⣯⣽⢯⣙⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠿⣿⣿⣿⣿⣿⣿⡿⠿⠟⠁⠀\n" +
                                "⠘⢟⣾⣿⣿⣚⠷⣳⢳⣫⣽⣿⣛⣾⡷⢾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⠀⠀⠁⠀⠈⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠙⢋⣿⣿⣯⣙⣯⣵⣿⣿⣯⣽⣟⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠉⠛⢻⠟⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣟⡟⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⣡⣿⣿⣿⣿⡗⣮⢻⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀");
                        try {
                            Thread.sleep(3 * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }

                        c.sendMessage("⠉⠁⠈⠀⠀⠀⠀⠀⠀⠀⠀⠈⠀⠉⠉⠉⢉⡩⡍⣩⢿⣿⣿⣿⣿⣿⡿⣿⣿⡿⣇⡄⠀⠀⠈⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠾⢳⣯⣾⣿⠿⣿⣿⣿⣿⣿⣿⣿⣿⡬⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠁⣲⣾⡛⢥⡒⢄⠀⠀⠀⠀⠈⠉⢿⣿⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣸⣿⣿⣐⣣⣜⣢⡁⢈⡴⠄⠀⠀⢈⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡸⢹⣿⣡⢎⣽⣭⣿⣻⣇⢿⣶⣦⣄⢨⣿⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡔⣷⣿⠀⣨⡟⣄⢢⡿⢭⡇⠈⠛⠛⠎⣹⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣻⣼⣇⠻⣸⢸⢻⣼⣿⣤⡤⡄⠀⢀⡻⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⢻⡔⢢⣙⣮⣽⣤⣆⡀⠃⠀⣤⡿⠅⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⢻⡰⢌⡹⢿⣍⠿⠛⢠⡼⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⢸⡿⡇⢮⢼⣁⠉⠀⢠⡌⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡘⣸⣸⣑⠎⡶⡭⠗⢋⠁⠴⣻⣦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⡠⡴⡈⣼⣭⣭⡯⣷⡰⡈⢆⣩⣞⠐⣿⣿⣿⣖⣢⣤⠄⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⢀⣲⣽⣶⣇⠥⡉⣿⣏⢿⡇⣯⢗⣩⣶⡟⠊⢘⣿⣿⣿⣿⣿⣾⣿⣶⣭⣿⣽⣒⣤⣀⡀⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⢀⠄⣀⣪⣴⣿⣿⣿⣿⣿⡇⣄⣿⣿⣴⣇⣟⣾⠟⠋⠀⢀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⣾⣷⣿⣿⣿⣿⣿⣿⣿⣿⡐⠸⣿⣿⠿⢷⣋⠀⠀⠀⢠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣇⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣭⠛⢛⣟⣛⣛⠒⠲⠶⣿⣿⣿⣿⣿⣿⣿⣿⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠀⢨⡯⣭⣭⣽⣟⣻⣿⣿⣿⣿⣿⣿⣿⣿⡷⣌⣿⣿⣿⣿⣿⣿⣿⠏⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠈⠰⡷⠶⠶⢦⣬⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⢿⣿⠟⠃⣹⣿⣿⡀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠈⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⣶⡝⠛⠛⠛⠛⢳⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⡟⣾⣧⠚⢳⣿⣿⡟⠀⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣻⣟⣛⣛⣛⠶⠶⠾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⡷⢾⣦⡜⣿⣿⣷⡄⠀⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣽⣯⣭⣍⣉⣛⣛⣛⣿⣿⣿⣿⣿⣿⣿⣿⣿⢷⣿⢴⣨⢹⣿⣿⣿⣿⣦⠀⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⢿⡷⢶⣾⣯⣭⣭⣭⣿⣿⣿⣿⣿⣿⣿⣿⣍⡎⢃⠘⣉⢸⣿⣿⣿⣿⣿⣆⠀⠀\n" +
                                "⠀⠀⠀⠀⠀⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣻⣿⣶⠶⠶⠶⢾⣯⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣾⣿⣦⣿⣿⣿⣿⣿⣿⣿⡆⠀\n" +
                                "⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⢿⣏⣛⣛⣛⣟⣻⠶⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀\n" +
                                "⠀⠀⠀⠀⠀⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⢼⡿⡿⣿⣭⣯⣽⣟⣛⣟⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⣿⣿⣿⣿⠆");
                        c.sendMessage("YOU'VE BEEN RICK ROLLED");

                    }
                    break;
                case "/politics/":
                    politicsOnline.add(this);
                    currentRoom="politics";
                    break;
                case "/social/":
                    socialOnline.add(this);
                    currentRoom="social";
                    break;
                case "/tech/":
                    techOnline.add(this);
                    currentRoom="tech";
                    break;
                case "/crypto/":
                    cryptoOnline.add(this);
                    currentRoom="crypto";
                    break;
            }

            out.println("You have entered the "+currentRoom+" room!");
            broadcast(nick," has entered the room",currentRoom);
            System.out.println(nick+" entered the "+currentRoom+" room");

        }

        private boolean containsBannedWords(String message) {
            String regex = "[,\\.\\s]";
            //https://www.w3schools.com/java/ref_string_split.asp
            String[] words = message.split(regex);
            for (String word : words) {
                for (String bannedWord : bannedWords) {
                    if (word.equalsIgnoreCase(bannedWord)) {
                        String breachMessage="user "+nick+" used one of the banned words: "+word;

                        try (FileWriter fileWriter = new FileWriter
                                ("C:\\Users\\lenovo\\IdeaProjects\\utp_project2git\\src\\Rule_Breach_History.txt", true)) {
                            fileWriter.write(breachMessage+"\n");
                            System.out.println(breachMessage);
                        } catch (IOException e) {
                            System.out.println("Something went wrong");
                            e.printStackTrace();
                        }

                        return true;
                    }
                }
            }
            return false;
        }

        public void sendMessage(String message){out.println(message);}

        public void killSwitch(){
            try {
                in.close();
                out.close();
                everyoneOnline.remove(this);
                client.close();
            } catch (IOException e) {}
        }

        public void showEmAll(ArrayList<ConnectionHandler> peopleList,String title){
            out.println(title);
            for(ConnectionHandler c : peopleList){
                out.println(c.nick);
            }
        }
    }
    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                Scanner scnn=new Scanner(System.in);
                while(b){
                    System.out.println("Awaiting input...");
                    String message=scnn.nextLine();
                    if(message.equals("*kill*")) {
                        scnn.close();
                        killSwitch();
                        //break;//TODO added later
                    }else{
                        announcement("!SERVER ANNOUNCEMENT!: ",message);
                    }
                }
            } catch (Exception e) {
                killSwitch();
            }
        }
    }

    public static void main(String[] args) {
        Server Elysium = new Server();
        Elysium.run();
    }
}

//TODO show banned words in helpdesk            DONE
//TODO group chat                               DONE
//TODO make announcements                       DONE
//TODO make announcemnets from server           DONE
//TODO send all but *****                       DONE

