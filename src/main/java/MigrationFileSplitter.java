import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MigrationFileSplitter {
	private int start = 0;
	private int end = 0;

	private static String message = "";
	private List<File> filesWritten = new ArrayList<>();

	private void splitFileByAgents(File file, int numberOfAgents, String splitDirectory) throws Exception {
		// splitting the Parent file into smaller files based on the number of agents
		// and total number of lines in the file

		message = "Counting number of lines in the file \"" + file.getName() + "\"";
		System.out.println(message);

		int numberOfLines = countLines(file);

		message = "Counting over, totally " + numberOfLines + " lines in the file \"" + file.getName() + "\"";
		System.out.println(message);

		int[] linesPerFile = split(numberOfLines, numberOfAgents);

		message = "Lines are split into " + numberOfAgents + " smaller units based on the number of agents "
				+ numberOfAgents;
		System.out.println(message);
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
			message = FileUtils.createSplitDirectory(file, dirName);
			System.out.println(message);
		} else {
			FileUtils.clearDirectory(splitDir);
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
			System.out.println("Exception in counting lines in file: " + file.getName() + ", Error " + e.getMessage());
		}
		return lines;
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
		System.out.println(message);

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
			System.out.println("Exception when writing a file: " + splitFile.getName() + ", ERROR Message \""
					+ e.getMessage() + " \"");
		}

		int totalLinesWritten = ((end - start) + 1);
		message = totalLinesWritten + " lines written to this file : \"" + splitFile.getName() + "\"";
		System.out.println(message);

		filesWritten.add(splitFile);
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

		message = "initialized";
		System.out.println(message);

		MigrationFileSplitter splitter = new MigrationFileSplitter();
		int numOfAgents = 0;
		String splitDirectory = "";
		File file = null;

		try {
			if (args != null) {
				// if (argsArray != null) {
				String[] argsArray = args.split("#", -1);
				String path = argsArray[0];
				file = new File(path);

				try {
					numOfAgents = Integer.parseInt(argsArray[1]);
					splitDirectory = argsArray[2];
					splitter.splitFileByAgents(file, numOfAgents, splitDirectory);
				} catch (NumberFormatException ex) {
					System.out.println("Number of agents should be a number, \"" + argsArray[1]
							+ "\" is not a number,ERROR \"" + ex.getMessage() + " \"");
				}
			} else {
				message = "File path and Number of agents should be given, ERROR \"Arguments cannot be 'null' \"";
				throw new Exception(message);
			}
			message = "File splitting Completed, split into " + numOfAgents + " Files";
			System.out.println(message);

		} catch (ArrayIndexOutOfBoundsException ex) {
			System.out.println(
					"File-path and number-of-agents should be given as runtime arguments, ERROR \"Arguments Missing!\"");
		} catch (Exception ex) {
			System.out.println("ERROR \"" + ex.getMessage() + "\"");
		}
		return message;
	}

	public static void main(String[] args) {
		String params = args[0] + "#" + args[1] + "#" + args[2];
		splitFile(params);
	}
}
