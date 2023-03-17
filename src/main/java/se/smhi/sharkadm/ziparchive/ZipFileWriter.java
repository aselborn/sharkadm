package se.smhi.sharkadm.ziparchive;

/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import se.smhi.sharkadm.translate.TranslateCodesManager_NEW;
import se.smhi.sharkadm.utils.ParseFileUtil;

/**
 * Utility class used to produce SHARK archive files.
 */
public class ZipFileWriter {

    public static void createZipArchive(String zipFileName,
                                        String datatype,
                                        String fromPath,
                                        String toPath,
                                        ZipArchiveGenerateData dataVisitor,
                                        ZipArchiveGenerateDataColumns dataColumnsVisitor,
                                        ZipArchiveGenerateMetadataAuto metadataVisitor) {
        try {
            // Out directory.
            Path outPath = Paths.get(toPath);
            // Check if directory exists?
            if (!Files.exists(outPath)) {
                try {
                    // Create directory and parents.
                    Files.createDirectories(outPath);
                } catch (IOException e) {
                    // Fail to create directory.
                    e.printStackTrace();
                    return;
                }
            }

            // Create zip file.
            String zipName = toPath.concat("\\").concat(zipFileName);
            ZipOutputStream zipArchiveStream = new ZipOutputStream(new FileOutputStream(zipName));

            // Add README files.
            String readmeEn = getReadme(datatype, "en");
            String readmeSv = getReadme(datatype, "sv");

            zipArchiveStream.putNextEntry(new ZipEntry("README.txt"));
            byte[] readmeEnBytes = readmeEn.getBytes();
            zipArchiveStream.write(readmeEnBytes, 0, readmeEn.length());
            zipArchiveStream.closeEntry();

            zipArchiveStream.putNextEntry(new ZipEntry("README_sv.txt"));
            byte[] readmeSvBytes = readmeSv.getBytes();
            zipArchiveStream.write(readmeSvBytes, 0, readmeSv.length());
            zipArchiveStream.closeEntry();

            // Add manually added metadata.
//	        zipArchiveStream.putNextEntry(new ZipEntry("shark_metadata.txt"));
//            byte[] bytes = Files.readAllBytes(Paths.get(fromPath.concat("\\shark_metadata.txt")));
//            zipArchiveStream.write(bytes, 0, bytes.length);
//            zipArchiveStream.closeEntry();
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(fromPath.concat("\\shark_metadata.txt")));
                // Continue if not NoSuchFileException.
                zipArchiveStream.putNextEntry(new ZipEntry("shark_metadata.txt"));
                zipArchiveStream.write(bytes, 0, bytes.length);
                zipArchiveStream.closeEntry();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Add automatically added metadata.
            zipArchiveStream.putNextEntry(new ZipEntry("shark_metadata_auto.txt"));
            for (String row : metadataVisitor.metadataFileContent) {
                String row_and_cr = row + "\r\n"; // New line (windows style).
                byte[] data = row_and_cr.getBytes();
                zipArchiveStream.write(data, 0, row_and_cr.length());
            }
            zipArchiveStream.closeEntry();

            // Add calculated shark_data.txt file.
            zipArchiveStream.putNextEntry(new ZipEntry("shark_data.txt"));
            for (String row : dataVisitor.dataFileContent) {
                String row_and_cr = row + "\r\n"; // New line (windows style).
                byte[] data = row_and_cr.getBytes();
                zipArchiveStream.write(data, 0, row_and_cr.length());
            }
            zipArchiveStream.closeEntry();

            // Add calculated shark_data_columns.txt file. Will not be added to zip if empty.
            if (dataColumnsVisitor.dataFileContent.size() > 0) {
                zipArchiveStream.putNextEntry(new ZipEntry("shark_data_columns.txt"));
                for (String row : dataColumnsVisitor.dataFileContent) {
                    String row_and_cr = row + "\r\n"; // New line (windows style).
                    byte[] data = row_and_cr.getBytes();
                    zipArchiveStream.write(data, 0, row_and_cr.length());
                }
                zipArchiveStream.closeEntry();
            }



            // Add code translations file.
            zipArchiveStream.putNextEntry(new ZipEntry("translate_codes.txt"));
            for (String row : TranslateCodesManager_NEW.instance().getUsedRows()) {
                String row_and_cr = row + "\r\n"; // New line (windows style).
                byte[] data = row_and_cr.getBytes();
                zipArchiveStream.write(data, 0, row_and_cr.length());
            }
            zipArchiveStream.closeEntry();



            // Add all files and directories in "processed_data". Recursive calls to addDirsAndFilesToZip().
            File[] files = new File(fromPath.concat("\\processed_data")).listFiles();
            String parentDir = "processed_data";
            addDirsAndFilesToZip(files, zipArchiveStream, parentDir);

            // Add all files and directories in "received_data". Recursive calls to addDirsAndFilesToZip().
            if (Files.exists(Paths.get(fromPath.concat("\\received_data")))) {
                files = new File(fromPath.concat("\\received_data")).listFiles();
                parentDir = "received_data";
                addDirsAndFilesToZip(files, zipArchiveStream, parentDir);
            }

            zipArchiveStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getReadme(String datatype, String language) {

        // Path to README template.
        String readmeTemplate = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\readme_files\\" +
                "README_" + datatype + "_" + language + ".txt";
        String readmeContent = "";
        try {
            readmeContent = new String(Files.readAllBytes(Paths.get(readmeTemplate)));
        } catch (IOException e) { }

        if (!readmeContent.equals("")) {
            return readmeContent;
        }

        // Return default texts depending on language.
        if (language.equals("sv")) {
            return  "Denna fil innehåller marin miljöövervakningsdata som levererats till datavärden SMHI." +
                    "\r\n\r\n" +
                    "Information: https://www.smhi.se/klimatdata/oceanografi/havsmiljodata" +
                    "\r\n\r\n" +
                    "Kontakt: shark@smhi.se";
        } else {
            return  "This file contains marine environmental monitoring data delivered to the Swedish National data host for oceanographic and marine biological environmental data." +
                    "\r\n\r\n" +
                    "Info at: https://www.smhi.se/klimatdata/oceanografi/havsmiljodata" +
                    "\r\n\r\n" +
                    "Contact: shark@smhi.se";
        }
    }

    public static void addDirsAndFilesToZip(File[] files, ZipOutputStream zipArchiveStream, String parentDir) {
        for (File file : files) {
            if (file.isDirectory()) {
                addDirsAndFilesToZip(file.listFiles(), zipArchiveStream, parentDir); // Recursive call.
            } else {
                try {
                    zipArchiveStream.putNextEntry(new ZipEntry(parentDir + "\\" + file.getName()));
                    byte[] bytes = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                    zipArchiveStream.write(bytes, 0, bytes.length);
                    zipArchiveStream.closeEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

