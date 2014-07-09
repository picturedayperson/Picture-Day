package core;

import java.io.File;

import javax.swing.JFileChooser;

public class DirectorySelection
{
	public static String act(File startingDirectory)
	{
		JFileChooser fileChooser = new JFileChooser(startingDirectory);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int selectionResult = fileChooser.showOpenDialog(null);
		
		if (selectionResult == JFileChooser.APPROVE_OPTION)
			return fileChooser.getSelectedFile().getAbsolutePath();
		else
			return null;
	}
}