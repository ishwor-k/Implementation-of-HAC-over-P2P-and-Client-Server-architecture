package bauHACServerClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;


/**
 * A heartbeat_protocol implemented in HAC for Server-Client network
 * @author BAU team
 * @version 21 March 2020
 */
public class BauServerClient 
{
	// clientInfo is the ArrayList of the objects of BauCLientInfo 
	// containing both server and clients information
	ArrayList<BauClientInfo> clientInfo = new ArrayList<BauClientInfo>();
	
	// localHost is ones own ip address
	static InetAddress localHost ;
	static DatagramSocket socket;
	static int port = 7050;
	
	// id is the unique code given to each IP address
	int id;
	// serverId is the id of current server
	static int serverId;
	static InetAddress serverIp;
	// serverPos is the server IP location on clientInfo ArrayList
	static int serverPos;
	
	// constructor class
	public BauServerClient(int id)
	{
		this.id = id;
		prepareClientLists();
		
	}
	
	// method to prepare all the IP addresses in a network to be used
	// here we manually add the IPs in the clientInfo
	void prepareClientLists()
	{
		clientInfo.add(new BauClientInfo(1, "192.168.0.15"));
		clientInfo.add(new BauClientInfo(2, "192.168.0.52"));
		clientInfo.add(new BauClientInfo(3, "192.168.0.79"));
	}
	
	// method to update information about client of the given id
	public void updateClientInfo(int id)
	{
		// look for ones own id in the clientInfo
		for(int i = 0; i < clientInfo.size(); i++)
		{
			// if the status of current device is off then change it to true.
			if(clientInfo.get(i).id == id && (!clientInfo.get(i).status))
			{
				clientInfo.get(i).changeStatus();
			}
		}
		
	}
	
	// update the servers status in the clientInfo
	public void updateServerInfo()
	{
		if(!clientInfo.isEmpty())
		{
			serverId = clientInfo.get(serverPos).id;
			clientInfo.get(serverPos).changeStatus();
		}
		
	}
	
	// method to set all client status to false
	public void resetClientInfo()
	{
		for(int i = 0; i < clientInfo.size(); i++)
		{
			if(clientInfo.get(i).status)
			{
				clientInfo.get(i).changeStatus();
			}
		}
	}
	
	// method to display the status of the clients
	public void displayStatus()
	{
		for(int i = 0; i < clientInfo.size(); i++)
		{
			if(clientInfo.get(i).getIp().equals(serverIp))
			{
				System.out.println("client" + clientInfo.get(i).id + " is server");
			}
			else
			{
				System.out.println(clientInfo.get(i).toString());
			}
		}	
		System.out.println("------------------");
	}
	
	// method to send clientInfo ArrayList 
	// this method is to be used by server
	public void sendList()
	{
		try
		{
			for(int i = 0; i < clientInfo.size(); i++)
			{
				// destination is the clients
				InetAddress destIp = InetAddress.getByName( clientInfo.get(i).ip);
				// if a IP excluding ones own is available then send the ArrayList
				if( !destIp.equals(localHost))
				{
					ByteArrayOutputStream bstream = new ByteArrayOutputStream();
					ObjectOutputStream outStream = new ObjectOutputStream(bstream);
					BauArrayList sendArrayList = new BauArrayList(clientInfo);
					outStream.writeObject(sendArrayList);
					byte[] serializedData = bstream.toByteArray();
					DatagramPacket sendPacket = new DatagramPacket(serializedData, serializedData.length,destIp, port);
					socket.send(sendPacket);
					System.out.println("Arraylist sent");
				}
			}
		}
		catch (UnknownHostException e)
	    {
	        e.printStackTrace();
	    }
	    catch (SocketException e)
	    {
	        e.printStackTrace();
	    }
	    catch (IOException e)
	    {
	        e.printStackTrace();
	    }

	}
	
	
	// method to receive the ArrayList sent by a server
	// this method is to be used by clients
	@SuppressWarnings("unchecked")
	public void receiveList()
	{
		try
		{
			byte[] receivedData = new byte[1024];
			while(true)
			{
				synchronized (this) 
				{
					DatagramPacket receivedPacket = new DatagramPacket( receivedData, receivedData.length);
					socket.receive(receivedPacket);
					System.out.println("ArrayList received");
					ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivedData));
					BauArrayList receivedList = (BauArrayList)iStream.readObject();
					// save the received arraylist in clientInfo
					clientInfo = (ArrayList<BauClientInfo>) receivedList.clientDataArrayList.clone();
					// after receiving from server change the status of server to true.
					// this happens once in an interval after reset
					if( !clientInfo.get(serverPos).status)
					{
						clientInfo.get(serverPos).changeStatus();
					}
					iStream.close();
				}				
			}
		}

		catch (SocketException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException i) 
        {
            i.printStackTrace();
        } 
        catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
		
	// server method
	public void server()
	{
		try 
		{
			socket = new DatagramSocket(port);
		} 
		catch (SocketException e) 
		{
			e.printStackTrace();
		}
		
		// a thread to receive the BauPacket sent by client
		// it determines if a client is active or not
		Thread BauPacketReceiver = new Thread(new Runnable()
				{
					public void run()
					{
						try
						{
							byte[] receivedData = new byte[1024];
							while(true)
							{
								synchronized (this) 
								{										
									DatagramPacket receivedPacket = new DatagramPacket( receivedData, receivedData.length);
									socket.receive(receivedPacket);
									System.out.println("packet received");
			
									ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(receivedData));
									BauPacket receivedInfo = (BauPacket)iStream.readObject();
									System.out.println(receivedInfo.toString());
									iStream.close();		
									// get the id of client who sent the packet and update its status
									updateClientInfo(receivedInfo.getId());
								}				
							}
						}

						catch (SocketException e) 
						{
							e.printStackTrace();
						} 
						catch (IOException i) 
						{
							i.printStackTrace();
						} 
						catch (ClassNotFoundException e) 
						{
							e.printStackTrace();
						}
					}
				});
		BauPacketReceiver.start();
		
		// Thread to display the clients status and send information to all the clients
		// At the end it resets the clientInfo ArrayList for update in the availability
		Thread CheckAndDisplay = new Thread( new Runnable()
				{
					public void run()
					{
						while(true)
						{
							displayStatus();
							sendList();
							resetClientInfo();
							try 
							{
								Thread.sleep(15000);
							}
							catch (InterruptedException e) 
							{
								
								e.printStackTrace();
							}
						}
					}
				});
		CheckAndDisplay.start();
	}
	
	public void client()
	{
		try 
		{
			socket = new DatagramSocket(port);
		}
		catch (SocketException e1) 
		{
			e1.printStackTrace();
		}
		BauPacket toSend = new BauPacket(id);
		InetAddress destIp = serverIp ;
		ByteArrayOutputStream bstream = new ByteArrayOutputStream();
		ObjectOutputStream outStream;
		try 
		{
			System.out.println("Packet created");
			outStream = new ObjectOutputStream(bstream);
			outStream.writeObject(toSend);
			byte[] serializedData = bstream.toByteArray();
			DatagramPacket sendPacket = new DatagramPacket(serializedData, serializedData.length, destIp, port);
			socket.send(sendPacket);
			System.out.println("packet sent");
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		resetClientInfo();
		new Thread(new Runnable()
				{
					public void run()
					{
						receiveList();
					}
				}).start();
	}	
	
	
	// the method to make an IP a server or a client and
	// perform their respective duties
	void runServerClient(int serverPosition)
	{
		serverPos = serverPosition;		
		serverIp = clientInfo.get(serverPos).getIp();
		if( serverIp.equals(localHost))
		{
			System.out.println("server is up");
			server();
		}
		else
		{
			// run client for first time since the status of server is not updated first
			System.out.println("Client" + id + "is running for first time");
			client();
			
			// wait for 15 seconds for receiving ArrayList
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// while the server is on
			while(clientInfo.get(serverPos).status)
			{
				System.out.println("Client" + id + "is running");
				client();
				
				// wait 15 seconds for server to send arraylist
				// other wise check server status
				try {
					Thread.sleep(15000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}	
			
			// after a server fails, the new server will be the next Ip in the arrayList
			// if the failed server is last element then the next server will be the first
			System.out.println("Server down initializing new Server");
			serverPos = (serverPos + 1) % clientInfo.size();
			runServerClient(serverPos);
		}
		
	}	
	
	public static void main(String[] args)
	{
		// localHost is ones own ip
		try
		{
			localHost = InetAddress.getByName("192.168.0.15");
		} 
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		
		// use your own id in the argument
		BauServerClient client = new BauServerClient(1);
		
		// the argument will be the pos of server
		client.runServerClient(0);
	}
}


 