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

import static com.gooddata.agent.Configuration.GDC_PASSWORD;
import static com.gooddata.agent.Configuration.GDC_USERNAME;
import static com.gooddata.agent.Configuration.JDBC_DRIVER;
import static com.gooddata.agent.Configuration.JDBC_DRIVER_PATH;
import static com.gooddata.agent.Configuration.JDBC_PASSWORD;
import static com.gooddata.agent.Configuration.JDBC_URL;
import static com.gooddata.agent.Configuration.JDBC_USERNAME;
import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apache.log4j.PropertyConfigurator;

import com.gooddata.agent.api.GdcRESTApiWrapper;
import com.gooddata.agent.api.GdcRESTApiWrapper.GdcUser;
import com.gooddata.agent.api.GdcRESTApiWrapper.GraphExecutionResult;
import com.gooddata.agent.api.NamePasswordConfiguration;
import com.gooddata.agent.jdbc.JdbcConnector;
import com.gooddata.agent.jdbc.JdbcExtractor;

/**
 * Hello world!
 *
 */
public class Main {
   public static final String LOG4J_FILENAME = "log4j.properties";
   private static final String CONFIG_FILE = ".CONFIG.FILE";
   private Configuration conf;

   private Main(String[] args) {
      OptionParser parser = new OptionParser();
      String [] paramNames = { JDBC_DRIVER, JDBC_DRIVER_PATH,
            JDBC_PASSWORD, JDBC_URL, JDBC_USERNAME,
            GDC_PASSWORD, GDC_USERNAME };
      Properties props = loadCommandLineParameters(parser, paramNames, args);

      String propsFile = props.getProperty(CONFIG_FILE);
      if (propsFile == null) {
         error("Usage: java  %s [options] file.properties", Main.class.getName());
      }
      Properties defaults = loadConfigurationFile(propsFile);

      try {
         conf = Configuration.fromProperties(props, defaults);
      } catch (InvalidConfigurationException e) {
         errors(e.getErrors().values());
      }
   }

   private static Properties loadConfigurationFile(String propsFile) {
      Properties props = new Properties();
      try {
         props.load(new FileInputStream(propsFile));
      } catch (FileNotFoundException e) {
         error("File '%s' does not exist", propsFile);
      } catch (IOException e) {
         error("Error reading file '%s'", propsFile);
      }
      return props;
   }

   private static Properties loadCommandLineParameters(OptionParser parser, String[] paramNames, String[] args) {
      for (String p : paramNames) {
         parser.accepts(p.replaceAll("\\.", "-")).withRequiredArg();
      }
      OptionSet options = parser.parse(args);
      Properties props = new Properties();
      for (String p : paramNames) {
         String dashedParam = p.replaceAll("\\.", "-");
         String value = (String)options.valueOf(dashedParam);
         if (value != null) {
            props.setProperty(p, value);
         }
      }
      if (!options.nonOptionArguments().isEmpty()) {
         props.setProperty(CONFIG_FILE, (String)options.nonOptionArguments().get(0));
      }
      return props;
   }

   private void run() {
      Date now = new Date();
      Collector collector = null;
      if (conf.getGdcUploadUrl() == null) {
         ok("Upload URL is not set up, skipping");
      } else {
         Uploader u = new Uploader(conf.getGdcUploadUrl(),
               conf.getGdcUsername(), conf.getGdcPassword());
         collector = (conf.getGdcUploadArchive() != null) ? new ArchiveCollector(
               conf.getGdcUploadArchive(), now) : new ManifestCollector(
               conf.getGdcUploadManifest(), now);
         jdbcExtract(collector);
         fsExtract(collector);

         Map<File, String> toUpload = null;
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
      }
      if (conf.getGdcEtlProcessUrl() != null) {
         GdcRESTApiWrapper client = new GdcRESTApiWrapper(
               buildNamePasswordConfiguration(conf));
         EtlParams etlParams = createEtlParameters(conf, collector);
         client.login();
         GraphExecutionResult ger = client.executeGraph(
               conf.getGdcEtlProcessUrl(), conf.getGdcEtlGraph(), etlParams.params, etlParams.hiddenParams);
         ok(format("Graph %s under %s executed, log file at %s",
               conf.getGdcEtlProcessPath(), conf.getGdcEtlGraph(),
               ger.getLogUrl()));
      } else {
         ok("ETL not set up, skipping");
      }
   }

   private static EtlParams createEtlParameters(Configuration conf, Collector collector) {
      final Map<String,String> params = conf.getGdcEtlParams();
      final Map<String,String> hiddenParams = new HashMap<String,String>();
      final String gdcUserNameUrlenc, gdcPasswordUrlenc;
      try {
         gdcUserNameUrlenc = URLEncoder.encode(conf.getGdcUsername(), "utf-8");
         gdcPasswordUrlenc = URLEncoder.encode(conf.getGdcPassword(), "utf-8");
      } catch (UnsupportedEncodingException e) {
         throw new AssertionError(e);
      }
      if (conf.getGdcUploadUrl() != null) {
         if (conf.getGdcUploadArchive() != null) {
            params.put(conf.getGdcEtlParamNameZip(), collector.getMainFile());
            params.put(conf.getGdcEtlParamNameZipUrlNoCreds(), mainFileUrlNoCreds(conf, collector));
            if (conf.isSendCredentials()) {
               params.put(conf.getGdcEtlParamNameZipUrl(), mainFileUrl(conf, collector, gdcUserNameUrlenc, gdcPasswordUrlenc));
            }
         }
         if (conf.getGdcUploadManifest() != null) {
            params.put(conf.getGdcEtlParamNameManifest(),
                  collector.getMainFile());
            params.put(conf.getGdcEtlParamNameManifestUrlNoCreds(),
                  format("%s/%s", conf.getGdcUploadManifest().replaceAll("/$", ""), collector.getMainFile()));
            if (conf.isSendCredentials()) {
               params.put(conf.getGdcEtlParamNameManifestUrl(), mainFileUrl(conf, collector, gdcUserNameUrlenc, gdcPasswordUrlenc));
            }
         }
      }
      if (conf.isSendCredentials()) {
         params.put(conf.getGdcEtlParamNameGdcUsername(), conf.getGdcUsername());
         hiddenParams.put(conf.getGdcEtlParamNameGdcPassword(), conf.getGdcPassword());
      }
      return new EtlParams(params, hiddenParams);
   }

   private static String mainFileUrl(Configuration conf, Collector collector, String gdcUserNameUrlenc, String gdcPasswordUrlenc) {
      return format("%s://%s:%s@%s%s/%s", conf.getGdcUploadProtocol(), gdcUserNameUrlenc, gdcPasswordUrlenc,
            conf.getGdcUploadHost(), conf.getGdcUploadPath(), collector.getMainFile());
   }

   private static String mainFileUrlNoCreds(Configuration conf, Collector collector) {
      return format("%s/%s", conf.getGdcUploadUrl().replaceAll("/$", ""), collector.getMainFile());
   }

   private void jdbcExtract(Collector collector) {
      if (conf.getJdbcUrl() != null) {
         JdbcConnector connector = new JdbcConnector();
         connector.setDriver(conf.getJdbcDriver());
         if (conf.getJdbcDriverPath() != null) {
            // not mandatory - a driver may already be on the classpath
            connector.setDriverPath(conf.getJdbcDriverPath());
         }
         connector.setJdbcUrl(conf.getJdbcUrl());
         connector.setUsername(conf.getJdbcUsername());
         connector.setPassword(conf.getJdbcPassword());
         JdbcExtractor extractor = new JdbcExtractor(connector);
         try {
            File jdbcExtractsDir = extractor.extract(conf
                  .getJdbcExtractMappings());
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
      if (new File(LOG4J_FILENAME).exists()) {
         PropertyConfigurator.configure(LOG4J_FILENAME);
      }
      Main m = new Main(args);
      m.run();
   }

   private static NamePasswordConfiguration buildNamePasswordConfiguration(
         Configuration conf) {
      return new NamePasswordConfiguration(conf.getGdcApiProtocol(),
            conf.getGdcApiHost(), conf.getGdcUsername(), conf.getGdcPassword());
   }

   private static void ok(String msg) {
      System.out.println("OK: " + msg);
   }

   private static void error(String msg, String... params) {
      System.err.printf(msg, (Object[]) params);
      System.err.println();
      System.exit(1);
   }

   private static void errors(Collection<Exception> errors) {
      for (Exception e : errors) {
         System.err.println(e.getMessage());
      }
      System.exit(1);
   }

   private static class EtlParams {
      final Map<String,String> params;
      final Map<String,String> hiddenParams;
      public EtlParams(Map<String,String> params, Map<String,String> hiddenParams) {
         this.params = params;
         this.hiddenParams = hiddenParams;
      }
   }
}
