package core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class Files_
{
	public static void write(File file, Iterable<String> lines)
	{
		try
		{
			Files.write(file.toPath(), lines);
		}
		catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}
	}

	public static void copy(URL source, File destination)
	{
		try
		{
			Files.copy(new File(source.toURI()).toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
}