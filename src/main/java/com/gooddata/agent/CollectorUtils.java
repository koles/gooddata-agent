/*
 * Copyright (c) 2014, GoodData Corporation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
 *        the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
 *        or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
