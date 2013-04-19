package com.gooddata.agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

/**
 * Hello world!
 *
 */
public class Main 
{
    public static void main(String[] args)
    {
        if (args.length != 1) {
        	error("Usage: java %s file.properties", Main.class.getName());
        }
        String propsFile = args[0];
        Properties props = new Properties();
        try {
			props.load(new FileInputStream(propsFile));
		} catch (FileNotFoundException e) {
			error("File '%s' does not exist", propsFile);
		} catch (IOException e) {
			error("Error reading file '%s'", propsFile);
		}
        Configuration conf = null;
		try {
			conf = Configuration.fromProperties(props);
		} catch (InvalidConfigurationException e) {
			errors(e.getErrors().values());
		}
        Uploader u = new Uploader(conf.getGdcUploadUrl(), conf.getGdcUsername(), conf.getGdcPassword());
        Collector c = new Collector(conf.getFsInputDir(), conf.getFsWildcard());
        File archive = null;
		try {
			archive = c.collect();
		} catch (IOException e) {
			error("Error collection files: " + e.getMessage());
		}
        try {
			u.upload(archive, conf.getGdcUploadPath(), Utils.generateRemoteFileName(conf.getGdcUploadArchive()));
		} catch (IOException e) {
			error("Error uploading to WebDAV: " + e.getMessage());
		}
    }

    private static void error(String msg, String ... params) {
    	System.err.printf(msg, (Object[])params);
    	System.err.println();
    	System.exit(1);
    }

    private static void errors(Collection<Exception> errors) {
    	for (Exception e : errors) {
    		System.err.println(e.getMessage());
    	}
    	System.exit(1);
    }
}
