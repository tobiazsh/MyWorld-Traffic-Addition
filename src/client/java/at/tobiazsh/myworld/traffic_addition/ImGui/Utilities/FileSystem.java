package at.tobiazsh.myworld.traffic_addition.ImGui.Utilities;


/*
 * @created 05/10/2024 (DD/MM/YYYY) - 00:06
 * @project MyWorld Traffic Addition
 * @author Tobias
 */


import at.tobiazsh.myworld.traffic_addition.MyWorldTrafficAdditionClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSystem {
	public static class FromResource {
		public static List<Folder> listFolders(String path){
			List<Folder> countryFolders = new ArrayList<>();

			try {
				// URL resourceURL = MyWorldTrafficAdditionClient.class.getResource(path);

				URL resourceURL = MyWorldTrafficAdditionClient.class.getResource(path);
				if (resourceURL != null) {
					URI uri = resourceURL.toURI();
					java.nio.file.FileSystem fileSystem = null;

					try {
						// If the URI scheme is 'jar', try to get an existing filesystem or create a new one
						if (uri.getScheme().equals("jar")) {
							try {
								fileSystem = FileSystems.getFileSystem(uri); // Check if a FileSystem already exists
							} catch (FileSystemNotFoundException e) {
								fileSystem = FileSystems.newFileSystem(uri, new java.util.HashMap<>()); // Create a new one if it doesn't exist
							}
						}

						// Use the Path object to check files and folders
						Path resourcePath = Paths.get(uri);
						DirectoryStream<Path> directoryStream = Files.newDirectoryStream(resourcePath);

						for (Path entry : directoryStream) {
							if (Files.isDirectory(entry)) {
								// It's a directory, add it to the list
								countryFolders.add(new Folder(entry.getFileName().toString(), path + entry.getFileName() + "/"));
							} else {
								// It's a file, print an error
								System.err.println("Error: " + entry.getFileName() + " is a file, not a folder.");
							}
						}

					} finally {
						// Close the FileSystem if it was created in this block
						if (fileSystem != null && fileSystem.isOpen()) {
							fileSystem.close();
						}
					}
				} else {
					System.err.println("Error (Searching Directories): The specified path was not found");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			return countryFolders;
		}
	}

	public static class Folder {
		public String path;
		public String name;

		public Folder(String name, String path) {
			this.path = path;
			this.name = name;
		}
	}
}
