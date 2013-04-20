package com.gooddata.agent;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	public static final String DEFAULT_FILE_PARAM = "file";

	private Map<String,Exception> errors = new HashMap<String, Exception>();
	private Map<String,String[]> ALL_OR_NONE = new HashMap<String, String[]>(){{
		put("gdc.etl.*", new String[] { "gdc.etl.process_url", "gdc.etl.graph" });
	}};

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
			conf.setGdcEtlProcessUrl(props.getProperty("gdc.etl.process_url"));
		} catch (Exception e) {
			conf.errors.put("gdc.etl.process_url", e);
		}
		conf.setGdcEtlGraph(props.getProperty("gdc.etl.graph"));
		conf.setGdcEtlParams(buildParams(props, "gdc.etl.param."));
		conf.setGdcEtlParamNameFile(props.getProperty("gdc.etl.param_name.file"));
		conf.setGdcUploadArchive(props.getProperty("gdc.upload_archive"));
		// Source files
		conf.setFsInputDir(props.getProperty("filesystem.input_dir"));
		conf.setFsWildcard(props.getProperty("filesystem.wildcard"));
		
		conf.validate();
		return conf;
	}
	
	private void validate() throws InvalidConfigurationException {
		for (Map.Entry<String, String[]> aon : ALL_OR_NONE.entrySet()) {
			int itemsSet = 0;
			for (String i : aon.getValue()) {
				itemsSet++;
			}
			if (itemsSet > 0 && itemsSet < aon.getValue().length) {
				errors.put(
					aon.getKey(),
					new IllegalArgumentException(format("None or all of %s properties should be set", aon.getKey())));
			}
		}
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
				   gdcUploadPath = null,
				   gdcApiHost = null,
				   gdcApiProtocol = null,
				   gdcEtlProcessPath = null,
				   gdcEtlProcessUrl = null,
				   gdcEtlGraph = null,
				   gdcEtlParamNameFile = null,
				   gdcUploadArchive = null;

	private Map<String,String> gdcEtlParams = new HashMap<String,String>();

	// Source files
	private String fsInputDir = null,
				   fsWildcard = null;

	public String getFsWildcard() {
		return fsWildcard;
	}

	public void setFsWildcard(String fsWildcard) {
		this.fsWildcard = fsWildcard;
	}

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

	public String getGdcEtlProcessUrl() {
		return gdcEtlProcessUrl;
	}

	public void setGdcEtlProcessUrl(String gdcEtlProcessUrl) throws MalformedURLException {
		if (gdcEtlProcessUrl != null) {
			this.gdcEtlProcessUrl = gdcEtlProcessUrl;
			URL url = new URL(gdcEtlProcessUrl);
			this.gdcApiHost = url.getHost();
			this.gdcApiProtocol = url.getProtocol();
			this.gdcEtlProcessPath = url.getPath();
		}
	}

	public String getGdcEtlGraph() {
		return gdcEtlGraph;
	}

	public void setGdcEtlGraph(String gdcEtlGraph) {
		this.gdcEtlGraph = gdcEtlGraph;
	}

	public Map<String, String> getGdcEtlParams() {
		return gdcEtlParams;
	}

	private static Map<String,String> buildParams(Properties props, String paramsPrefix) {
		Map<String,String> result = new HashMap<String, String>();
		Enumeration propNames = props.propertyNames();
		if (propNames.hasMoreElements()) {
			for (String p = (String)propNames.nextElement(); propNames.hasMoreElements(); p = (String)propNames.nextElement()) {
				if (p.startsWith(paramsPrefix)) {
					String paramName = p.replaceFirst(paramsPrefix, "");
					result.put(paramName, props.getProperty(p));
				}
			}
		}
		return result;
	}

	public void setGdcEtlParams(Map<String, String> gdcEtlParams) {
		this.gdcEtlParams = gdcEtlParams;
	}

	public String getGdcEtlParamNameFile() {
		return gdcEtlParamNameFile;
	}

	public void setGdcEtlParamNameFile(String gdcEtlParamNameFile) {
		this.gdcEtlParamNameFile = (gdcEtlParamNameFile == null) ? DEFAULT_FILE_PARAM : gdcEtlParamNameFile;
	}

	public String getGdcApiHost() {
		return gdcApiHost;
	}

	public String getGdcApiProtocol() {
		return gdcApiProtocol;
	}

	public String getGdcEtlProcessPath() {
		return gdcEtlProcessPath;
	}

}
