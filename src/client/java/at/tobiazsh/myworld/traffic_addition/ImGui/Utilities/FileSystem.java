package at.tobiazsh.myworld.traffic_addition.ImGui.Utilities;


/*
 * @created 05/10/2024 (DD/MM/YYYY) - 00:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAddition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileSystem {
	public static class FromResource {

		/*
		 * Input: String (root path)
		 * Returns: List of Folders with both path and name
		 * Description: Creates a list of folders in a folder from a path in the jar resources
		 */
		public static Folder listFolders(String path) throws IOException {
			return crawlDirectory(path).removeFiles();
		}

		public static String resourceFolderLocator = "myworld_traffic_addition.folder.loc";

		// Works.. Somehow... Idk even know anymore please leave me alone
		// I know it's ugly but I am proud of it
		public static Folder crawlDirectory(String path) throws IOException {
			URL resourceURL = MyWorldTrafficAddition.class.getResource(normalizePath(path));
			if (resourceURL == null) throw new IOException("Error (Crawling Directories): Specified Resource path not found!");

			// Decode URL and handle Windows-specific leading slash in the path
			String decodedResourceURLStr = URLDecoder.decode(resourceURL.getPath(), StandardCharsets.UTF_8);
			decodedResourceURLStr = handleWindowsPath(decodedResourceURLStr);

			URL locateFileURL = MyWorldTrafficAddition.class.getClassLoader().getResource(resourceFolderLocator);
			if (locateFileURL == null) throw new IOException("Error (Crawling Directories): Couldn't locate resource folder locator file!");

			// Decode URL and handle Windows-specific leading slash in the path
			String decodedLocateFileURLStr = URLDecoder.decode(locateFileURL.getPath(), StandardCharsets.UTF_8);
			decodedLocateFileURLStr = handleWindowsPath(decodedLocateFileURLStr);

			Path resourcePath = Path.of(decodedResourceURLStr);
			Path resourceFolderPath = Path.of(decodedLocateFileURLStr).getParent();

			java.io.File resourceFolder = resourcePath.toFile();

			Path relativeResourcePath = Path.of("/".concat(normalizePath(resourceFolderPath.relativize(resourcePath).toString()))); // Normalizes path and adds "/" at the beginning

			Folder rootFolder = new Folder(relativeResourcePath.getFileName().toString(), relativeResourcePath.toString());

			java.io.File contents[] = resourceFolder.listFiles();

			assert contents != null;
			for (java.io.File file : contents) {
				if (file.isDirectory()) {
					Path absPath = file.toPath();
					Path relPath = resourceFolderPath.relativize(absPath);
					String relPathStr = normalizePath(relPath.toString());

					Folder subDir = new Folder(absPath.getFileName().toString(), relPathStr);

					subDir.addContent(crawlDirectory(relPathStr));
					rootFolder.addContent(subDir);
				} else if (file.isFile()) {
					Path absPath = file.toPath();
					Path relPath = resourceFolderPath.relativize(absPath);
					String relPathStr = normalizePath(relPath.toString());

					File newFile = new File(absPath.getFileName().toString(), relPathStr);
					rootFolder.addContent(newFile);
				}
			}

			return rootFolder;
		}
	}

	private static String handleWindowsPath(String str) {
		if (str.startsWith("/") && str.contains(":")) {
			str = str.substring(1);
		}

		return str;
	}

	public static String normalizePath(String str) {
		str = str.replaceAll("\\\\", "/");
		if (!str.startsWith("/")) str = "/".concat(str);
		if(!str.endsWith("/")) str = str.concat("/");
		return str;
	}

	public static class DirectoryElement {
		public String path;
		public String name;

		public DirectoryElement(String name, String path) {
			this.path = path;
			this.name = name;
		}
	}

	public static class Folder extends DirectoryElement implements Iterable<DirectoryElement> {
		public List<DirectoryElement> content = new ArrayList<>();

		public Folder(String name, String path) {
			super(name, path);
		}

		public void addContent(DirectoryElement directoryElement) {
			content.add(directoryElement);
		}

		// Remove Files from only the root directory
		public Folder removeFiles() {
			Iterator<DirectoryElement> iterator = content.iterator();

			while (iterator.hasNext()) {
				DirectoryElement element = iterator.next();
				if (element instanceof File) iterator.remove(); // Remove file safely
				else if (element instanceof Folder) ((Folder) element).removeFiles();
			}

			return this;
		}

		@Override
		public @NotNull Iterator<DirectoryElement> iterator() {
			return content.iterator();
		}
	}

	public static class File extends DirectoryElement {
		public File(String name, String path) {
			super(name, path);
		}
	}
}

