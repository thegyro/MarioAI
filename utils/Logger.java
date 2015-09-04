package myagent.utils;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import myagent.agents.RLAgent.LearningParams;

public class Logger {
	public static void log(String message) {
		String filename = LearningParams.LOG_FILE_NAME;

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter( new FileWriter(filename, true)));
			writer.println(message);		
		} catch(IOException ioe) {
			System.err.println("IOException boss: " + ioe.getMessage());
		} finally {
			if(writer != null)
				writer.close();
		}
	}

	public static void log(String message, List<Object> args) {
		message = String.format(message, args.toArray());
		String filename = LearningParams.LOG_FILE_NAME;

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter( new FileWriter(filename, true)));
			writer.println(message);	
		} catch(IOException ioe) {
			System.err.println("IOException boss: " + ioe.getMessage());
		} finally {
			if(writer != null)
				writer.close();
		}
	}

	public static void dumpScores(ArrayList<Integer> scores) {
		PrintWriter writer = null;
		try {
			String filename = LearningParams.SCORE_FILE;
			writer = new PrintWriter(new BufferedWriter( new FileWriter(filename, true)));
			
			StringBuilder sb = new StringBuilder();
			for(int i: scores) {
				sb.append(i);
				sb.append('\n');
			}

			sb.append("\n\n");
			String dump = sb.toString();
			writer.println(dump);


		} catch(IOException ioe) {
			System.err.println("IOException boss: " + ioe.getMessage());
		} finally {
			if(writer != null)
				writer.close();
		}
	}
}