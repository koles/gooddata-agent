package com.gooddata.agent;

import static java.lang.String.format;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.filefilter.WildcardFileFilter;

public class AbstractFileCollector {
	protected final Map <String, File> inputFilesMap = new HashMap<String, File>();
	
	/* (non-Javadoc)
	 * @see com.gooddata.agent.Collector#add(java.lang.String, java.lang.String)
	 */
	public void add(String inputDir, String wildcard) throws IOException {
		add(new File(inputDir), wildcard);
	}

	/* (non-Javadoc)
	 * @see com.gooddata.agent.Collector#add(java.io.File, java.lang.String)
	 */
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
}
