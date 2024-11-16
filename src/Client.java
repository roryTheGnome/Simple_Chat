import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Client implements Runnable {

    private Socket user;
    private String nick;
    private BufferedReader in;
    private PrintWriter out;
    private boolean b;

    @Override
    public void run() {
        try{
            user=new Socket("127.0.0.1",29991);//host is local
            out = new PrintWriter(user.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(user.getInputStream()));
            b=true;

            //TODO added later
            System.out.println(in.readLine());
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            nick = userInput.readLine();
            out.println(nick);
            //TODO added later



            InputHandler input=new InputHandler();
            Thread thread=new Thread(input);
            thread.start();

            String m=null;
            //String m = in.ReadLine();
            while((m=in.readLine())!=null){
                System.out.println(m);
                //m=in.readLine();
            }

        }catch(IOException e){
            killSwitch();
        }
    }

    public void killSwitch(){
        b=false;
        try{
            in.close();
            out.close();
            user.close();
        }catch (IOException e){}
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try{
                BufferedReader in2=new BufferedReader(new InputStreamReader(System.in));
                while(b){
                    String message=in2.readLine();
                    if(message.equals("*exit*")){
                        out.println("*quit*");
                        in2.close();
                        killSwitch();
                        //break;//TODO added later
                    }
                    else{
                        out.println(message);
                    }
                }
            } catch (IOException e) {
                killSwitch();
            }
        }
    }
    public static void main(String[] args) {
        Client user=new Client();
        user.run();
    }
}
