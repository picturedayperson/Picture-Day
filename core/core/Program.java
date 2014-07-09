package core;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class Program
{
	public static void main(String[] arguments)
	{
		LookAndFeelMode.set();
		
		Configuration configuration = new Configuration();
		
		if (configuration.isNew())
		{
			System.out.println("Rough usage instructions:");
			System.out.println("");
			System.out.println("Run the main class " + Program.class.getCanonicalName() + ".  Point it to your directory full of story images.");
			System.out.println("You can do this either by selecting the directory by hand via the dialog box that comes up, or by passing");
			System.out.println("the directory path as the first command-line argument.");
			System.out.println("");
			System.out.println("A configuration text file will be generated near the .class files or JAR or whatever.  Edit it and run again");
			System.out.println("if you'd like to use different options.  The most (only?) noteworthy option is the one that specifies the");
			System.out.println("sizes (widths, specifically) of the images that will be supplied to the user.  The webpage this program");
			System.out.println("produces automatically resizes itself to fit the width of the user's browser window's viewing area.  But");
			System.out.println("it can only choose sizes that are specified in the aforementioned configuration option.  Specifying many");
			System.out.println("sizes might be more convenient for the user, unless they end up downloading too many files during resizing.");
			System.out.println("Specifying few sizes will reduce the total size of the output file set.  (There may be a more dynamic");
			System.out.println("way to handle this, like by doing it with just one image set and resizing in the browser, but I didn't go");
			System.out.println("that route.  Oh well.  Also--if you don't like this resizing system then just specify a single size, either");
			System.out.println(" \"full\" or a width number, and you'll end up with a simple-looking page that has no size menu.)");
			System.out.println("");
			System.out.println("A captions text file will be generated in the images directory.  Edit it by adding text after the colons, and");
			System.out.println("then run again to add text to your story.");
			System.out.println("");
			System.out.println("Appropriately resized images and various .html files will be created in a new subdirectory of the images");
			System.out.println("directory.  Upload this to your website or whatever to publish your story.  The index.html file is the entry");
			System.out.println("point for the user; that's what you send people a link to.  The rest should be automatic.");
			System.out.println("");
			System.out.println("However, if you don't like the default CSS or JavaScript logic or anything else, then go ahead and edit it.");
			System.out.println("Note that any internal changes you make to text files in the output directory will probably be overwritten");
			System.out.println("the next time the program runs--so move or copy the output elsewhere before making many such changes.");
			System.out.println("Internal changes to images will not be overwritten during later runs, and added files will not be deleted.");
			System.out.println("(In fact, nothing is ever explicitly deleted by this program.)  So if you change your image-size options,");
			System.out.println("you may be left with unneeded images of no-longer-used sizes in the output directory; delete these");
			System.out.println("yourself if you wish to.");
			System.out.println("");
			System.out.println("(Note:  This program isn't really optimized for low memory usage, so if you load many large input files, you");
			System.out.println("might run out of memory.  Run with something like -Xmx1200M to give Java more memory.  If even that isn't");
			System.out.println("enough, then the source code will have to be changed.  This is perfectly feasible but I just didn't bother");
			System.out.println("with it yet.)");
			System.out.println("");
			System.out.println("Licensing information:");
			System.out.println("This program's own source code is free for use by anybody for any purpose.");
			System.out.println("This program relies on the imgscalr Java library by Riyad Kalla, and might be distributed with an unmodified");
			System.out.println("copy of the source code for same.");
			System.out.println("This program relies on the imagesLoaded JavaScript library by Tomas Sardyha and David DeSandro, and includes");
			System.out.println("an unmodified copy of same.");
			System.out.println("");
		}
		
		String inputDirectory = null;
		
		if (arguments.length > 0)
			inputDirectory = arguments[0];
		
		if (inputDirectory == null)
			inputDirectory = DirectorySelection.act(configuration.getLastInputDirectory());
		
		if (inputDirectory != null)
		{
			configuration.noteInputDirectory(inputDirectory);
			run(inputDirectory, configuration.getImageSizes());
			configuration.record();
		}
	}

	private static void run(String inputDirectoryName, Collection<ImageWidth> targetWidths)
	{
		String picturesDirectoryName = "pictures";
		
		File inputDirectory = new File(inputDirectoryName);
		File htmlDirectory = makeSubdirectory(inputDirectory, "Picture Day");
		File picturesDirectory = makeSubdirectory(htmlDirectory, picturesDirectoryName);
		
		ArrayList<Image> inputImages = loadImages(inputDirectory);
		
		int inputWidth = calculateWidth(inputImages);
		final ArrayList<ImageWidth> relevantWidths = getRelevantWidths(targetWidths, inputWidth);
		ImageWidth greatestWidth = relevantWidths.get(relevantWidths.size() - 1);

		File captionsFile = createFile(inputDirectory, "captions.txt");
		Captions captions = gatherCaptions(captionsFile, inputImages);
		captions.write(captionsFile);

		Files_.copy(Program.class.getResourceAsStream("sizing.js"), new File(htmlDirectory, "sizing.js"));
		Files_.copy(Program.class.getResourceAsStream("imagesloaded.js"), new File(htmlDirectory, "imagesloaded.js"));
		Files_.copy(Program.class.getResourceAsStream("style.css"), new File(htmlDirectory, "style.css"));

		Action1<List<String>> startHtml = null;
		Action1<List<String>> finishHtml = null;
		
		for (ImageWidth width: relevantWidths)
		{
			String sizeDirectoryName = width.getValueString();
			String greatestSizeDirectoryName = greatestWidth.getValueString();
			
			File resizedPicturesDirectory = makeSubdirectory(picturesDirectory, sizeDirectoryName);
			
			String resizedPicturesPath = picturesDirectoryName + "/" + sizeDirectoryName + "/";
			String largestPicturesPath = picturesDirectoryName + "/" + greatestSizeDirectoryName + "/";
			
			for (Image image: inputImages)
			{
				String picturePath = resizedPicturesPath + image.getName();
				
				System.out.println("about to write " + picturePath);
				image.write(resizedPicturesDirectory, width);
			}
			
			final ArrayList<String> html = new ArrayList<>();
			
			Action3<Integer, String, String> addText = new Action3<Integer, String, String>()
				{
					@Override
					public void perform(Integer tabLevel, String className, String text)
					{
						String tabs = "";
						
						for (int i = 0; i < tabLevel; i++)
							tabs += "	";

						html.add(tabs + "<div class=\"" + className + "\">");
						html.add(tabs + "	" + text);
						html.add(tabs + "</div>");
					}
				};
			
			startHtml = new Action1<List<String>>()
			{
				@Override
				public void perform(List<String> html)
				{
					final ArrayList<String> size = new ArrayList<>();
					
			html.add("<!DOCTYPE html>");
			html.add("<html>");
			html.add("	<head>");
			html.add("		<title>Picture Day</title>");
			html.add("		<script src=\"imagesloaded.js\"></script>");
			html.add("		<script src=\"sizing.js\"></script>");
			html.add("		<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\">");
			html.add("	</head>");
			html.add("	<body>");
			html.add("		<div id=\"topMenu\">");
			html.add("			<div class=\"size-menu-x\"></div>");
			html.add("			<div class=\"size-title\">Size:</div>");
			for (ImageWidth pageWidth: relevantWidths)
			size.add("			<div class=\"size-item\" data-size=\"" + pageWidth.getValue() + "\" data-page=\"" + getIndexFileName(pageWidth) + "\">" + pageWidth.getValue() + "</div>");
			size.add("			<div class=\"size-item\" data-size=\"whatfits\">best fit</div>");
			for (String line: size) html.add(line);
			html.add("		</div>");
			html.add("		<div id=\"rightMenu\">");
			html.add("			<div class=\"size-menu-x\"></div>");
			html.add("			<div class=\"size-title\">Size</div>");
			for (String line: size) html.add(line);
			html.add("		</div>");
			html.add("		<div id=\"menuNub\"></div>");
				}
			};
			startHtml.perform(html);
			html.add("		<div class=\"main-container\" style=\"width: " + width.getValueString() + "px\">");
			html.add("			<a id=\"top\"></a>");
			addText.perform(3, "story-title", captions.getTitle());
			
			for (Image image: inputImages)
			{
				html.add("			<div class=\"story-item\">");
				html.add("				<a id=\"" + image.getName() + "\" href=\"" + largestPicturesPath + image.getName() + "\">");
				html.add("					<img src=\"" + resizedPicturesPath + image.getName() + "\" alt=\"An image failed to load.\">");
				html.add("				</a>");
				addText.perform(4, "caption", captions.get(image));
				html.add("			</div>");
			}

			html.add("			<a id=\"coda\"></a>");
			addText.perform(3, "story-coda", captions.getCoda());
			html.add("		</div>");
			finishHtml = new Action1<List<String>>()
			{
				@Override
				public void perform(List<String> html)
				{
			html.add("	</body>");
			html.add("</html>");
				}
			};
			finishHtml.perform(html);
			
			File htmlFile = createFile(htmlDirectory, getIndexFileName(width));
			Files_.write(htmlFile, html);
		}

		final ArrayList<String> html = new ArrayList<>();
		
		startHtml.perform(html);
		html.add("		<div class=\"main-container\" style=\"width: 200px\"></div>");
		finishHtml.perform(html);
		
		File indexFile = createFile(htmlDirectory, "index.html");
		Files_.write(indexFile, html);
		
		System.out.println("done");
	}

	private static String getIndexFileName(ImageWidth width)
	{
		return "index" + width.getValueString() + ".html";
	}

	private static File createFile(File directory, String fileName)
	{
		File file = new File(directory, fileName);
		
		if (!file.exists())
			try
			{
				file.createNewFile();
			}
			catch (IOException exception)
			{
				throw new RuntimeException(exception);
			}
		
		return file;
	}

	private static Captions gatherCaptions(File captionsFile, ArrayList<Image> inputImages)
	{
		List<String> lines;
		
		try
		{
			lines = Files.readAllLines(captionsFile.toPath());
		}
		catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}
		
		return new Captions(lines, inputImages);
	}

	private static ArrayList<Image> loadImages(File inputDirectory)
	{
		ArrayList<Image> inputImages = new ArrayList<>();

		for (File imageFile: inputDirectory.listFiles(new FileFilter()
		{
			@Override
			public boolean accept(File pathname)
			{
				return pathname.isFile();
			}
		}))
		{
			Image image = readImage(imageFile);
			
			if (image != null)
				inputImages.add(image);
		}
		
		inputImages.sort(new Comparator<Image>()
		{
			@Override
			public int compare(Image image1, Image image2)
			{
				return image1.getName().compareTo(image2.getName());
			}
		});
		
		return inputImages;
	}

	private static ArrayList<ImageWidth> getRelevantWidths(Collection<ImageWidth> targetWidths, int inputWidth)
	{
		ArrayList<ImageWidth> relevantWidths = new ArrayList<>();
		boolean accommodatingWidthEncountered = false;
		
		for (ImageWidth width: targetWidths)
			if (!width.accommodates(inputWidth))
				relevantWidths.add(width);
			else
				accommodatingWidthEncountered = true;
		
		if (accommodatingWidthEncountered)
			relevantWidths.add(new ImageWidth(inputWidth));
		
		Collections.sort(relevantWidths);
		
		if (relevantWidths.isEmpty())
			throw new RuntimeException("No widths were selected");
		
		return relevantWidths;
	}

	// the maximal width, where a few of the largest images are ignored
	private static int calculateWidth(ArrayList<Image> inputImages)
	{
		float insignificantPortionSize = .05f;
		
		ArrayList<Integer> widths = new ArrayList<>();
		
		for (Image image: inputImages)
			widths.add(image.getWidth());
		
		Collections.sort(widths);
		
		int insignificantEntryCount = (int) Math.ceil(widths.size() * insignificantPortionSize);
		if (insignificantEntryCount >= widths.size())
			insignificantEntryCount = widths.size() - 1;
		
		return widths.get(widths.size() - 1 - insignificantEntryCount);
	}

	private static File makeSubdirectory(File directory, String subdirectoryName)
	{
		File subdirectory = new File(directory, subdirectoryName);
		
		if (!subdirectory.exists())
			subdirectory.mkdir();
		
		return subdirectory;
	}

	private static Image readImage(File imageFile)
	{
		try
		{
			ImageInputStream stream = ImageIO.createImageInputStream(imageFile);
			
			if (stream == null)
				return null;

			Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(stream);
			
			if (!imageReaders.hasNext())
			{
				stream.close();
				return null;
			}
			
			ImageReader imageReader = imageReaders.next();
			imageReader.setInput(stream);
			
			return new Image(imageFile, imageReader);
		}
		catch (IOException exception)
		{
			throw new RuntimeException(exception);
		}
	}
}