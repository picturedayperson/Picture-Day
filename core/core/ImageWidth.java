package core;

public class ImageWidth implements Comparable<ImageWidth>
{
	private final Integer value;

	public ImageWidth()
	{
		value = null;
	}
	
	public ImageWidth(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}

	public String getValueString()
	{
		return value != null ? value.toString() : "full";
	}

	@Override
	public int compareTo(ImageWidth peer)
	{
		if (value == null || peer.value == null)
			throw new UnsupportedOperationException();
		else
			return value < peer.value ? -1 : value > peer.value ? 1 : 0;
	}

	public boolean accommodates(int width)
	{
		return value == null || width <= value;
	}
}