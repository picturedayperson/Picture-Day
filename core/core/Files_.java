package core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

	public static void copy(InputStream source, File destination)
	{
		try
		{
			Files.copy(source, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}
}