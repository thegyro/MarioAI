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

	public static void dumpScores(ArrayList<Integer> scores) {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter( new FileWriter(filename, true)));
			
			StringBuilder sb = new StringBuilder;
			for(int i: scores) {
				sb.append(i);
				sb.append('\n');
			}

			sb.append('\n\n');
			String dump = sb.toString();
			writer.println(dump);


		} catch(IOException ioe) {
			System.err.println("IOException boss: " + ioe.getMessage());
		} finally {
			if(out != null)
				out.close();
		}
	}
}