package bauHACP2P;
import java.net.*;
import java.io.*;
import java.util.*;


/**
 * A heartbeat_protocol implemented in HAC for P2P network
 * @author BAU team
 * @version 16 March 2020
 */

public class BauP2P 
{
	// id is unique to each client
	private static int id = 3;
	
	// the port number to be used by socket
	private static int port = 6050;
	
	// the ArrayList of clients information
	static ArrayList<BauClientInfo> clientInfo = new ArrayList<BauClientInfo>();

	// the socket to be used
	static DatagramSocket socket;
	
	static InetAddress localHost ;
	
	// method to update information about client of the given id
	public static void updateClientInfo(int id)
	{
		for(int i = 0; i < clientInfo.size(); i++)
		{
			// if the status of current client is false then 
			// set it to true
			if(clientInfo.get(i).id == id && (!clientInfo.get(i).status))
			{
				clientInfo.get(i).changeStatus();
			}
		}
		
	}
	
	// method to set all client status to false
	public static void resetClientInfo()
	{
		for(int i = 0; i < clientInfo.size(); i++)
		{
			if(clientInfo.get(i).status)
			{
				clientInfo.get(i).changeStatus();
			}
			
			// if it is own ip then it is automatically true
			if(clientInfo.get(i).getIp().equals(localHost))
			{
				clientInfo.get(i).status = true;
			}
		}
	}
	
	// method to display the status of the clients
	public static void displayStatus()
	{
		for(int i = 0; i < clientInfo.size(); i++)
		{
				System.out.println(clientInfo.get(i).toString());
		}	
		System.out.println("------------------");
	}
	
	// main method
	public static void main(String[] args) 
	{
		// add clients information in the arraylist manually
		// here you can add more clients in a network
		clientInfo.add(new BauClientInfo(1, "192.168.0.15"));
		clientInfo.add(new BauClientInfo(2, "192.168.0.52"));
		clientInfo.add(new BauClientInfo(3, "192.168.0.79"));
		try 
		{
			localHost = InetAddress.getByName("192.168.0.79"); 
		}
		catch (UnknownHostException e2) 
		{
			e2.printStackTrace();
		}
		
		System.out.println("Ready.");
		
		System.out.println(localHost);
		
		// initialize socket for communication
		try 
		{
			socket = new DatagramSocket(port);
		} 
		catch (SocketException e1) 
		{
			e1.printStackTrace();
		}
		
		// thread for running server( receiver) to receive data from clients
		Thread receivedInfo = new Thread(new Runnable()
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
		
		// thread for running client( sender) to send data
		Thread sendInfo = new Thread(new Runnable()
				{
					public void run()
					{
						//sendInfo();
						BauPacket toSend = new BauPacket(id);
						
						try
						{
							while(true)
							{
								synchronized (this) 
								{		
									for(int i = 0; i < clientInfo.size(); i++)
									{
										InetAddress destIp = InetAddress.getByName( clientInfo.get(i).ip);
										// if the IP is reachable is not ones own IP
										if( /*clientInfo.get(i).status &&*/ (!destIp.equals(localHost)))
										{
											ByteArrayOutputStream bstream = new ByteArrayOutputStream();
											ObjectOutputStream outStream = new ObjectOutputStream(bstream);
											outStream.writeObject(toSend);
											byte[] serializedData = bstream.toByteArray();
											DatagramPacket sendPacket = new DatagramPacket(serializedData, serializedData.length,destIp, port);
											socket.send(sendPacket);
											System.out.println("packet sent");
										}								
									}
								}
								
								// wait for 5 seconds before sending another data
								Thread.sleep(5000);
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
				        } catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						
					}			
				});
		
		// to show the status of all the clients
		Thread showStatus = new Thread(new Runnable()
		{
			public void run()
			{
				while(true)
				{
						try
						{
							
							displayStatus();
							resetClientInfo();
							Thread.sleep(15000);							
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						
				}
			}
		});
		
		// start threads
		receivedInfo.start();	
		sendInfo.start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		showStatus.start();
		/*
		try 
		{
			receivedInfo.join();
			sendInfo.join();
			showStatus.join();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	*/
	
	}
}

	


