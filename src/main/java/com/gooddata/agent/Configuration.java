/*
 * Copyright (c) 2014, GoodData Corporation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
 *        the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
 *        or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.gooddata.agent;

import static java.lang.String.format;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class Configuration {
	public static final String JDBC_PASSWORD = "jdbc.password";

   public static final String GDC_PASSWORD = "gdc.password";

   public static final String GDC_USERNAME = "gdc.username";

   public static final String JDBC_DRIVER_PATH = "jdbc.driverPath";

   // for a backward compatibility
   public static final String JDBC_DRIVER_PATH_BC = "jdbc.driver_path";

   public static final String JDBC_DRIVER = "jdbc.driver";

   public static final String JDBC_URL = "jdbc.url";

   public static final String JDBC_USERNAME = "jdbc.username";

   /**
	 * The default name of a parameter used to pass the zip file
	 * with data to be processed.
	 */
	public static final String DEFAULT_PARAM_ZIP = "gdc_agent_zip";

	/**
	 * The default name of a parameter used to pass the manifest text file
	 * that contains the list of files to be processed.
	 */
	public static final String DEFAULT_PARAM_MANIFEST = "gdc_agent_manifest";
	
	/**
	 * The default name of a parameter used to pass the timestamp
	 * formatted as yyyyMMddHHmmss.
	 */
	public static final String DEFAULT_PARAM_NOW = "gdc_agent_now";

	/**
	 * The default name of a parameter used to pass the username of a user
	 * who started the ETL process. This information is not sent if the
	 * gdc.etl.send_credentials property is set to false.
	 */
	public static final String DEFAULT_PARAM_GDC_USERNAME = "gdc_username";

	/**
    * The default name of a parameter used to pass the password of a user
    * who started the ETL process. This information is not sent if the
    * gdc.etl.send_credentials property is set to false.
    */
	public static final String DEFAULT_PARAM_GDC_PASSWORD = "gdc_password";

   /**
    * The default name of a parameter used to pass the password of a user
    * who started the ETL process. This information is not sent if the
    * gdc.etl.send_credentials property is set to false.
    */
	public static final String DEFAULT_PARAM_ZIP_URL = "gdc_agent_zip_url";

	/**
    * The default name of a parameter used to pass the password of a user
    * who started the ETL process. Unlike {@link #DEFAULT_PARAM_ZIP_URL},
    * this information is sent even if the gdc.etl.send_credentials property
    * is set to false.
    */
	public static final String DEFAULT_PARAM_ZIP_URL_NO_CREDENTIALS = "gdc_agent_zip_url_nocreds";

	/**
    * The default name of a parameter used to pass the password of a user
    * who started the ETL process. This information is not sent if the
    * gdc.etl.send_credentials property is set to false.
    */
	public static final String DEFAULT_PARAM_MANIFEST_URL = "gdc_agent_manifest_url";

	/**
    * The default name of a parameter used to pass the password of a user
    * who started the ETL process. Unlike {@link #DEFAULT_PARAM_MANIFEST_URL},
    * this information is sent even if the gdc.etl.send_credentials property
    */
   public static final String DEFAULT_PARAM_MANIFEST_URL_NO_CREDENTIALS = "gdc_agent_manifest_url_nocreds";

	
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
		put("jdbc.*", new String[] { JDBC_DRIVER_PATH, JDBC_DRIVER, JDBC_USERNAME, JDBC_URL });
	}};
	private String[][] ALTERNATIVES = new String[][] {
		new String[] { "gdc.upload_archive", "gdc.upload_manifest" }
	};

	private Configuration(Properties props) {}
	
	public static Configuration fromProperties(Properties props) throws InvalidConfigurationException {
	   return fromProperties(new Properties(), props);
	}
	
	/**
	 * Expected usage:
	 *   * props = command line parameters
	 *   * defaults = config file
	 * @param props
	 * @param defaults
	 * @return
	 * @throws InvalidConfigurationException
	 */
	public static Configuration fromProperties(Properties props, Properties defaults) throws InvalidConfigurationException {
	   InputConfiguration inputConf = new InputConfiguration(props, defaults);
		Configuration conf = new Configuration(props);
		// GoodData properties
		conf.setGdcUsername(required(inputConf, GDC_USERNAME, conf.errors));
		conf.setGdcPassword(required(inputConf, GDC_PASSWORD, conf.errors));
		try {
			conf.setGdcUploadUrl(inputConf.getProperty("gdc.upload_url"));
		} catch (Exception e) {
			conf.errors.put("gdc.upload_url", e);
		}
		try {
			conf.setGdcEtlProcessUrl(inputConf.getProperty("gdc.etl.process_url"));
		} catch (Exception e) {
			conf.errors.put("gdc.etl.process_url", e);
		}
		conf.setGdcEtlGraph(inputConf.getProperty("gdc.etl.graph"));
		conf.setGdcEtlParams(buildParams(inputConf, "gdc.etl.param."));
		conf.setGdcEtlHiddenParams(buildParams(inputConf, "gdc.etl.hidden_param."));
		conf.setGdcEtlParamNameZip(inputConf.getProperty("gdc.etl.param_name.file"));
		conf.setGdcEtlParamNameManifest(inputConf.getProperty("gdc.etl.param_name.manifest"));
		conf.setGdcEtlParamNameNow(inputConf.getProperty("gdc.etl.param_name.now"));
		conf.setGdcEtlParamNameReports(inputConf.getProperty("gdc.etl.param_name.reports"));
		conf.setGdcEtlParamNameGdcUsername(inputConf.getProperty("gdc.etl.param_name.gdc_username"));
		conf.setGdcEtlParamNameGdcPassword(inputConf.getProperty("gdc.etl.param_name.gdc_password"));
		conf.setGdcEtlParamNameZipUrl(inputConf.getProperty("gdc.etl.param_name.url"));
		conf.setGdcEtlParamNameZipUrlNoCreds(inputConf.getProperty("gdc.etl.param_name.url.no_creds"));
		conf.setGdcEtlParamNameManifestUrl(inputConf.getProperty("gdc.etl.param_name.manifest_url"));
		conf.setGdcEtlParamNameManifestUrlNoCreds(inputConf.getProperty("gdc.etl.param_name.manifest_url.no_creds"));
		conf.setGdcUploadArchive(inputConf.getProperty("gdc.upload_archive"));
		conf.setGdcUploadManifest(inputConf.getProperty("gdc.upload_manifest"));
		if ("true".equalsIgnoreCase(inputConf.getProperty("gdc.etl.send_credentials"))) {
		   conf.setSendCredentials(true);
		}
		// Source files
		conf.setFsInputDir(inputConf.getProperty("filesystem.input_dir"));
		conf.setFsWildcard(inputConf.getProperty("filesystem.wildcard"));

		// JDBC Data Source
		conf.setJdbcDriver(inputConf.getProperty(JDBC_DRIVER));
		conf.setJdbcDriverPath(inputConf.getProperty(JDBC_DRIVER_PATH_BC)); // backward compatibility
		if (inputConf.getProperty(JDBC_DRIVER_PATH) != null) {
		   conf.setJdbcDriverPath(inputConf.getProperty(JDBC_DRIVER_PATH));
		}
		conf.setJdbcUsername(inputConf.getProperty(JDBC_USERNAME));
		conf.setJdbcPassword(inputConf.getProperty(JDBC_PASSWORD));
		conf.setJdbcUrl(inputConf.getProperty(JDBC_URL));

		// JDBC data sets
		conf.jdbcExtractMappings = buildJdbcExtractMappings(inputConf, "data.", ".sql");
		conf.validate(inputConf);
		return conf;
	}
	
	private void validate(InputConfiguration props) throws InvalidConfigurationException {
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
	
	private static String required(InputConfiguration props, String key, Map<String, Exception> errors) {
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
				   gdcEtlParamNameGdcUsername = null,
				   gdcEtlParamNameGdcPassword = null,
				   gdcEtlParamNameZipUrl = null,
				   gdcEtlParamNameZipUrlNoCreds = null,
				   gdcEtlParamNameManifestUrl = null,
				   gdcEtlParamNameManifestUrlNoCreds = null,
				   gdcUploadArchive = null,
				   gdcUploadManifest = null,
				   gdcEtlSendCredentials = null,
				   jdbcDriverPath = null,
				   jdbcDriver = null,
				   jdbcUsername = null,
				   jdbcPassword = null,
				   jdbcUrl;
	private boolean sendCredentials = false;

	public String getGdcUploadManifest() {
		return gdcUploadManifest;
	}

	public void setGdcUploadManifest(String gdcUploadManifest) {
		this.gdcUploadManifest = gdcUploadManifest;
	}

	private Map<String,String> gdcEtlParams = null,
	                         gdcEtlHiddenParams = null,
			                   jdbcExtractMappings = null;

	public Map<String, String> getGdcEtlHiddenParams() {
      return gdcEtlHiddenParams;
   }

   public void setGdcEtlHiddenParams(Map<String, String> gdcEtlHiddenParams) {
      this.gdcEtlHiddenParams = gdcEtlHiddenParams;
   }

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
	   if (gdcUploadUrl != null) {
   		this.gdcUploadUrl = gdcUploadUrl;
   		try {
      		URL url = new URL(gdcUploadUrl);
      		this.gdcUploadHost = url.getHost();
      		this.gdcUploadProtocol = url.getProtocol();
      		this.gdcUploadPath = url.getPath();
   		} catch (MalformedURLException e) {
   		   throw new RuntimeException("The upload URL " + gdcUploadUrl + " is invalid");
   		}
	   }
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
			try {
   			URL url = new URL(gdcEtlProcessUrl);
   			this.gdcApiHost = url.getHost();
   			this.gdcApiProtocol = url.getProtocol();
   			this.gdcEtlProcessPath = url.getPath();
			} catch (MalformedURLException e) {
			   throw new RuntimeException("The ETL process URL " + gdcEtlProcessUrl + " is invalid");
			}
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

	private static Map<String,String> buildParams(InputConfiguration props, String paramsPrefix) {
		Map<String,String> result = new HashMap<String, String>();
		Set<String> propNames = props.propertyNames();
		for (final String p : propNames) {
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

	private static Map<String, String> buildJdbcExtractMappings(InputConfiguration props, String prefix, String suffix) {
		Map<String,String> result = new HashMap<String, String>();
		Set<String> propNames = props.propertyNames();
		for (final String p : propNames) {
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
		this.gdcEtlParamNameManifest = (gdcEtlParamNameManifest == null) ? DEFAULT_PARAM_MANIFEST : gdcEtlParamNameManifest;
	}

	public String getGdcEtlParamNameNow() {
		return gdcEtlParamNameNow;
	}

	public void setGdcEtlParamNameNow(String gdcEtlParamNameNow) {
		this.gdcEtlParamNameNow = (gdcEtlParamNameNow == null) ? DEFAULT_PARAM_NOW : gdcEtlParamNameNow;
	}

	public String getGdcEtlParamNameReports() {
		return gdcEtlParamNameReports;
	}

	public void setGdcEtlParamNameReports(String gdcEtlParamNameReports) {
		this.gdcEtlParamNameReports = (gdcEtlParamNameReports == null) ? DEFAULT_PARAM_REPORTS : null; // FIXME
	}
	
	public String getGdcEtlParamNameGdcUsername() {
      return gdcEtlParamNameGdcUsername;
   }

   public void setGdcEtlParamNameGdcUsername(String gdcEtlParamNameGdcUsername) {
      this.gdcEtlParamNameGdcUsername = (gdcEtlParamNameGdcUsername == null) ? DEFAULT_PARAM_GDC_USERNAME : gdcEtlParamNameGdcUsername;
   }

   public String getGdcEtlParamNameGdcPassword() {
      return gdcEtlParamNameGdcPassword;
   }

   public void setGdcEtlParamNameGdcPassword(String gdcEtlParamNameGdcPassword) {
      this.gdcEtlParamNameGdcPassword = (gdcEtlParamNameGdcPassword == null) ? DEFAULT_PARAM_GDC_PASSWORD : gdcEtlParamNameGdcPassword;
   }

   public String getGdcEtlParamNameZipUrl() {
      return gdcEtlParamNameZipUrl;
   }

   public void setGdcEtlParamNameZipUrl(String gdcEtcParamNameZipUrl) {
      this.gdcEtlParamNameZipUrl = (gdcEtcParamNameZipUrl == null) ? DEFAULT_PARAM_ZIP_URL : gdcEtcParamNameZipUrl;
   }

   public String getGdcEtlParamNameZipUrlNoCreds() {
      return gdcEtlParamNameZipUrlNoCreds;
   }

   public void setGdcEtlParamNameZipUrlNoCreds(String gdcEtcParamNameZipUrlNoCreds) {
      this.gdcEtlParamNameZipUrlNoCreds = (gdcEtcParamNameZipUrlNoCreds == null) ? DEFAULT_PARAM_ZIP_URL_NO_CREDENTIALS : gdcEtcParamNameZipUrlNoCreds;
   }

   public String getGdcEtlParamNameManifestUrl() {
      return gdcEtlParamNameManifestUrl;
   }

   public void setGdcEtlParamNameManifestUrl(String gdcEtcParamNameManifestUrl) {
      this.gdcEtlParamNameManifestUrl = (gdcEtcParamNameManifestUrl == null) ? DEFAULT_PARAM_MANIFEST_URL : gdcEtcParamNameManifestUrl;
   }

   public String getGdcEtlParamNameManifestUrlNoCreds() {
      return gdcEtlParamNameManifestUrlNoCreds;
   }

   public void setGdcEtlParamNameManifestUrlNoCreds(
         String gdcEtcParamNameManifestUrlNoCreds) {
      this.gdcEtlParamNameManifestUrlNoCreds = (gdcEtcParamNameManifestUrlNoCreds == null) ? DEFAULT_PARAM_MANIFEST_URL_NO_CREDENTIALS : gdcEtcParamNameManifestUrlNoCreds;
   }

   public boolean isSendCredentials() {
      return sendCredentials;
   }

   public void setSendCredentials(boolean sendCredentials) {
      this.sendCredentials = sendCredentials;
   }

   public static class InputConfiguration {
	   Properties props;
	   Properties defaults;
	   InputConfiguration(Properties props, Properties defaults) {
	      this.props = props;
	      this.defaults = defaults;
	   }

	   String getProperty(String key) {
	      String dashedKey = key.replaceAll("\\.", "-");
	      String result = props.getProperty(dashedKey);
	      if (result == null) {
	         result = props.getProperty(key);
   	      if (result == null) {
   	         result = defaults.getProperty(key);
   	      }
	      }
	      return result;
	   }

	   Set<String> propertyNames() {
	      Set<String> result = new HashSet<String>();
	      result.addAll((List<String>)Collections.list(defaults.propertyNames()));
	      result.addAll((List<String>)Collections.list(props.propertyNames()));
	      return result;
	   }
	}
}
