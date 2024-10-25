package at.tobiazsh.myworld.traffic_addition.ImGui.Utilities;


/*
 * @created 05/10/2024 (DD/MM/YYYY) - 00:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
		public static Folder listFolders(String path) throws IOException, URISyntaxException {
			return crawlDirectory(path);
		}

		public static Folder crawlDirectory(String path) throws IOException, URISyntaxException {
			// Retrieve the resource URL and handle missing resource
			URL resourceURL = MyWorldTrafficAdditionClient.class.getResource(path);
			if (resourceURL == null) {
				throw new IllegalArgumentException("Error: Specified path not found - " + path);
			}

			URI uri = resourceURL.toURI();
			java.nio.file.FileSystem fileSystem = initializeFileSystem(uri);

			Path resourcePath = Paths.get(uri);
			Folder rootDir = new Folder(resourcePath.getFileName().toString(), path);
			populateDirectory(rootDir, resourcePath, path);

			// Clean up the file system if created
			if (fileSystem != null && fileSystem.isOpen()) {
				fileSystem.close();
			}

			// Debug
			for (DirectoryElement element : rootDir) {
				System.out.println("Element Path: " + element.path + "     Element Name: " + element.name);
			}

			return rootDir;
		}

		private static java.nio.file.FileSystem initializeFileSystem(URI uri) throws IOException {
			if ("jar".equals(uri.getScheme())) {
				try {
					return FileSystems.getFileSystem(uri);
				} catch (FileSystemNotFoundException e) {
					return FileSystems.newFileSystem(uri, new java.util.HashMap<>());
				}
			}
			return null; // No file system needed for non-JAR resources
		}

		private static void populateDirectory(Folder rootDir, Path resourcePath, String basePath) throws IOException, URISyntaxException {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(resourcePath)) {
				for (Path entry : directoryStream) {
					String entryPath = basePath + entry.getFileName();
					String fileName = entry.getFileName().toString();

					if (Files.isDirectory(entry)) {
						entryPath = entryPath.concat("/");

						// Recursively crawl the subdirectory with its full path
						Folder subDir = crawlDirectory(entryPath);
						rootDir.addContent(subDir);
					} else {
						rootDir.addContent(new File(fileName, entryPath));
					}
				}
			}
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

