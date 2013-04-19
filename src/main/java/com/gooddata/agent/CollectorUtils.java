package com.gooddata.agent;

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

public class CollectorUtils {

    /**
     * Creates new zipFile which will contain all files listed in attribute _csvFiles_ and dataSetManifest.
     * 
     * Mostly stolen from {@link com.gooddata.restapi.upload.impl.AbstractUploader}
     *
     * @param csvFiles map of files to be included in ZIP file. Key=file-name in ZIP file, value=path to that file on filesystem.
     * @param zipArchivePathName full-path & name of the new zip file
     */
    protected static void createZipArchive(final Map<String, File> csvFiles, final String zipArchivePathName) {
        ZipOutputStream out = null;

        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipArchivePathName)));

            for (Map.Entry<String, File> entry : csvFiles.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                copyFileToZip(entry.getValue(), out);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(format("The zip archive file %s cannot be created!", zipArchivePathName), e);
        } catch (IOException e) {
            throw new RuntimeException("IO problem occured during zipping the archive!", e);
        } finally {
        	if (out != null) {
        		try {
        			out.close();
        		} catch (IOException e) {
        			throw new RuntimeException(format("Error closing the zip archive file %s", zipArchivePathName), e);
        		}
        	}
            // IOUtils.closeQuietly(out);
        }
    }
    
    /**
     * Stolen from {@link com.gooddata.restapi.upload.impl.AbstractUploader}
     */
    private static void copyFileToZip(File csvFile, ZipOutputStream out) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(csvFile);
            IOUtils.copy(is, out);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
