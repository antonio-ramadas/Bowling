package connections;

public class DataPacket {
	public String Event;
	public float Value;

	public void setData(String s, float f)
	{
		Event = s; Value = f;
	}
}
