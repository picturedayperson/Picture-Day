package core;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Configuration
{
	private final File file;
	private File lastInputDirectory;
	private boolean isOld;
	private Collection<ImageWidth> imageSizes;

	public Configuration()
	{
		try
		{
			String homeDirectoryName = Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart();
			
			if (homeDirectoryName.endsWith(".jar"))
				homeDirectoryName = homeDirectoryName.substring(0, homeDirectoryName.lastIndexOf("/") + 1);
			
			file = new File(homeDirectoryName + "Picture Day configuration.txt");
			
			if (!file.exists())
				file.createNewFile();
			
			for (String line: Files.readAllLines(file.toPath()))
				consider(line);
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}

	private void consider(String line)
	{
		int splitPoint = line.indexOf(":");
		
		if (splitPoint > 0)
		{
			String optionName = line.substring(0, splitPoint);
			String optionValue = line.substring(splitPoint + 1).trim();
			
			switch (optionName.toLowerCase())
			{
			case "last input directory":
				lastInputDirectory = new File(optionValue);
				break;
			case "has been used":
				isOld = Boolean.parseBoolean(optionValue);
				break;
			case "target image widths":
				imageSizes = parseSizes(optionValue);
				break;
			default:
				System.out.println("Unrecognized configuration setting:  " + line);
			}
		}
	}

	public void record()
	{
		ArrayList<String> lines = new ArrayList<>();
		
		lines.add("Last input directory:  " + lastInputDirectory.getAbsolutePath());
		lines.add("Has been used:  " + true);
		lines.add("Target image widths:  " + serialize(getImageSizes()));

		Files_.write(file, lines);
	}

	public void noteInputDirectory(String inputDirectory)
	{
		lastInputDirectory = new File(inputDirectory);
	}

	public File getLastInputDirectory()
	{
		return lastInputDirectory;
	}

	public boolean isNew()
	{
		return !isOld;
	}

	public Collection<ImageWidth> getImageSizes()
	{
		if (imageSizes != null)
			return imageSizes;
		else
			return Arrays.asList(new ImageWidth(), new ImageWidth(1800), new ImageWidth(1500), new ImageWidth(1200), new ImageWidth(900), new ImageWidth(700), new ImageWidth(550));
	}

	private Collection<ImageWidth> parseSizes(String optionValue)
	{
		ArrayList<ImageWidth> sizes = new ArrayList<>();

		for (String sizeText: optionValue.split(","))
		{
			sizeText = sizeText.trim();
			
			if (sizeText.equals("full") || sizeText.equals("\"full\""))
				sizes.add(new ImageWidth());
			else
			{
				try
				{
					sizes.add(new ImageWidth(Integer.parseInt(sizeText)));
				}
				catch (NumberFormatException numberFormatException)
				{
					System.out.println("Unrecognized image size:  " + sizeText);
				}
			}
		}
		
		return sizes;
	}

	private String serialize(Iterable<ImageWidth> imageSizes_)
	{
		StringBuilder result = new StringBuilder();
		
		for (ImageWidth imageWidth: imageSizes_)
		{
			if (result.length() > 0) result.append(", ");
			result.append(imageWidth.getValueString());
		}
		
		return result.toString();
	}
}