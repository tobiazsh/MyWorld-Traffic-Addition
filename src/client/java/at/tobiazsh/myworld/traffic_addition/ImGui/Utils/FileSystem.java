package at.tobiazsh.myworld.traffic_addition.ImGui.Utils;


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

		/**
		 * List all folders in the specified path
		 * @param path Path to the resource
		 * @return Folder
		 * @throws IOException Is thrown when an I/O error occurs while crawling the directory structure from the specified path or when an I/O error occurs while creating the file system
		 * @throws URISyntaxException Is thrown when an error occurs while creating the URI from the specified path
		 */
		public static Folder listFolders(String path) throws IOException, URISyntaxException {
			Folder folder = crawlDirectory(path);
			if (folder == null) return null; // If there aren't any folders in the currentFolder, return null to avoid NullPointerException
			return folder.removeFiles();
		}

		/**
		 * List all files in the specified path
		 * @param path Path to the resource
		 * @return Folder
		 * @throws IOException Is thrown when an I/O error occurs while crawling the directory structure from the specified path or when an I/O error occurs while creating the file system
		 * @throws URISyntaxException Is thrown when an error occurs while creating the URI from the specified path
		 */
		public static Folder listFiles(String path) throws IOException, URISyntaxException {
			Folder folder = crawlDirectory(path);
			if (folder == null) return null; // If there aren't any files in the currentFolder, return null to avoid NullPointerException
			return folder.concentrateFiles();
		}

		/**
		 * List all files and folders in the specified path
		 * @param path Path to the resource
		 * @return Folder
		 * @throws IOException Is thrown when an I/O error occurs while crawling the directory structure from the specified path or when an I/O error occurs while creating the file system
		 * @throws URISyntaxException Is thrown when an error occurs while creating the URI from the specified path
		 */
		public static Folder listALl(String path) throws IOException, URISyntaxException {
			return crawlDirectory(path);
		}

		/**
		 * Crawl the directory structure from the specified path
		 * @param path Path to the resource
		 * @return Folder
		 * @throws IOException Is thrown when an I/O error occurs while crawling the directory structure from the specified path or when an I/O error occurs while creating the file system
		 * @throws URISyntaxException Is thrown when an error occurs while creating the URI from the specified path
		 */

		private static Folder crawlDirectory(String path) throws IOException, URISyntaxException {
			// Retrieve the resource URL and handle missing resource
			URL resourceURL = MyWorldTrafficAdditionClient.class.getResource(path);
			if (resourceURL == null) {
				throw new IllegalArgumentException("Error: Specified path not found - " + path);
			}

			URI uri = resourceURL.toURI();
			Folder rootDir = null;

			try (CustomFileSystem fileSystem = initializeFileSystem(uri)) {
				Path resourcePath = Paths.get(uri);
				rootDir = new Folder(resourcePath.getFileName().toString(), path);

				// Populate the directory structure
				populateDirectory(rootDir, resourcePath, path);

            } catch (Exception e) {
				// Handle or log the exception
			}

			return rootDir;
		}

		/**
		 * Initialize the file system for JAR resources
		 * @param uri URI of the resource
		 * @return java.nio.file.FileSystem
		 * @throws IOException Is thrown when an I/O error occurs while creating the file system
		 */

		private static CustomFileSystem initializeFileSystem(URI uri) throws IOException {
			String uriStr = uri.toString();
			String fileSystemPath = uriStr.split("!")[0];

			if ("jar".equals(uri.getScheme())) {
				try {
					return new CustomFileSystem((FileSystems.getFileSystem(URI.create(fileSystemPath))), false); // Not closing the filesystem avoid a ClosedFileSystemException in Minecraft
				} catch (FileSystemNotFoundException e) {
					System.out.println("Creating new file system for: " + fileSystemPath);
					return new CustomFileSystem(FileSystems.newFileSystem(URI.create(fileSystemPath), new java.util.HashMap<>()), true); // But it should close it when it is opened just for this. Kinda like borrowing the value lol
				}
			}
			return null; // No file system needed for non-JAR resources
		}

		/**
		 * Populate the directory structure from the specified path
		 * @param rootDir Original directory
		 * @param resourcePath Path to the resource
		 * @param basePath Base path of the resource
		 * @throws IOException IOException is thrown when an I/O error occurs while crawling the directory structure from the specified path or when an I/O error occurs while creating the file system.
		 * @throws URISyntaxException URISyntaxException is thrown when an error occurs while creating the URI from the specified path.
		 */

		private static void populateDirectory(Folder rootDir, Path resourcePath, String basePath) throws IOException, URISyntaxException {
			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(resourcePath)) {
				for (Path entry : directoryStream) {
					String entryPath = basePath + entry.getFileName();
					String fileName = entry.getFileName().toString();

					if (Files.isDirectory(entry)) {
						entryPath = entryPath.concat("/");

						// Recursively crawl the subdirectory with its full path
						Folder subDir = FromResource.crawlDirectory(entryPath);
						rootDir.addContent(subDir);
					} else {
						rootDir.addContent(new File(fileName, entryPath).initFileType());
					}
				}
			}
		}
	}

	public static class CustomFileSystem implements AutoCloseable {
		public java.nio.file.FileSystem filesystem;
		public boolean shouldClose;

		public CustomFileSystem(java.nio.file.FileSystem fs, boolean shouldClose) {
			this.shouldClose = shouldClose;
			this.filesystem = fs;
		}

		@Override
		public void close() throws Exception {
			if (shouldClose && filesystem.isOpen()) {
				filesystem.close();
			}
		}
	}

	public static class DirectoryElement {
		public String path;
		public String name;

		public DirectoryElement(String name, String path) {
			this.path = path;
			this.name = name;
		}

		/**
		 * Check if the element is a currentFolder
		 * @return boolean
		 */
		public boolean isFolder() {
			return this instanceof Folder;
		}

		/**
		 * Check if the element is a file
		 * @return boolean
		 */
		public boolean isFile() {
			return this instanceof File;
		}
	}

	public static class Folder extends DirectoryElement implements Iterable<DirectoryElement> {
		public List<DirectoryElement> content = new ArrayList<>();

		public Folder(String name, String path) {
			super(name, path);
		}

		/**
		 * Add content to the currentFolder
		 * @param directoryElement Element to add
		 */
		public void addContent(DirectoryElement directoryElement) {
			content.add(directoryElement);
		}

		/**
		 * Remove all files from the currentFolder
		 */
		public Folder removeFiles() {
			Iterator<DirectoryElement> iterator = content.iterator();

			while (iterator.hasNext()) {
				DirectoryElement element = iterator.next();
				if (element.isFile()) iterator.remove(); // Remove file safely
				else if (element.isFolder()) ((Folder) element).removeFiles();
			}

			return this;
		}

		/**
		 * Remove all folders from the current directory
		 * @return Folder
		 */
		public Folder removeFoldersCurrentDir() {
			content.removeIf(DirectoryElement::isFolder);
			return this;
		}

		/**
		 * Remove all folders from the entire directory structure
		 * @return Folder
		 */
		public Folder concentrateFiles() {
			// Use a list to hold the files to add to rootFolder
			List<DirectoryElement> filesToMove = new ArrayList<>();

			// Helper method to recursively collect files and empty folders
			collectFilesRecursively(this, filesToMove);

			// Add all collected files to the root currentFolder
			this.content.addAll(filesToMove);

			// Optionally, clear the structure by removing all folders now that files are at the root
			this.content.removeIf(DirectoryElement::isFolder);

			return this;
		}

		public List<DirectoryElement> asList() {
			return content;
		}

		/**
		 * Concentrate files by type. Attention! Folders will stay and only file that don't match this filetype in the current Folder will be removed!
		 * @return Folder
		 */
		public Folder concentrateFileType(String fileType) {
			content.removeIf(element -> element.isFile() && !((File) element).getFileType().equals(fileType.toUpperCase()));
			return this;
		}

		/**
		 * Remove all files from the current Folder that don't match the specified filetype
		 * @param fileType Filetype to match
		 * @return Folder
		 */
		public Folder removeFilesByType(String fileType) {
			content.removeIf(element -> element.isFile() && !((File) element).getFileType().equals(fileType));
			return this;
		}

		/**
		 * Recursively collect files from subfolders
		 * @param currentFolder The current folder being processed
		 * @param filesToMoveList The list of files to move; Empty list when using method elsewhere
		 */
		private void collectFilesRecursively(Folder currentFolder, List<DirectoryElement> filesToMoveList) {
			Iterator<DirectoryElement> iterator = currentFolder.content.iterator();

			while (iterator.hasNext()) {
				DirectoryElement element = iterator.next();
				if (element.isFile()) {
					// Add files directly to the list
					filesToMoveList.add(element);
					iterator.remove(); // Remove the file from its original location
				} else if (element.isFolder()) {
					// Recursively collect files from subfolders
					collectFilesRecursively((Folder) element, filesToMoveList);
					iterator.remove(); // Remove the empty currentFolder after files are collected
				}
			}
		}

		public int size() {
			return content.size();
		}

		public void deleteObject(DirectoryElement element) {
			content.remove(element);
		}

		public DirectoryElement at(int index) {
			return content.get(index);
		}

		@Override
		public @NotNull Iterator<DirectoryElement> iterator() {
			return content.iterator();
		}
	}

	public static class File extends DirectoryElement {
		private String filetype;

		public File(String name, String path, String filetype) {
			super(name, path);
			this.filetype = filetype;
		}

		public File(String name, String path) {
			super(name, path);
		}

		/**
		 * Returns the filetype of a given filename
		 * @param name Name of the file
		 * @return The filetype as String in Uppercase
		 */
		public static String readFileType(String name) {
			return name.substring(name.lastIndexOf(".") + 1).toUpperCase();
		}

		/**
		 * Initialize the filetype of the file
		 * @return This File with Filetype
		 */
		public File initFileType() {
			this.filetype = readFileType(name);
			return this;
		}

		/**
		 * Get the filetype of this File
		 * @return Filetype as String
		 */
		public String getFileType() {
			return this.filetype;
		}
	}
}

