package bauHACServerClient;

import java.io.Serializable;
import java.util.ArrayList;

public class BauArrayList implements Serializable
{
	private static final long serialVersionUID = 1L;
	public ArrayList<BauClientInfo> clientDataArrayList = new ArrayList<BauClientInfo>() ;
	
	@SuppressWarnings("unchecked")
	public BauArrayList(ArrayList<BauClientInfo> clientArrayList)
	{
		clientDataArrayList = (ArrayList<BauClientInfo>) clientArrayList.clone();
	}

}
