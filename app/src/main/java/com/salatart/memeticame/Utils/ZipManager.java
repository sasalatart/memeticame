package com.salatart.memeticame.Utils;

import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by sasalatart on 10/14/16.
 * Code from http://stacktips.com/tutorials/android/how-to-programmatically-zip-and-unzip-file-in-android
 */

public class ZipManager {
    public static String SEPARATOR = "zipManagerSeparator";
    public static int BUFFER_SIZE = 2048;

    public static Uri zip(String[] files, String zipFileName) throws IOException {
        BufferedInputStream origin = null;
        String zipPath = FileUtils.getMemeticameDirectory() + "/" + zipFileName;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipPath)));
        try {
            byte data[] = new byte[BUFFER_SIZE];

            for (String file : files) {
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                } finally {
                    origin.close();
                }
            }
        } finally {
            out.close();
        }

        fastUnzip(zipPath);
        return Uri.parse("file://" + zipPath);
    }

    public static ArrayList<String> unzip(String zipFile, String location) throws IOException {
        ArrayList<String> output = new ArrayList<>();

        try {
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile));
            try {
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();

                    if (ze.isDirectory()) {
                        File unzipFile = new File(path);
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {
                        FileOutputStream fout = new FileOutputStream(path, false);
                        try {
                            for (int c = zin.read(); c != -1; c = zin.read()) {
                                fout.write(c);
                            }
                            zin.closeEntry();
                        } finally {
                            fout.close();
                        }

                        output.add(path);
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e("INFO", "Unzip exception", e);
        }

        return output;
    }

    public static boolean fastUnzip(String inputZipFile) {
        String destinationDirectory = FileUtils.getMemeticameDirectory();

        try {
            List<String> zipFiles = new ArrayList();
            File sourceZipFile = new File(inputZipFile);
            File unzipDestinationDirectory = new File(destinationDirectory);
            ZipFile zipFile = new ZipFile(sourceZipFile, ZipFile.OPEN_READ);

            unzipDestinationDirectory.mkdir();

            Enumeration zipFileEntries = zipFile.entries();
            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(unzipDestinationDirectory, currentEntry);

                if (currentEntry.endsWith(".zip")) {
                    zipFiles.add(destFile.getAbsolutePath());
                }

                File destinationParent = destFile.getParentFile();

                destinationParent.mkdirs();

                try {
                    if (!entry.isDirectory()) {
                        BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
                        int currentByte;
                        byte data[] = new byte[BUFFER_SIZE];

                        FileOutputStream fos = new FileOutputStream(destFile);
                        BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);

                        while ((currentByte = is.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, currentByte);
                        }

                        dest.flush();
                        dest.close();
                        is.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            zipFile.close();

            for (Iterator<String> iterator = zipFiles.iterator(); iterator.hasNext(); ) {
                String zipName = iterator.next();
                unzip(zipName, destinationDirectory + File.separatorChar + zipName.substring(0, zipName.lastIndexOf(".zip")));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
