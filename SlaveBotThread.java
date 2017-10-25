/**
 * Created by yluo0203 on 4/7/17.
 */

import java.io.*;
import java.net.*;

public class SlaveBotThread extends Thread
{
    private Socket socket = null;
    private DataInputStream input = null;
    private SlaveBot slavebot = null;

    public SlaveBotThread (SlaveBot slavebot, Socket socket)
    {
        this.slavebot = slavebot;
        this.socket = socket;
        open();
        start();

    }
    public void open()
    {
        try
        {
            input  = new DataInputStream(socket.getInputStream());
        }
        catch(IOException ioe)
        {
            System.out.println("Error getting input stream: " + ioe);
//            slavebot.stop();
        }
    }
    public void close()
    {
        try
        {
            if (input != null) input.close();
        }
        catch(IOException ioe){
            System.out.println("Error closing input stream: " + ioe);}
    }
    public void run()
    {
        while (true)
        {
            try
            {
                slavebot.handle(input.readUTF());
            }
            catch(IOException ioe)
            {
                System.out.println("Listening error: " + ioe.getMessage());
                System.out.println("Server disconnect unexpectly.");
                slavebot.stop();
            }
        }
    }
}

