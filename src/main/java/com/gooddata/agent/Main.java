package com.gooddata.agent;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import com.gooddata.agent.api.GdcRESTApiWrapper;
import com.gooddata.agent.api.NamePasswordConfiguration;
import com.gooddata.agent.api.GdcRESTApiWrapper.GraphExecutionResult;

/**
 * Hello world!
 *
 */
public class Main 
{
	private Configuration conf;

	private Main(String[] args) {
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
		try {
			conf = Configuration.fromProperties(props);
		} catch (InvalidConfigurationException e) {
			errors(e.getErrors().values());
		}
	}

	private void run() {
        Uploader u = new Uploader(conf.getGdcUploadUrl(), conf.getGdcUsername(), conf.getGdcPassword());
        Collector c = new Collector(conf.getFsInputDir(), conf.getFsWildcard());
    	String remoteFileName = Utils.generateRemoteFileName(conf.getGdcUploadArchive());
        File archive = null;
		try {
			archive = c.collect();
		} catch (IOException e) {
			error("Error collection files: " + e.getMessage());
		}
        try {
			u.upload(archive, conf.getGdcUploadPath(), remoteFileName);
			ok(format("%s uploaded under %s", remoteFileName, conf.getGdcUploadUrl()));
		} catch (IOException e) {
			error("Error uploading to WebDAV: " + e.getMessage());
		}
        if (conf.getGdcEtlProcessUrl() != null) {
	        GdcRESTApiWrapper client = new GdcRESTApiWrapper(buildNamePasswordConfiguration(conf));
	        Map<String,String> params = conf.getGdcEtlParams();
	        params.put(conf.getGdcEtlParamNameFile(), remoteFileName);
	        client.login();
	        GraphExecutionResult ger = client.executeGraph(conf.getGdcEtlProcessUrl(), conf.getGdcEtlGraph(), params);
	        ok(format("Graph %s under %s executed, log file at %s", conf.getGdcEtlProcessPath(), conf.getGdcEtlGraph(), ger.getLogUrl()));
        } else {
        	ok("ETL not set up, skipping");
        }
	}

    public static void main(String[] args) {
    	Main m = new Main(args);
    	m.run();
    }

    private static NamePasswordConfiguration buildNamePasswordConfiguration(Configuration conf) {
    	return new NamePasswordConfiguration(
    			conf.getGdcApiProtocol(),
    			conf.getGdcApiHost(),
    			conf.getGdcUsername(),
    			conf.getGdcPassword());
    }

    private static void ok(String msg) {
    	System.out.println("OK: " + msg);
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
