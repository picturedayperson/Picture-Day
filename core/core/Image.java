package core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;

public class Image
{
	private BufferedImage image;
	private final File file;
	private final String formatName;
	private final int width;

	public Image(File imageFile, ImageReader imageReader)
	{
		file = imageFile;
		
		try
		{
			width = imageReader.getWidth(0);
			formatName = imageReader.getFormatName();
		}
		catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}
	}

	public int getWidth()
	{
		return width;
	}

	public String getName()
	{
		return file.getName();
	}

	public void write(File directory, ImageWidth width)
	{
		File outputFile = new File(directory, getName());
		
		if (outputFile.exists())
			return;
		
		if (image == null)
			try
			{
				System.out.println("about to read " + getName());
				image = ImageIO.read(file);
			}
			catch (IOException exception)
			{
				throw new RuntimeException(exception);
			}
		
		BufferedImage output;
		
		if (width.accommodates(getWidth()))
			output = image;
		else
			output = Scalr.resize(image, Method.ULTRA_QUALITY, Mode.FIT_TO_WIDTH, width.getValue());
		
		try
		{
			ImageIO.write(output, formatName, outputFile);
		}
		catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}
	}
}