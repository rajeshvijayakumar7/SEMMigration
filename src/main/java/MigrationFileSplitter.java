import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MigrationFileSplitter {
	private int start = 0;
	private int end = 0;
	private static String message = "";
	private static String logPath = "";

	private void splitFileByAgents(File file, int numberOfAgents, String splitDirectory) throws Exception {
		// splitting the Parent file into smaller files based on the number of agents
		// and total number of lines in the file

		message = "Counting number of lines in the file \"" + file.getName() + "\"";
		FileSplitLogger.logIt(logPath, message);

		int numberOfLines = countLines(file);

		message = "Counting over, totally " + numberOfLines + " lines in the file \"" + file.getName() + "\"";
		FileSplitLogger.logIt(logPath, message);

		int[] linesPerFile = split(numberOfLines, numberOfAgents);

		message = "Lines are split into " + numberOfAgents + " smaller units based on the number of agents "
				+ numberOfAgents;
		FileSplitLogger.logIt(logPath, message);
		createDirectory(file, splitDirectory);

		for (int i = 0; i < numberOfAgents; i++) {
			start = end + 1;
			end = end + linesPerFile[i];
			writeFile(file, splitDirectory, start, end);
		}
	}

	private void createDirectory(File file, String dirName) {
		File splitDir = getSplitDir(file, dirName);
		if (!splitDir.exists()) {
			if (FileUtils.createSplitDirectory(file, dirName)) {
				message = "Split Directory Successfully Created!";
				FileSplitLogger.logIt(logPath, message);
			} else {
				message = "ERROR! Not able to create a split directory!";
				FileSplitLogger.logIt(logPath, message);
			}
		} else {
			message = "Split Directory already exists!";
			FileSplitLogger.logIt(logPath, message);

			if (FileUtils.clearDirectory(splitDir)) {
				message = "Split Directory Cleared!";
				FileSplitLogger.logIt(logPath, message);
			} else {
				message = "ERROR! Not able to create a CLEAR directory!";
				FileSplitLogger.logIt(logPath, message);
			}
		}
	}

	private int countLines(File file) {
		// getting number of lines from the parent file
		int lines = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			while (reader.readLine() != null) {
				lines++;
			}
		} catch (IOException e) {
			message = "Exception in counting lines in file: " + file.getName() + ", Error " + e.getMessage();
			FileSplitLogger.logIt(logPath, message);
		}
		return lines;
	}

	private void setLogFileName(File parentFile) {
		String parentFileWithoutExtension = FileUtils.removeFileExtensions(parentFile.getName());
		String logFileName = parentFileWithoutExtension + "-FileSplit.log";
		String[] pathElements = FileUtils.getPathElements(parentFile.getAbsolutePath(), logFileName);
		logPath = FileUtils.getPathFromArray(pathElements);
	}

	private String formatFileName(String fileName, int start, int end) {
		// ex: If Parent File name is 'Test' contains 4822 lines, then
		// Formatted file name will be'Test-S1-E4822.txt'
		String fileWithoutExtension = FileUtils.removeFileExtensions(fileName);
		String formattedFileName = fileWithoutExtension + "-S" + start + "-E" + end + ".txt";
		return formattedFileName;
	}

	private void writeFile(File parentFile, String splitDirectory, int start, int end) throws Exception {

		String parentFileName = parentFile.getName();
		String formattedFileName = formatFileName(parentFileName, start, end);
		String[] pathElements = FileUtils.getPathElements(parentFile.getAbsolutePath(), splitDirectory);
		String newPath = FileUtils.getPathFromArray(pathElements);
		String splitFilePath = newPath + FileUtils.getPathDelimiter() + formattedFileName;
		File splitFile = new File(splitFilePath);

		message = "writing \"" + splitFile.getName() + "\" file...";
		FileSplitLogger.logIt(logPath, message);

		int linesCount = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(parentFile))) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(splitFile))) {
				String line = "";
				while ((line = reader.readLine()) != null) {
					linesCount++;
					if (linesCount < start || linesCount > end) {
						continue;
					}
					writer.write(line + "\n");
				}
			}
		} catch (IOException e) {
			message = "Exception when writing a file: " + splitFile.getName() + ", ERROR Message \"" + e.getMessage()
					+ " \"";
			FileSplitLogger.logIt(logPath, message);
		}

		int totalLinesWritten = ((end - start) + 1);
		message = totalLinesWritten + " lines written to this file : \"" + splitFile.getName() + "\"";
		FileSplitLogger.logIt(logPath, message);
	}

	private File getSplitDir(File parentFile, String splitDirName) {
		String[] pathElements = FileUtils.getPathElements(parentFile.getAbsolutePath(), splitDirName);
		String splitDirPath = FileUtils.getPathFromArray(pathElements);
		File splitDir = new File(splitDirPath);
		return splitDir;
	}

	private int[] split(int totalLines, int numOfAgents) {
		int[] linesPerFile = new int[numOfAgents];
		int q = findQuotient(totalLines, numOfAgents);
		int r = findRemainder(totalLines, numOfAgents);
		for (int i = 0; i < numOfAgents; i++) {
			if (i == numOfAgents - 1) {
				linesPerFile[i] = q + r;
			} else {
				linesPerFile[i] = q;
			}
		}
		return linesPerFile;
	}

	private int findQuotient(int totalLines, int numOfAgents) {
		return totalLines / numOfAgents;
	}

	private int findRemainder(int totalLines, int numOfAgents) {
		return totalLines % numOfAgents;
	}

	public static String splitFile(String args) {
		// runtime arguments should be in the right order
		// first argument: path of the parent file <path should be upto the filename>
		// second argument: number of agents

		int numOfAgents = 0;
		String splitDirectory = "";
		File file = null;

		try {
			if (args != null) {
				// if (argsArray != null) {
				MigrationFileSplitter splitter = new MigrationFileSplitter();
				String[] argsArray = args.split("#", -1);
				String path = argsArray[0];
				file = new File(path);
				splitter.setLogFileName(file);
				FileSplitLogger.initLog(logPath);

				message = "initialized";
				FileSplitLogger.logIt(logPath, message);

				try {
					numOfAgents = Integer.parseInt(argsArray[1]);
					splitDirectory = argsArray[2];
					splitter.splitFileByAgents(file, numOfAgents, splitDirectory);
				} catch (NumberFormatException ex) {
					message = "Number of agents should be a number, \"" + argsArray[1] + "\" is not a number,ERROR \""
							+ ex.getMessage() + " \"";
					FileSplitLogger.logIt(logPath, message);
				}
			} else {
				message = "File path and Number of agents should be given, ERROR \"Arguments cannot be 'null' \"";
				throw new Exception(message);
			}
			message = "File splitting Completed, split into " + numOfAgents + " Files";
			FileSplitLogger.logIt(logPath, message);

		} catch (ArrayIndexOutOfBoundsException ex) {
			message = "File-path and number-of-agents should be given as runtime arguments, ERROR \"Arguments Missing!\"";
			FileSplitLogger.logIt(logPath, message);
		} catch (Exception ex) {
			message = "ERROR \"" + ex.getMessage() + "\"";
			FileSplitLogger.logIt(logPath, message);
		}
		return message;
	}

	public static void main(String[] args) {
		String params = args[0] + "#" + args[1] + "#" + args[2];
		splitFile(params);
	}
}
