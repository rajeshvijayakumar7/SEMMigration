import java.io.File;

public class FileUtils {

	protected static String getSplitFilePath(String parentFilePath, String splitFileName) {
		// getting split file path
		String[] pathElements = getPathElements(parentFilePath, splitFileName);
		String newPath = getPathFromArray(pathElements);
		return newPath;
	}

	protected static String removeFileExtensions(String fileName) {
		String[] extensions = { ".xlsx", ".xls", ".csv", ".txt" };
		for (String extension : extensions) {
			if (fileName.contains(extension)) {
				fileName = fileName.replace(extension, "");
			}
		}
		return fileName;
	}

	protected static boolean clearDirectory(File dir) {
		boolean result = false;
		for (File file : dir.listFiles())
			if (!file.isDirectory())
				file.delete();

		result = true;

		return result;
	}

	protected static String[] getPathElements(String parentFilePath, String splitFileName) {
		// to store split files in the same path where parent file exist, here
		// path elements are reconstructed with a newly splitted file name by replacing
		// the parent file name element
		// in the same path
		String[] pathElements = parentFilePath.split(getPathDelimiter());
		pathElements[pathElements.length - 1] = splitFileName;
		return pathElements;
	}

	protected static String getPathFromArray(String[] pathElements) {
		// building file path from the given array of path elements
		String newPath = "";
		int lastIndex = pathElements.length - 1;
		for (int i = 0; i < pathElements.length; i++) {
			if (i == lastIndex) {
				newPath = newPath.concat(pathElements[i]);
				continue;
			}
			newPath = newPath.concat(pathElements[i]).concat(getPathDelimiter());
		}
		return newPath;
	}

	protected static String getPathDelimiter() {
		// if it's unix/linux based system then it returns "/"
		// or else it's windows system then it returns "\\" as delimiter
		if (File.separator.compareTo("/") == 0) {
			return File.separator;
		} else {
			return File.separator + File.separator;
		}
	}

	protected static boolean createSplitDirectory(File parentFile, String splitDirectory) {
		boolean result = false;
		String[] pathElements = FileUtils.getPathElements(parentFile.getAbsolutePath(), splitDirectory);
		String newPath = FileUtils.getPathFromArray(pathElements);
		File dir = new File(newPath);

		if (dir.mkdir()) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

}
