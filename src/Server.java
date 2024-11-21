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

        String socketStr = null;
        try {
            socketStr = Files.readString(Paths.get("C:\\Users\\lenovo\\IdeaProjects\\utp_project2git\\src\\Server_Socket.txt")).trim();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int port = Integer.parseInt(socketStr);
        try{
            Elysium = new ServerSocket(port);
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
        private String statue;
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
            }
        }

        @Override
        public void run() {
                    //drink water
            try{
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                out.println("Enter a ncikname: ");
                System.out.println("Connection received, awaiting nickname...");
                nick = in.readLine();
                statue="";
                System.out.println(nick+ " is in!");

                announcement(statue+nick," is online!!!");
                out.println("Enter *help* inn order to get to the help desk");

                String message=null;
                while((message=in.readLine())!=null){
                    if(message.startsWith("*quit*")){
                        announcement(statue+nick," is offline!!!");
                        System.out.println(nick+" is offline!");
                        killSwitch();
                    } else if (message.startsWith("*openhelpdesk*")) {
                        out.println("COMMAND LIST");
                        out.println("*exit*        exit");
                        out.println("*st*          change your statue");
                        out.println("*help*        shows command list");
                        out.println("*bList*       shows the banned words list");
                        out.println("*rooms*       shows room list");
                        out.println("*dm*          direct messages");
                        out.println("*mdm*         direct messages to multiple user");
                        out.println("*but*         send message to all but xxxx");  //TODO
                        out.println("*all/room*    shows the list of people online in the room");
                        out.println("*all*         shows the list of all people online");
                    }else if(message.startsWith("*st*")) {
                        out.println("Statue List:");
                        out.println("[0] for happy \n [1] for netural " +
                                "\n [2] for bored  \n [3] for tired " +
                                "\n [4] for duck face \n [5] for confused" +
                                "\n [6] for cool");
                        String choosen=in.readLine();
                        switch(choosen){
                            case "0":switchStatue(0);break;
                            case "1":switchStatue(1);break;
                            case "2":switchStatue(2);break;
                            case "3":switchStatue(3);break;
                            case "4":switchStatue(4);break;
                            case "5":switchStatue(5);break;
                            case "6":switchStatue(6);break;
                            default:out.println("no such statue exist");break;
                        }
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
                                broadcastBut(statue+nick,multiMessage,"all",enemyList);
                            }
                            else{
                                broadcastBut(statue+nick,multiMessage,currentRoom,enemyList);
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
                                            c.sendMessage(statue+nick+"(dm): "+multiMessage);
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
                                else {c.sendMessage(statue+nick+"(dm): "+dm);}
                                bb=false;
                                out.println("message sented succsessfully");
                            }
                        }
                        if(bb){
                            out.println("No such user with that nick is online at the moment");
                        }
                    }else if (message.startsWith("*rooms*")) {
                        out.println("//           home");
                        out.println("/cmdy/     comedy room");
                        out.println("/r/     random stuff room");
                        out.println("/sci/    science room");
                        out.println("/p/   politics room");
                        out.println("/s/     social room");
                        out.println("/t/       tech room");
                        out.println("/c/     crypto room");
                    } else if (message.startsWith("/")) {
                        switchRoom(message);
                    }
                    else if (containsBannedWords(message)) {
                        out.println("Message contains banned words and will not be sent.");
                    }
                    else{
                    broadcast(statue+nick,message,currentRoom);}
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
                case "/cmdy/":
                    comedyOnline.add(this);
                    currentRoom="comedy";
                    break;
                case "/sci/":
                    scienceOnline.add(this);
                    currentRoom="science";
                    break;
                case "//":
                    peopleOnline.add(this);
                    currentRoom="home";
                    break;
                case "/r/":
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
                            Thread.sleep(1 * 1000);
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
                            Thread.sleep(2 * 1000);
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
                            Thread.sleep(2 * 1000);
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
                case "/p/":
                    politicsOnline.add(this);
                    currentRoom="politics";
                    break;
                case "/s/":
                    socialOnline.add(this);
                    currentRoom="social";
                    break;
                case "/t/":
                    techOnline.add(this);
                    currentRoom="tech";
                    break;
                case "/c/":
                    cryptoOnline.add(this);
                    currentRoom="crypto";
                    break;
            }

            out.println("You have entered the "+currentRoom+" room!");
            broadcast(nick," has entered the room",currentRoom);
            System.out.println(nick+" entered the "+currentRoom+" room");

        }

        public void switchStatue(int i){
            switch(i){
                case 0:statue="(◉‿◉)";break;
                case 1:statue="(ㆆ _ ㆆ)";break;
                case 2:statue="(-_-)";break;
                case 3:statue="(0__#)";break;
                case 4:statue="(・3・)";break;
                case 5:char c='\\';statue="¯"+c+"(°_o)/¯";break;
                case 6:statue="(⌐■_■)";break;
            }
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
                out.println(c.statue+" "+c.nick);
            }
        }
    }
    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                Scanner scnn=new Scanner(System.in);
                System.out.println("Awaiting input...");
                while(b){
                    String message=scnn.nextLine();
                    announcement("!SERVER ANNOUNCEMENT!: ",message);
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
