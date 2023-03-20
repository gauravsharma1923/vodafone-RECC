

package recc;

import java.nio.channels.Channel;
import java.util.HashMap;

public class Hash 
{

	private HashMap<String, String> channel=new HashMap<String,String>();
	private HashMap<String, String> use=new HashMap<String,String>();
	
	
	public HashMap<String,String> channel()
	{
		channel.put("R", "Recharge");
		channel.put("F", "First recharge");
		channel.put("U", "Ussd");
		channel.put("S", "SMS");
		channel.put("I", "IT");
		channel.put("B", "Backend");
		return channel;
		
	}
	
	public HashMap<String,String> use()
	{
		use.put("I", "Mobile Internet");
		use.put("B", "Mobile Broadband");
		use.put("P", "Shared Parent");
		use.put("C", "Shared Child");
		use.put("2G", "2G");
		use.put("3G", "3G/4G");
		use.put("4G", "4G Only");
		use.put("WF", "WIFI");
		
	return use;
	}	
	

}

