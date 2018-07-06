/**
  This program is meant to be used to test the android app 'Rock Paper Scissors online game'
  that you have to develop for the course DT8025 Real-Time Embedded Systems.

  It implements the server to
    1. accept connection
    2. register players
    3. send the list of online (available) players
    4. associate two palyers together upon request/accept
    5. pass messages and status between two palyers playing
    6.

  To start the server run

  > java RPSServer
*/

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;

public class UnoServer {

	private static boolean accepting = true;
	private static Scanner stdIn =  new Scanner(new BufferedInputStream(System.in));
	private static ArrayList<Socket> theConnections = new ArrayList<Socket>();
	private static ArrayList<String> theClientsChoice = new ArrayList<String>();


	private static HashMap<String,Scanner> client_ins = new HashMap<String,Scanner>();
	private static HashMap<String,InputStream> client_inps = new HashMap<String,InputStream>();
	private static HashMap<String,PrintWriter> client_outs = new HashMap<String,PrintWriter>();

	public static void main(String[] args)throws java.io.FileNotFoundException, java.io.IOException  {
		int port = 4444;

		FileWriter writer = new FileWriter("rps.txt", false);
		writer.write("<Rock Paper Scissors Online Game>\n");
		writer.close();

		buildConnections(port);
	}

private static void buildConnections(int port){
	new Thread(new Runnable(){
		public void run(){
			stdIn.nextLine();
			accepting = false;
		}
	}).start();

	try {
		final ServerSocket serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(1000); // to be able to stop blocking now and then
		System.err.println("Started server on port " + port);
		while (accepting) {
			try{
				Socket clientSocket = serverSocket.accept();
				InputStream is = clientSocket.getInputStream();
				Scanner in = new Scanner(new BufferedInputStream(is));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

				String id_name = in.next();

				client_ins.put(id_name,in);
				client_inps.put(id_name,is);
				client_outs.put(id_name,out);

				System.err.println("Accepted connection from client " + id_name);

				out.println("<Accepted connection from " + id_name +"/>");
				out.flush();
			}catch(SocketTimeoutException e){}

			//Listen to connected clients
			if(client_ins.size()>0)
				ManageGame();
		}
	} catch (IOException ioe) { System.err.println("here" + ioe); }
}

	private static void ManageGame() throws java.io.IOException{


		ArrayList<String> theClients    = new  ArrayList<String>(client_ins.keySet());
		ArrayList<Scanner> ins          = new  ArrayList<Scanner>(client_ins.values());
		ArrayList<InputStream> inps     = new  ArrayList<InputStream>( client_inps.values());
		ArrayList<PrintWriter> outs     = new  ArrayList<PrintWriter>( client_outs.values());

		PrintWriter out;
		InputStream is;
		Scanner in;

		String s = "";
		int i1, i2;

		String clientName = "";

		for(int i = 0; i<ins.size();i++){
			in = ins.get(i);
			is = inps.get(i);
			out = outs.get(i);

			if(is.available()>0){
				s = in.nextLine();
				clientName = theClients.get(i);
				if(s.startsWith("<ReadyToPlay/>")) {
					System.out.println(clientName + " is now online.");
					ReadyToPlay(clientName, out);
					out.flush();
				} else if(s.startsWith("<OnlinePlayers/>")) {
					System.out.println(clientName + " asks for online players.");
					OnlinePlayers("<OnlinePlayer", out);
					out.flush();
				} else if (s.startsWith("<PlayRequest ")) {
					String with = s.replace("/>", "").substring(s.indexOf(' ')+1) ;
					System.out.println(clientName + " requests to play with " + with + ".");
					PlayRequest(clientName, with, s, out);
					out.flush();
				} else if (s.startsWith("<DiscardPlayRequest ")) {
					String with = s.replace("/>", "").substring(s.indexOf(' ')+1) ;
					System.out.println(clientName + " discards the request to play with " + with + ".");
					DiscardPlayRequest(clientName, with, s, out);
					out.flush();
				} else if(s.startsWith("<PlayRequestsForMe/>")) {
					System.out.println(clientName + " asks for play requests for him/her.");
					PlayRequestsFor(clientName, s, out);
					out.flush();
				} else if(s.startsWith("<AcceptPlayRequest ")) {
					String from = s.replace("/>", "").substring(s.indexOf(' ')+1) ;
					System.out.println(clientName + " accepted play request from " + from + ".");
					AcceptPlayRequest(clientName, from, s, out);
					out.flush();
				} else if(s.startsWith("<RejectPlayRequest ")) {
					String from = s.replace("/>", "").substring(s.indexOf(' ')+1) ;
					System.out.println(clientName + " rejected play request from " + from + ".");
					RejectPlayRequest(clientName, from, s, out);
					out.flush();
				} else if(s.startsWith("<MyPlayRequests/>")) {
					System.out.println(clientName + " asks for their play requests.");
					MyPlayRequests(clientName, s, out);
					out.flush();
				} else if(s.startsWith("<Choice ")) {
					String choice = s.replace("/>", "").substring(s.indexOf(' ')+1);
					System.out.println(clientName + " chose " + choice + ".");
					UpdateGameStatus(clientName, choice, s, out);
					out.flush();
				} else if(s.startsWith("<CheckGameStatus/>")) {
					System.out.println(clientName + " asks for game status.");
					CheckGameStatus(clientName, s, out);
					out.flush();
				} else if(s.startsWith("<LeaveSession/>")) {
					System.out.println(clientName + " left the session.");
					LeaveSession(clientName, s, out);
					out.flush();
				} else if(s.startsWith("<Exit/>")) {
					System.out.println(clientName + " left the game.");
					LeaveGame(clientName, s, out);
					out.flush();
				}
			}
		}
	}

private static void ReadyToPlay(String s, PrintWriter out) throws IOException {
    try
    {
        out.println("<Welcome to the Rock Paper Scissors Online Game!/>");
        out.flush();

        FileWriter writer = new FileWriter("rps.txt",true);
        writer.write("<OnlinePlayer name=\"" + s + "\" status=\"idle\"/>\n");
        writer.close();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }

}

private static  int OnlinePlayers(String s, PrintWriter out)
         {
	     int iSend=0;
         try
             {
             File file = new File("rps.txt");
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String line = "", text = "";
             while((line = reader.readLine()) != null)
                 {

            	 if(line.startsWith(s)){
            		 iSend++;
            		 out.println(line);
                     out.flush();
            	 }
                 text += line + "\n";

             }
             reader.close();


             FileWriter writer = new FileWriter("rps.txt");
             writer.write(text);
             writer.close();
         }
         catch (IOException ioe)
             {
             ioe.printStackTrace();
         }
         return iSend;
     }

    private static void PlayRequest(String clientName, String with, String s, PrintWriter out) {
        try
        {
            File file = new File("rps.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", text = "";
            boolean validReq = false;

            while((line = reader.readLine()) != null)
            {

                if(line.startsWith("<OnlinePlayer name=\"" + clientName)){

                    line = line.replace("idle", "waiting");
                } else if(line.startsWith("<OnlinePlayer name=\"" + with)){
                    validReq = true;
                }

                text += line + "\n";
            }
            reader.close();

            if (validReq) {
                text += "<PlayRequest From=\"" + clientName + "\" With=\"" + with + "\" status=\"pending\"/>\n";
                out.println("<Play request was sent to " + with + "!/>");
                out.flush();

            } else {
                out.println("<Invalid Request: " + with + " is not available!/>");
                out.flush();
            }

            FileWriter writer = new FileWriter("rps.txt");
            writer.write(text);
            writer.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }
    private static void PlayRequestsFor(String clientName, String s, PrintWriter out)
    {
        try
        {
            File file = new File("rps.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", text = "";
            while((line = reader.readLine()) != null)
            {

                if(line.startsWith("<PlayRequest") & line.indexOf(" With=\""+ clientName) >= 0){
                    out.println(line);
                    out.flush();
                }
                text += line + "\n";

            }
            reader.close();


            FileWriter writer = new FileWriter("rps.txt");
            writer.write(text);
            writer.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return;
    }

    private static void MyPlayRequests(String clientName, String s, PrintWriter out)
    {
        try
        {
            File file = new File("rps.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", text = "";
            while((line = reader.readLine()) != null)
            {
                if(line.startsWith("<PlayRequest") & line.indexOf("From=\""+ clientName) >= 0){
                    out.println(line);
                    out.flush();
                }
                text += line + "\n";

            }
            reader.close();


            FileWriter writer = new FileWriter("rps.txt");
            writer.write(text);
            writer.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        return;
    }

    private static void AcceptPlayRequest(String clientName, String from, String s, PrintWriter out)
    {
        try
        {
            File file = new File("rps.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", text = "";
            while((line = reader.readLine()) != null)
            {

                    if(line.startsWith("<PlayRequest") & line.indexOf(" From=\""+ from) >= 0){
                        line = line.replace("pending", "accepted");
                        out.println(line);
                        out.flush();
                    }

                    if(line.startsWith("<OnlinePlayer name=\"" + clientName)){

                        line = line.replace("idle", "busy");
                    }

                    if(line.startsWith("<OnlinePlayer name=\"" + from)){

                        line = line.replace("waiting", "busy");
                    }


                text += line + "\n";

            }
            reader.close();

            text += "<GameSession> <Player1 name=\"" + from + "\" choice=\"\"/> <Player2 name=\"" + clientName + "\" choice=\"\"/> <GameSession/>\n";

            FileWriter writer = new FileWriter("rps.txt");
            writer.write(text);
            writer.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private static void RejectPlayRequest(String clientName, String from, String s, PrintWriter out)
    {
        try
        {
            File file = new File("rps.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", text = "";
            while((line = reader.readLine()) != null)
            {

                if(line.startsWith("<PlayRequest") & line.indexOf(" From=\""+ from) >= 0){
                    line = line.replace("pending", "rejected");
                    out.println(line);
                    out.flush();
                }

                if(line.startsWith("<OnlinePlayer name=\"" + from)){

                    line = line.replace("waiting", "idle");
                }

                text += line + "\n";

            }
            reader.close();

            FileWriter writer = new FileWriter("rps.txt");
            writer.write(text);
            writer.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    private static void DiscardPlayRequest(String clientName, String with, String s, PrintWriter out)
    {
        try
        {
            File file = new File("rps.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = "", text = "";
            boolean validReq = false;
            while((line = reader.readLine()) != null)
            {

                if(line.startsWith("<PlayRequest") & line.indexOf(" With=\""+ with) >= 0){
                    validReq = true;
                    line = line.replace("pending", "discarded");
                    out.println(line);
                    out.flush();
                    line = "";
                }

                if(line.startsWith("<OnlinePlayer name=\"" + clientName)){

                    line = line.replace("waiting", "idle");
                }

                text += line + "\n";

            }
            reader.close();

            if (!validReq) {
                out.println("<Invalid Rquest: No pending play request with " + with + "/>");
                out.flush();
            }

            FileWriter writer = new FileWriter("rps.txt");
            writer.write(text);
            writer.close();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


private static void UpdateGameStatus(String clientName, String choice, String s, PrintWriter out)
{
        try
        {
        File file = new File("rps.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";
        boolean validReq = false;

        while((line = reader.readLine()) != null)
        {
            if (line.startsWith("<GameSession>") & line.indexOf(clientName) >= 0) {
                line = line.replaceAll("=\"" + clientName + "\" choice=\"([^<]*)\"/>", "=\"" + clientName + "\" choice=\"" + choice + "\"/>");
                validReq = true;
            }

            text += line + "\n";
        }
        reader.close();

        if (!validReq) {
            out.println("<Invalid Rquest: no valid session!/>");
            out.flush();
        }

        FileWriter writer = new FileWriter("rps.txt");
        writer.write(text);
        writer.close();
    }
    catch (IOException ioe) {
        ioe.printStackTrace();
    }

}

private static void CheckGameStatus(String clientName, String s, PrintWriter out)
{
    try {
        File file = new File("rps.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "", tempLine = "";
        boolean inSession = false, found = false;
        while((line = reader.readLine()) != null) {

            if(line.startsWith("<GameSession>") & line.indexOf(clientName) >= 0) {
                out.println(line);
                out.flush();
            }
            text += line + "\n";

        }
        reader.close();


        FileWriter writer = new FileWriter("rps.txt");
        writer.write(text);
        writer.close();
    }
    catch (IOException ioe) {
        ioe.printStackTrace();
    }
    return;
}

private static  void LeaveGame(String clientName, String swrite, PrintWriter out)
{
    try
    {
        File file = new File("rps.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";

        while((line = reader.readLine()) != null)
        {

            if (line.startsWith("<OnlinePlayer name=\"" + clientName)){

                line = "";
                out.println("<Left Game/>");
                out.flush();
            } else if (line.startsWith("<GameSession") & line.indexOf(clientName) >= 0){

                line = "";
            } else if (line.startsWith("<PlayRequest") & line.indexOf(clientName) >= 0){

                line = "";
            }

            text += line + "\n";


        }
        reader.close();

        FileWriter writer = new FileWriter("rps.txt");
        writer.write(text);
        writer.close();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }

}

private static  void LeaveSession(String clientName, String swrite, PrintWriter out)
{
    try
    {
        File file = new File("rps.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";

        while((line = reader.readLine()) != null)
        {

            if(line.startsWith("<OnlinePlayer name=\"" + clientName)){

                line = line.replace("busy", "idle");
                out.println("<Left Session/>");
                out.flush();

            } else if(line.startsWith("<GameSession") & line.indexOf(clientName) >= 0){

                line = "";
            } else if(line.startsWith("<PlayRequest") & line.indexOf(clientName) >= 0){

                line = "";
            }

            text += line + "\n";


        }
        reader.close();

        FileWriter writer = new FileWriter("rps.txt");
        writer.write(text);
        writer.close();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }
}
}


























