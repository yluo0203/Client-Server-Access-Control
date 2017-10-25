/**
 * Created by yluo0203 on 4/28/2017.
 */


        import java.net.*;
        import java.io.*;

public class MasterBotThread extends Thread
{
    private MasterBot        server    = null;
    private Socket           socket    = null;
    private int              ID        = -1;
    private DataInputStream  streamIn  =  null;
    private DataOutputStream streamOut = null;

    public MasterBotThread(MasterBot server, Socket socket)
    {
        super();
        this.server = server;
        this.socket = socket;
        ID     = socket.getPort();
    }
    public void send(String msg)
    {
        try
        {
            streamOut.writeUTF(msg);
            streamOut.flush();
        }
        catch(IOException ioe)
        {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            ioe.printStackTrace();
            //stop();
        }
    }

    public void open() throws IOException
    {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }
    public void close() throws IOException
    {
        if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null) streamOut.close();
    }
    public void run()
    {
        //System.out.println("Server Thread " + ID + " is running.");
        while (true)
        {
            try
            {
                String res = streamIn.readUTF();

                server.handle(ID, res);

            }catch (EOFException eof){
//                System.out.println(ID + " close");
            }
            catch(IOException ioe)
            {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                ioe.printStackTrace();
                //stop();
            }
        }
    }

    public int getID()
    {
        return ID;
    }

    public Socket getSocket()
    {
        return socket;
    }


}

