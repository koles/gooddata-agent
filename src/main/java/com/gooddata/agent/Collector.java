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
	private final String inputDir, wildcard;

	public Collector(String inputDir, String wildcard) {
		this.inputDir = inputDir;
		this.wildcard = wildcard;
	}

	public File collect() throws IOException {
        FileFilter fileFilter = new WildcardFileFilter(wildcard);
        File[] inputFiles = new File(inputDir).listFiles(fileFilter);
        if (inputFiles.length == 0) {
        	throw new FileNotFoundException(format("No files matching '%s' found", wildcard));
        }
        Map <String, File> inputFilesMap = new HashMap<String, File>();
        for (final File file : inputFiles) {
        	inputFilesMap.put(file.getName(), file);
        }
        File archive = File.createTempFile("gdca-", ".zip");
        archive.deleteOnExit();
        createZipArchive(inputFilesMap, archive.getAbsolutePath());
        return archive;
	}
}
