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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ManifestCollector extends AbstractFileCollector implements Collector {
	private final Date now;
	private final String fileNameTemplate;
	private String mainFile = null;

	public String getMainFile() {
		return mainFile;
	}

	public ManifestCollector(String fileNameTemplate, Date now) {
		this.now = now;
		this.fileNameTemplate = fileNameTemplate;
	}

	@Override
	public Map<File, String> collect() throws IOException {
		File manifest = File.createTempFile("gdca-", ".txt");
        manifest.deleteOnExit();
        BufferedWriter mnfWrtr = new BufferedWriter(new FileWriter(manifest));
        Map<File,String> result = new HashMap<File, String>();
        for (Map.Entry<String, File> e : inputFilesMap.entrySet()) {
        	final String filename = e.getKey() + "." + now.getTime() + ".csv";
        	result.put(e.getValue(), filename);
        	mnfWrtr.write(filename + "\n");
        }
		mnfWrtr.close();
		this.mainFile = Utils.generateRemoteFileName(fileNameTemplate, now);
		result.put(manifest, mainFile);
		return result;
	}
}
