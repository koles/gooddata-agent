package com.gooddata.agent;

import static java.lang.String.format;
import static com.gooddata.agent.CollectorUtils.createZipArchive;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class Collector {
	private final Map <String, File> inputFilesMap = new HashMap<String, File>();
	
	public void add(String inputDir, String wildcard) throws IOException {
		add(new File(inputDir), wildcard);
	}

	public void add(File inputDir, String wildcard) throws IOException {
        FileFilter fileFilter = new WildcardFileFilter(wildcard);
        File[] inputFiles = inputDir.listFiles(fileFilter);
        if (inputFiles == null || inputFiles.length == 0) {
        	throw new FileNotFoundException(
    			format("No files matching '%s' found under '%s'", wildcard, inputDir));
        }
        for (final File file : inputFiles) {
        	inputFilesMap.put(file.getName(), file);
        }		
	}

	public File collect() throws IOException {
        File archive = File.createTempFile("gdca-", ".zip");
        archive.deleteOnExit();
        createZipArchive(inputFilesMap, archive.getAbsolutePath());
        return archive;
	}
}
