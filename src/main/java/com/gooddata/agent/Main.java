package com.gooddata.agent;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import com.gooddata.agent.api.GdcRESTApiWrapper;
import com.gooddata.agent.api.GdcRESTApiWrapper.GraphExecutionResult;
import com.gooddata.agent.api.NamePasswordConfiguration;
import com.gooddata.agent.jdbc.JdbcConnector;
import com.gooddata.agent.jdbc.JdbcExtractor;

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
		Date now = new Date();
        Uploader u = new Uploader(conf.getGdcUploadUrl(), conf.getGdcUsername(), conf.getGdcPassword());
        Collector collector = (conf.getGdcUploadArchive() != null)
        		? new ArchiveCollector(conf.getGdcUploadArchive(), now)
        		: new ManifestCollector(conf.getGdcUploadManifest(), now);
        jdbcExtract(collector);
        fsExtract(collector);

        Map<File,String >toUpload = null;
		try {
			toUpload = collector.collect();
		} catch (IOException e) {
			error("Error collection files: " + e.getMessage());
		}
        try {
			u.upload(toUpload, conf.getGdcUploadPath());
			ok(format("File(s) uploaded under %s", conf.getGdcUploadUrl()));
		} catch (IOException e) {
			error("Error uploading to WebDAV: " + e.getMessage());
		}
        if (conf.getGdcEtlProcessUrl() != null) {
	        GdcRESTApiWrapper client = new GdcRESTApiWrapper(buildNamePasswordConfiguration(conf));
	        Map<String,String> params = conf.getGdcEtlParams();
	        if (conf.getGdcEtlParamNameZip() != null) {
	        	params.put(conf.getGdcEtlParamNameZip(), collector.getMainFile());
	        }
	        if (conf.getGdcEtlParamNameManifest() != null) {
	        	params.put(conf.getGdcEtlParamNameManifest(), collector.getMainFile());
	        }
	        client.login();
	        GraphExecutionResult ger = client.executeGraph(conf.getGdcEtlProcessUrl(), conf.getGdcEtlGraph(), params);
	        ok(format("Graph %s under %s executed, log file at %s", conf.getGdcEtlProcessPath(), conf.getGdcEtlGraph(), ger.getLogUrl()));
        } else {
        	ok("ETL not set up, skipping");
        }
	}
	
	private void jdbcExtract(Collector collector) {
        if (conf.getJdbcUrl() != null) {
        	JdbcConnector connector = new JdbcConnector();
        	connector.setDriver(conf.getJdbcDriver());
        	connector.setDriverPath(conf.getJdbcDriverPath());
        	connector.setJdbcUrl(conf.getJdbcUrl());
        	connector.setUsername(conf.getJdbcUsername());
        	connector.setPassword(conf.getJdbcPassword());
        	JdbcExtractor extractor= new JdbcExtractor(connector);
        	try {
				File jdbcExtractsDir = extractor.extract(conf.getJdbcExtractMappings());
				if (jdbcExtractsDir != null) { // there are some database extracts
					try {
						collector.add(jdbcExtractsDir, "*");
					} catch (IOException e) {
						error("Cannot read database extracts from temporary directory %s",
								jdbcExtractsDir.getAbsolutePath());
					}
				}
			} catch (IOException e) {
				error("Error extracting data from database: %s", e.getMessage());
			} catch (SQLException e) {
				error("Error extracting data from database: %s", e.getMessage());
			}
        } else {
        	ok("JDBC data source not configured, skipping");
        }
	}

	public void fsExtract(Collector collector) {
        if (conf.getFsInputDir() != null) {
        	try {
        		collector.add(conf.getFsInputDir(), conf.getFsWildcard());
        	} catch (IOException e) {
        		error("Error reading from %s", conf.getFsInputDir());
        	}
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
