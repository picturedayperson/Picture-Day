package core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Captions
{
	private final Map<String, String> value;
	private final String title;
	private final String coda;

	public Captions(List<String> lines, ArrayList<Image> inputImages)
	{
		value = new TreeMap<>();
		String title_ = "";
		String coda_ = "";

		for (String line: lines)
		{
			int splitIndex = line.indexOf(":");
			
			if (splitIndex > 0)
			{
				String captionName = line.substring(0, splitIndex).trim();
				String captionText = line.substring(splitIndex + 1).trim();
				
				switch (captionName)
				{
				case "<title>":
					title_ = captionText;
					break;
				case "<coda>":
					coda_ = captionText;
					break;
				default:
					value.put(captionName, captionText);
				}
			}
		}
		
		for (Image image: inputImages)
			value.putIfAbsent(image.getName(), "");
		
		title = title_;
		coda = coda_;
	}

	public void write(File file)
	{
		List<String> lines = new ArrayList<>();
		
		addLine(lines, "<title>", title);
		
		for (Map.Entry<String, String> entry: value.entrySet())
			addLine(lines, entry.getKey(), entry.getValue());
		
		addLine(lines, "<coda>", coda);
		
		Files_.write(file, lines);
	}

	private void addLine(List<String> lines, String captionName, String captionText)
	{
		lines.add(captionName + ":  " + captionText);
	}
	
	public String getTitle()
	{
		return title;
	}

	public String get(Image image)
	{
		return value.get(image.getName());
	}

	public String getCoda()
	{
		return coda;
	}
}