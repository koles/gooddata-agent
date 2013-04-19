package com.gooddata.agent;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	private Map<String,Exception> errors = new HashMap<String, Exception>();

	private Configuration(Properties props) {}
	
	public static Configuration fromProperties(Properties props) throws InvalidConfigurationException {
		Configuration conf = new Configuration(props);
		// GoodData properties
		conf.setGdcUsername(required(props, "gdc.username", conf.errors));
		conf.setGdcPassword(required(props, "gdc.password", conf.errors));
		try {
			conf.setGdcUploadUrl(required(props, "gdc.upload_url", conf.errors));
		} catch (Exception e) {
			conf.errors.put("gdc.upload_url", e);
		}
		try {
			conf.setGdcEtlUrl(props.getProperty("gdc.etl_url"));
		} catch (Exception e) {
			conf.errors.put("gdc.etl_url", e);
		}
		conf.setGdcUploadArchive(props.getProperty("gdc.upload_archive"));
		// Source files
		conf.setFsInputDir(props.getProperty("filesystem.input_dir"));
		conf.setFsWildcard(props.getProperty("filesystem.wildcard"));
		
		conf.validate();
		return conf;
	}
	
	private void validate() throws InvalidConfigurationException {
		if (!errors.isEmpty()) {
			throw new InvalidConfigurationException(errors);
		}
	}
	
	private static String required(Properties props, String key, Map<String, Exception> errors) {
		String value = props.getProperty(key);
		if (value == null) {
			errors.put(key, new IllegalArgumentException(key + " is a mandatory property"));
		}
		return value;
	}

	// GoodData properties
	private String gdcUsername = null,
				   gdcPassword = null,
				   gdcUploadUrl = null,
				   gdcUploadHost = null,
				   gdcUploadProtocol = null,
				   gdcEtlUrl = null,
				   gdcUploadArchive = null;

	// Source files
	private String fsInputDir = null,
				   fsWildcard = null;

	public String getFsWildcard() {
		return fsWildcard;
	}

	public void setFsWildcard(String fsWildcard) {
		this.fsWildcard = fsWildcard;
	}

	private String gdcUploadPath;

	// ==== Getters and Setters =====

	public String getGdcUsername() {
		return gdcUsername;
	}

	private void setGdcUsername(String gdcUsername) {
		this.gdcUsername = gdcUsername;
	}

	public String getGdcPassword() {
		return gdcPassword;
	}

	private void setGdcPassword(String gdcPassword) {
		this.gdcPassword = gdcPassword;
	}

	public String getGdcUploadUrl() {
		return gdcUploadUrl;
	}

	private void setGdcUploadUrl(String gdcUploadUrl) throws MalformedURLException {
		this.gdcUploadUrl = gdcUploadUrl;
		URL url = new URL(gdcUploadUrl);
		this.gdcUploadHost = url.getHost();
		this.gdcUploadProtocol = url.getProtocol();
		this.gdcUploadPath = url.getPath();
	}

	public String getGdcEtlUrl() {
		return gdcEtlUrl;
	}

	private void setGdcEtlUrl(String gdcEtlUrl) {
		this.gdcEtlUrl = gdcEtlUrl;
	}

	public String getGdcUploadArchive() {
		return gdcUploadArchive;
	}

	private void setGdcUploadArchive(String gdcUploadFile) {
		this.gdcUploadArchive = gdcUploadFile;
	}

	public String getFsInputDir() {
		return fsInputDir;
	}

	private void setFsInputDir(String fsInputDir) {
		this.fsInputDir = fsInputDir;
	}

	public String getGdcUploadProtocol() {
		return gdcUploadProtocol;
	}

	public String getGdcUploadHost() {
		return gdcUploadHost;
	}

	public String getGdcUploadPath() {
		return gdcUploadPath;
	}

}
