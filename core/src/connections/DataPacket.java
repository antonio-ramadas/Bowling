package connections;

public class DataPacket {
	public String Event;
	public float Value;

	public DataPacket()
	{
		
	}
	public DataPacket(String event2, float value2) {
		setData(event2,value2);
	}

	public void setData(String s, float f)
	{
		Event = new String(s); Value = f;
	}
}
