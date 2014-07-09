package core;

import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

public class LookAndFeelMode
{
	public static void set()
	{
		List<LookAndFeelInfo> installedLookAndFeels = Arrays.asList(UIManager.getInstalledLookAndFeels());
		int lookAndFeelIndex = selectLookAndFeelIndex(installedLookAndFeels);
		
		try
		{
			UIManager.setLookAndFeel(installedLookAndFeels.get(lookAndFeelIndex).getClassName());
		}
		catch (Exception exception)
		{
			throw new RuntimeException(exception);
		}
	}

	private static int selectLookAndFeelIndex(List<LookAndFeelInfo> installedLookAndFeels)
	{
		for (int i = 0; i < installedLookAndFeels.size(); i++)
			if (installedLookAndFeels.get(i).getClassName().equals(WindowsLookAndFeel.class.getName()))
				return i;

		return -1;
	}
}