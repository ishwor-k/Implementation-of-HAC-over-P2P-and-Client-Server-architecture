package bauHACP2P;
import java.io.Serializable;

/**
 * Class for packet to send information over the network
 * @author The BAU team
 * @version 15 March, 2020
 */
public class BauPacket implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int id;
	
	// default constructor
	public BauPacket()
	{
		
	}
	
	// constructor with parameters
	public BauPacket(int id)
	{
		this.id = id;
	}
	
	// get methods
	public int getId()
	{
		return id;
	}
	// set methods
	public void setId(int id)
	{
		this.id = id;
	}

	public String toString()
	{
		return "id: " + id ; 
	}

}
