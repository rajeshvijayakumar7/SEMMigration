
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileSplitLogger {

	protected static void logIt(String logPath, String message) {
		try {
			// Open given file in append mode.
			BufferedWriter out = new BufferedWriter(new FileWriter(logPath, true));
			out.write(timeIt() + "\t" + message + "\n");
			out.close();
		} catch (IOException e) {
			System.out.println(" Exception While writing log : " + e.getMessage());
		}
	}

	protected static void initLog(String logPath) {
		File logFile = new File(logPath);
		try {
			logFile.delete();
			logFile.createNewFile();
		} catch (IOException e) {
			System.out.println("Exception While Creating log File!");
		}
	}

	private static String timeIt() {
		String pattern = "MM-dd-yyyy hh:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		return date;
	}
}
