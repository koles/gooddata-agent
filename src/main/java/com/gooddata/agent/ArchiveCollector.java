package com.gooddata.agent;

import static com.gooddata.agent.CollectorUtils.createZipArchive;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ArchiveCollector extends AbstractFileCollector implements Collector {
	
	private final Date now;
	private final String fileNameTemplate;
	private String mainFile = null;

	public String getMainFile() {
		return mainFile;
	}

	public ArchiveCollector(String fileNameTemplate, Date now) {
		this.now = now;
		this.fileNameTemplate = fileNameTemplate;
	}
	
	/* (non-Javadoc)
	 * @see com.gooddata.agent.Collector#collect()
	 */
	@Override
	public Map<File,String> collect() throws IOException {
        final File archive = File.createTempFile("gdca-", ".zip");
        archive.deleteOnExit();
        createZipArchive(inputFilesMap, archive.getAbsolutePath());
        this.mainFile = Utils.generateRemoteFileName(fileNameTemplate, now);
        Map<File,String> result = new HashMap<File, String>();
        result.put(archive, mainFile);
        return result;
	}
}
