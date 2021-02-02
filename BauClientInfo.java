package bauHACP2P;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *  a class to hold informtaion about a client
 * @author BAU
 *
 */
public class BauClientInfo implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int id;
	public String ip;
	public boolean status;
	
	// constructor with id and ip as paramater
	public BauClientInfo( int id, String ip)
	{
		this.id = id;
		this.ip = ip;
		status = true;	
	}
	
	// change the status of the client
	public void changeStatus()
	{
		status = !status;	
	}
	
	public String toString()
	{
		return "Client" +  id + " network status is " + status ;
	}
	
	public InetAddress getIp()
	{
		InetAddress ipAdd = null;
		try 
		{
			ipAdd = InetAddress.getByName(ip);
		}
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		}
		return ipAdd;
	}
	
}
