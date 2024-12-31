package com.firefly.feature;

import android.content.Context;
import android.os.Environment;

import com.firefly.utils.TurnipUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class TurnipDownloader {
    private static final String BASE_URL = "https://github.com/Vera-Firefly/TurnipDriver-CI/releases/download";
    private static final String FALLBACK_BASE_URL = "https://github.com/K11MCH1/AdrenoToolsDrivers/releases/download";
    private static final String VERSION_JSON_URL = BASE_URL + "/100000/version.json";
    private static final String DOWNLOAD_URL_TEMPLATE = "%s/%s/%s.zip";

    private static File dir;
    private static final Map<String, String> versionName = new HashMap<>();
    private static final Map<String, String> turnipName = new HashMap<>();

    private static void initDownloadDir(Context context) {
        if (dir == null) {
            dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Turnip");
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IllegalStateException("Failed to create download directory: " + dir.getAbsolutePath());
            }
        }
    }

    public static Set<String> getTurnipList(Context context) {
        File tempFile = null;
        initDownloadDir(context);

        try {
            tempFile = new File(dir, "version.json");

            URL url = new URL(VERSION_JSON_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
            }

            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONArray versions = jsonObject.getJSONArray("versions");

            Set<String> versionSet = new HashSet<>();
            versionName.clear();
            turnipName.clear();
            for (int i = 0; i < versions.length(); i++) {
                JSONObject versionObject = versions.getJSONObject(i);
                String version = versionObject.getString("version");
                String tag = versionObject.getString("tag");
                String fileName = versionObject.getString("fileName");
                versionName.put(version, tag);
                turnipName.put(tag, fileName);
                versionSet.add(version);
            }

            return versionSet;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    System.err.println("Failed to delete temp file: " + tempFile.getAbsolutePath());
                }
            }
        }
    }

    public static boolean downloadTurnipFile(Context context, String version) {
        initDownloadDir(context);

        String tag = versionName.get(version);
        if (tag == null) return false;

        String[] baseUrls = {BASE_URL, FALLBACK_BASE_URL};
        String fileUrl = null;

        for (String baseUrl : baseUrls) {
            String tempUrl = String.format(DOWNLOAD_URL_TEMPLATE, baseUrl, tag, version);
            if (checkUrlAvailability(tempUrl)) {
                fileUrl = tempUrl;
                break;
            }
        }

        if (fileUrl == null) {
            System.err.println("No available URL for downloading the file.");
            return false;
        }

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if (connection.getResponseCode() == 200) {
                File targetFile = new File(dir, version + ".zip");

                try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                }

                File extractDir = new File(dir, version);
                boolean success = unzipFile(targetFile, extractDir);
                if (!success) {
                    System.err.println("Failed to unzip file: " + targetFile.getAbsolutePath());
                    return false;
                }

                boolean deleted = targetFile.delete();
                if (!deleted) {
                    System.err.println("Failed to delete zip file: " + targetFile.getAbsolutePath());
                }

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkUrlAvailability(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (Exception e) {
            System.err.println("URL availability check failed: " + urlString);
            e.printStackTrace();
        }
        return false;
    }

    private static boolean unzipFile(File zipFile, File targetDir) {
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            System.err.println("Failed to create target directory: " + targetDir.getAbsolutePath());
            return false;
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            byte[] buffer = new byte[4096];

            while ((entry = zipInputStream.getNextEntry()) != null) {
                File outFile = new File(targetDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!outFile.exists() && !outFile.mkdirs()) {
                        System.err.println("Failed to create directory: " + outFile.getAbsolutePath());
                        return false;
                    }
                } else {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {
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

    public static boolean saveTurnipFile(Context context, String version) {
        String tag = versionName.get(version);
        if (tag == null) return false;

        String fileName = turnipName.get(tag);
        if (fileName == null) return false;

        File turnipDir = new File(dir, version);
        File sourceFile = new File(turnipDir, fileName);
        boolean success = copyFileToTurnipDir(sourceFile, version);

        deleteDirectory(turnipDir);

        return success;
    }

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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        dir.delete();
    }

}
