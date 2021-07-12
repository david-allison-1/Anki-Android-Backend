/*
 * Copyright (c) 2020 David Allison <davidallisongithub@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ankiweb.rsdroid.testing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.commons.exec.OS;

/**
 * Loads a librsdroid.so alternative to allow testing of rsdroid under a Robolectric-based environment
 */
public class RustBackendLoader {

    private static boolean alreadyLoaded;
    private static final HashMap<String, String> FILENAME_TO_PATH_CACHE = new HashMap<>();

    public static boolean PRINT_DEBUG = false;

    /**
     * Allows unit testing rsdroid under Robolectric <br/>
     * Loads (via {@link Runtime#load(String)}) a librsdroid.so alternative compiled for the current operating system.<br/><br/>
     *
     * This call is cached and is a no-op if called multiple times.
     *
     * @throws IllegalStateException OS is not Windows, Linux or macOS
     * @throws RuntimeException Failure when extracting library to load
     * @throws UnsatisfiedLinkError The library could not be loaded
     */
    public static void init() {
        if (!alreadyLoaded) {

            // This should help diagnose some issues,
            print("loading rsdroid-testing for: " + System.getProperty("os.name"));

            if (OS.isFamilyWindows()) {
                try {
                    load("rsdroid", ".dll");
                } catch (UnsatisfiedLinkError e) {
                    if (e.getMessage() != null && e.getMessage().contains("Can't find dependent libraries")) {
                        throw new UnsatisfiedLinkErrorEx("Failed to load library. Please install GCC (https://github.com/david-allison-1/Anki-Android-Backend/issues/46)", e);
                    }
                }

            } else if (OS.isFamilyMac()) {
                load("librsdroid", ".dylib");
            } else if (OS.isFamilyUnix()) {
                load("librsdroid", ".so");
            } else {
                String osName = System.getProperty("os.name");
                throw new IllegalStateException(String.format("Could not determine OS Type for: '%s'", osName));
            }

            alreadyLoaded = true;
        }
    }

    private static void print(String message) {
        if (PRINT_DEBUG) {
            System.out.println(message);
        }
    }

    /**
     * Allows unit testing rsdroid under Robolectric <br/>
     * Loads (via {@link Runtime#load(String)}) a librsdroid.so alternative compiled for the current operating system.<br/><br/>
     *
     * @param filePath A full path to the compiled .dll/.dylib/.so
     */
    public static void loadRsdroid(String filePath) {
        if (alreadyLoaded) {
            return;
        }

        loadPath(filePath);
        alreadyLoaded = true;
    }

    /**
     * loads a named file in the jar via {@link Runtime#load(String)}
     *
     * @param fileName The name of the file in the jar
     * @param extension The extension of the file in the jar
     *
     * @throws UnsatisfiedLinkError The library could not be loaded
     * @throws RuntimeException Failure when extracting library to load
     */
    private static void load(String fileName, String extension) {
        String path;
        try {
            path = getPathFromResourceStream(fileName, extension);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        loadPath(path);
    }

    private static void loadPath(String path) {
        try {
            Runtime.getRuntime().load(path);
        } catch (UnsatisfiedLinkError e) {
            if (!new File(path).exists()) {
                FileNotFoundException exception = new FileNotFoundException("Extracted file was not found. Maybe the temp folder was deleted. Please try again: '" + path + "'");
                throw new RuntimeException(exception);
            }
            if (e.getMessage() == null || !e.getMessage().contains("already loaded in another classloader")) {
                throw e;
            }
        }
    }

    /**
     * Extracts a named file from a JAR and saves it to a temp folder
     *
     * @param fileName The name of the file in the jar
     * @param extension The extension of the file in the jar
     * @return A path (on disk) to the extracted file from the JAR
     * @throws IllegalStateException The named file did not exist in the jar.
     * @throws IOException Error copying the file to the filesystem
     */
    private static String getPathFromResourceStream(String fileName, String extension) throws IOException {
        // TODO: Ensure that this is reasonably handled without too much copying.
        // Note: this will leave some data in the temp folder.
        String fullFilename = fileName + extension;

        // maintain a cache to the files so we reduce IO activity if a file has already been extracted.
        if (FILENAME_TO_PATH_CACHE.containsKey(fullFilename)) {
            return FILENAME_TO_PATH_CACHE.get(fullFilename);
        }

        String path = File.createTempFile(fileName, extension).getAbsolutePath();
        File targetFile = new File(path);

        // If our temp file already exists, return it
        // Likely a logical impossibility due to the implementation of createTempFile
        if (targetFile.exists() && targetFile.length() > 0) {
            return path;
        }

        try (InputStream rsdroid = RustBackendLoader.class.getClassLoader().getResourceAsStream(fullFilename)) {
            if (rsdroid == null) {
                throw new IllegalStateException("Could not find " + fullFilename);
            }

            try (OutputStream outStream = convertToOutputStream(targetFile)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = rsdroid.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
        }

        FILENAME_TO_PATH_CACHE.put(fullFilename, path);

        return path;
    }

    private static OutputStream convertToOutputStream(File targetFile) throws IOException {
        OutputStream outStream;
        try {
            outStream = new FileOutputStream(targetFile);
        } catch (Exception e) {
            throw new IOException("Could not open output file: {}", e);
        }
        return outStream;
    }
}