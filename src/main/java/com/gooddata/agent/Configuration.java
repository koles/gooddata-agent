package com.gooddata.agent;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class Configuration {
	/**
	 * The default name of a parameter used to pass the zip file 
	 * with data to be processed.
	 */
	public static final String DEFAULT_PARAM_ZIP = "gdc_agent_zip";

	/**
	 * The default name of a parameter used to pass the manifest text file
	 * that contains the list of files to be processed.
	 */
	public static final String DEFAULT_PARAM_MANIFEST = "gdc_agent_zip";
	
	/**
	 * The default name of a parameter used to pass the timestamp 
	 * formatted as yyyyMMddHHmmss.
	 */
	public static final String DEFAULT_PARAM_NOW = "gdc_agent_now";
	
	/**
	 * The default name of a parameter used to pass a remote directory where
	 * the ETL is expected to put any information about results of the data
	 * processing.
	 * Designed primarily for the purpose of fetching Contract Checker's
	 * reports.
	 */
	public static final String DEFAULT_PARAM_REPORTS = "gdc_agent_reports";

	private Map<String,Exception> errors = new HashMap<String, Exception>();
	private Map<String,String[]> ALL_OR_NONE = new HashMap<String, String[]>(){{
		put("gdc.etl.*", new String[] { "gdc.etl.process_url", "gdc.etl.graph" });
		put("jdbc.*", new String[] { "jdbc.driverPath", "jdbc.driver", "jdbc.username", "jdbc.url" });
	}};
	private String[][] ALTERNATIVES = new String[][] {
		new String[] { "gdc.upload_archive", "gdc.upload_manifest" } 
	};

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
		conf.setGdcEtlParamNameZip(props.getProperty("gdc.etl.param_name.file"));
		conf.setGdcEtlParamNameManifest(props.getProperty("gdc.etl.param_name.manifest"));
		conf.setGdcEtlParamNameNow(props.getProperty("gdc.etl.param_name.now"));
		conf.setGdcEtlParamNameReports(props.getProperty("gdc.etl.param_name.reports"));
		conf.setGdcUploadArchive(props.getProperty("gdc.upload_archive"));
		conf.setGdcUploadManifest(props.getProperty("gdc.upload_manifest"));
		// Source files
		conf.setFsInputDir(props.getProperty("filesystem.input_dir"));
		conf.setFsWildcard(props.getProperty("filesystem.wildcard"));

		// JDBC Data Source
		conf.setJdbcDriver(props.getProperty("jdbc.driver"));
		conf.setJdbcDriverPath(props.getProperty("jdbc.driver_path"));
		conf.setJdbcUsername(props.getProperty("jdbc.username"));
		conf.setJdbcPassword(props.getProperty("jdbc.password"));
		conf.setJdbcUrl(props.getProperty("jdbc.url"));

		// JDBC data sets
		conf.jdbcExtractMappings = buildJdbcExtractMappings(props, "data.", ".sql");
		conf.validate(props);
		return conf;
	}
	
	private void validate(Properties props) throws InvalidConfigurationException {
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
		for (String[] alts : ALTERNATIVES) {
			boolean defined = false;
			inner_loop: for (String a : alts) {
				if (props.getProperty(a) != null) {
					if (defined) {
						errors.put(a, new IllegalArgumentException(
								format("Exactly one of the following properties must be set: %s",
								StringUtils.join(alts, ", "))));
						break inner_loop; // unnecessary, just for clarity
					} else {
						defined = true;
					}
				}
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
				   gdcEtlParamNameZip = null,
				   gdcEtlParamNameManifest = null,
				   gdcEtlParamNameNow = null,
				   gdcEtlParamNameReports = null,
				   gdcUploadArchive = null,
				   gdcUploadManifest = null,
				   jdbcDriverPath = null,
				   jdbcDriver = null,
				   jdbcUsername = null,
				   jdbcPassword = null,
				   jdbcUrl;

	public String getGdcUploadManifest() {
		return gdcUploadManifest;
	}

	public void setGdcUploadManifest(String gdcUploadManifest) {
		this.gdcUploadManifest = gdcUploadManifest;
	}

	private Map<String,String> gdcEtlParams = null,
			                   jdbcExtractMappings = null;

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
		while (propNames.hasMoreElements()) {
			String p = (String)propNames.nextElement();
			if (p.startsWith(paramsPrefix)) {
				String paramName = p.replaceFirst(paramsPrefix, "");
				result.put(paramName, props.getProperty(p));
			}
		}
		return result;
	}

	public void setGdcEtlParams(Map<String, String> gdcEtlParams) {
		this.gdcEtlParams = gdcEtlParams;
	}

	public String getGdcEtlParamNameZip() {
		return gdcEtlParamNameZip;
	}

	public void setGdcEtlParamNameZip(String gdcEtlParamNameZip) {
		this.gdcEtlParamNameZip = (gdcEtlParamNameZip == null) ? DEFAULT_PARAM_ZIP : gdcEtlParamNameZip;
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

	public String getJdbcDriverPath() {
		return jdbcDriverPath;
	}

	public void setJdbcDriverPath(String jdbcDriverPath) {
		this.jdbcDriverPath = jdbcDriverPath;
	}

	public String getJdbcDriver() {
		return jdbcDriver;
	}

	public void setJdbcDriver(String jdbcDriver) {
		this.jdbcDriver = jdbcDriver;
	}

	public String getJdbcUsername() {
		return jdbcUsername;
	}

	public void setJdbcUsername(String jdbcUsername) {
		this.jdbcUsername = jdbcUsername;
	}

	public String getJdbcPassword() {
		return jdbcPassword;
	}

	public void setJdbcPassword(String jdbcPassword) {
		this.jdbcPassword = jdbcPassword;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public Map<String, String> getJdbcExtractMappings() {
		return jdbcExtractMappings;
	}

	private static Map<String, String> buildJdbcExtractMappings(Properties props, String prefix, String suffix) {
		Map<String,String> result = new HashMap<String, String>();
		Enumeration propNames = props.propertyNames();
		while (propNames.hasMoreElements()) {
			String p = (String)propNames.nextElement();
			if (p.startsWith(prefix) && p.endsWith(suffix)) {
				String dataset = p.replaceFirst(prefix, "").replaceAll(suffix + "$", "");
				result.put(dataset, props.getProperty(p));
			}
		}
		return result;
	}

	public String getGdcEtlParamNameManifest() {
		return gdcEtlParamNameManifest;
	}

	public void setGdcEtlParamNameManifest(String gdcEtlParamNameManifest) {
		this.gdcEtlParamNameManifest = gdcEtlParamNameManifest;
	}

	public String getGdcEtlParamNameNow() {
		return gdcEtlParamNameNow;
	}

	public void setGdcEtlParamNameNow(String gdcEtlParamNameNow) {
		this.gdcEtlParamNameNow = gdcEtlParamNameNow;
	}

	public String getGdcEtlParamNameReports() {
		return gdcEtlParamNameReports;
	}

	public void setGdcEtlParamNameReports(String gdcEtlParamNameReports) {
		this.gdcEtlParamNameReports = gdcEtlParamNameReports;
	}

}
