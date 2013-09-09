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
