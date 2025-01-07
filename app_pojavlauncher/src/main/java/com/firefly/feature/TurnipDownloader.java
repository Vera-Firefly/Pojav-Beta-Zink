package com.firefly.feature;

import android.content.Context;
import android.os.Environment;

import com.firefly.utils.TurnipUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TurnipDownloader {
    private static final String BASE_URL = "github.com/Vera-Firefly/TurnipDriver-CI/releases/download";
    private static final String FALLBACK_BASE_URL = "github.com/K11MCH1/AdrenoToolsDrivers/releases/download";
    private static final String VERSION_JSON_PATH = "/100000/version.json";
    private static final String DOWNLOAD_URL_TEMPLATE = "%s/%s/%s.zip";

    private static String DLS = "https://";
    private static File downloadDir;
    private static final Map<String, String> versionNameMap = new HashMap<>();
    private static final Map<String, String> turnipNameMap = new HashMap<>();
    private static volatile boolean isCancelled = false;
    private static Context appContext;

    // Initialize the downloader
    public static void initialize(Context context) {
        appContext = context.getApplicationContext();
        initDownloadDir();
    }

    // Initialize the download directory
    private static void initDownloadDir() {
        if (downloadDir == null) {
            downloadDir = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Turnip");
            if (!downloadDir.exists() && !downloadDir.mkdirs()) {
                throw new IllegalStateException("Unable to create download directory: " + downloadDir.getAbsolutePath());
            }
        }
    }

    public static void cancelDownload() {
        isCancelled = true;
    }

    public static boolean isDownloadCancelled() {
        return isCancelled;
    }

    // Get the list of Turnip versions
    public static List<String> getTurnipList(int sourceType) {
        isCancelled = false;
        String versionUrl = resolveVersionUrl(sourceType);

        if (versionUrl == null) {
            System.err.println("No valid version URL found.");
            return null;
        }

        File versionFile = getDownloadSubDir("version.json");
        if (!downloadFile(versionUrl, versionFile)) {
            return null;
        }

        return parseVersionFile(versionFile);
    }

    // Parse the version file
    private static List<String> parseVersionFile(File versionFile) {
        List<String> turnipVersions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(versionFile))) {
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
            JSONArray versionsArray = jsonObject.getJSONArray("versions");

            for (int i = 0; i < versionsArray.length(); i++) {
                JSONObject versionObject = versionsArray.getJSONObject(i);
                String version = versionObject.getString("version");
                String tag = versionObject.getString("tag");
                String fileName = versionObject.getString("fileName");

                versionNameMap.put(version, tag);
                turnipNameMap.put(tag, fileName);
                turnipVersions.add(version);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            cleanup(versionFile);
        }
        return turnipVersions;
    }

    // Download and extract the specified version
    public static boolean downloadTurnipFile(String version) {
        String tag = versionNameMap.get(version);
        if (tag == null) {
            System.err.println("No tag found for version: " + version);
            return false;
        }

        String fileUrl = resolveDownloadUrl(tag, version);
        if (fileUrl == null) {
            System.err.println("No valid download URL found for version: " + version);
            return false;
        }

        File zipFile = getDownloadSubDir(version + ".zip");
        if (!downloadFile(fileUrl, zipFile)) {
            return false;
        }

        File extractDir = getDownloadSubDir(version);
        if (!unzipFile(zipFile, extractDir)) {
            System.err.println("Failed to extract the file: " + zipFile.getAbsolutePath());
            return false;
        }

        cleanup(zipFile); // Clean up the zip file after extraction
        return true;
    }

    // Save the extracted file
    public static boolean saveTurnipFile(String version) {
        String tag = versionNameMap.get(version);
        String fileName = turnipNameMap.get(tag);
        if (tag == null || fileName == null) {
            System.err.println("Invalid version or file name.");
            return false;
        }

        File sourceFile = getDownloadSubDir(version + "/" + fileName);
        return copyFileToTurnipDir(sourceFile, version);
    }

    // Resolve the URL to fetch the version.json file
    private static String resolveVersionUrl(int sourceType) {
        String[] sources = {
            "https://",
            "https://mirror.ghproxy.com/"
        };
        if (sourceType > 0 && sourceType <= sources.length) {
            DLS = sources[sourceType - 1];
            return DLS + BASE_URL + VERSION_JSON_PATH;
        }
        for (String source : sources) {
            String testUrl = source + BASE_URL + VERSION_JSON_PATH;
            if (checkUrlAvailability(testUrl)) {
                DLS = source;
                return testUrl;
            }
        }
        return null;
    }

    // Resolve the download URL for the given version and tag
    private static String resolveDownloadUrl(String tag, String version) {
        String[] baseUrls = {
            DLS + BASE_URL,
            DLS + FALLBACK_BASE_URL
        };
        for (String baseUrl : baseUrls) {
            String testUrl = String.format(DOWNLOAD_URL_TEMPLATE, baseUrl, tag, version);
            if (checkUrlAvailability(testUrl)) {
                return testUrl;
            }
        }
        return null;
    }

    // Generic file download method
    private static boolean downloadFile(String fileUrl, File targetFile) {
        try (InputStream inputStream = new URL(fileUrl).openStream();
             FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if (isCancelled) {
                    cleanup(targetFile);
                    return false;
                }
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Unzip the file
    private static boolean unzipFile(File zipFile, File targetDir) {
        if (!zipFile.exists() || !zipFile.isFile()) {
            System.err.println("Invalid ZIP file: " + zipFile.getAbsolutePath());
            return false;
        }

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            System.err.println("Failed to create extraction target directory: " + targetDir.getAbsolutePath());
            return false;
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outFile.mkdirs() && !outFile.isDirectory()) {
                        throw new IOException("Failed to create directory: " + outFile.getAbsolutePath());
                    }
                } else {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check if a URL is valid
    private static boolean checkUrlAvailability(String urlString) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return connection.getResponseCode() >= 200 && connection.getResponseCode() < 400;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Copy file to the target directory
    private static boolean copyFileToTurnipDir(File sourceFile, String folderName) {
        File targetDir = new File(TurnipUtils.INSTANCE.getTurnipDir(), folderName);
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            return false;
        }
        File targetFile = new File(targetDir, "libvulkan_freedreno.so");
        try (InputStream inputStream = new FileInputStream(sourceFile);
             OutputStream outputStream = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            File sourceFiles = getDownloadSubDir(folderName);
            cleanup(sourceFiles);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper methods
    private static File getDownloadSubDir(String subPath) {
        return new File(downloadDir, subPath);
    }

    private static void cleanup(File... files) {
        for (File file : files) {
            if (file != null && file.exists()) {
                deleteRecursively(file);
            }
        }
    }

    private static boolean deleteRecursively(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            for (File child : fileOrDir.listFiles()) {
                deleteRecursively(child);
            }
        }
        return fileOrDir.delete();
    }
}