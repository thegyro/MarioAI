package myagent.utils;

import java.io.PrintWriter;
import myagent.agents.RLAgent.LearningParams;

public class Logger {
	public static void log(String message, List<Objects> args = null) {
		if(args != null)
			message = String.format(message, args.toArray());
		
		String filename = LearningParams.LOG_FILE_NAME;
		try {
		PrintWriter writer = new PrintWriter(new BufferedWriter( new FileWriter(filename, true)));
		writer.println(message);		
		} catch(IOException ioe) {
			System.err.println("IOException boss: " + ioe.getMessage());
		} finally {
			if(out != null)
				out.close();
		}

	}
}