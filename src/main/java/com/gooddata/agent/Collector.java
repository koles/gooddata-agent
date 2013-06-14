package com.gooddata.agent;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface Collector {

	public abstract void add(String inputDir, String wildcard)
			throws IOException;

	public abstract void add(File inputDir, String wildcard) throws IOException;

	/**
	 * @return map of local files to remote file names
	 * @throws IOException
	 */
	public abstract Map<File, String> collect() throws IOException;
	
	public String getMainFile();

}