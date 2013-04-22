package com.gooddata.agent.jdbc;

import static java.lang.String.format;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcConnector {
	private String jdbcUrl    = null,
				   driverPath = null,
				   driver     = null, // the JDBC driver class
				   username   = null,
				   password   = null;
	private URL driverUrl = null;

	public Connection connect() throws SQLException {
		Driver driver = loadDriver();
		Properties props = new Properties();
		props.setProperty("user", username);
		props.setProperty("password", password);
		Connection conn = driver.connect(jdbcUrl, props);
		return conn;
	}

	private Driver loadDriver() {
		final Class driverClass;
		if (driverUrl != null) {
			if (driverPath == null) { throw new AssertionError("driverUrl set but driverPath not"); }
			try {
			    URLClassLoader urlCl = new URLClassLoader(new URL[] { driverUrl }, System.class.getClassLoader());
			    driverClass = urlCl.loadClass(driver);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(format("Driver %s not found even in %s", driver, driverPath), e);
			}
		} else {
			try {
				driverClass = Class.forName(driver);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(
					format("Driver %s not found, you may need to point us to a specific jar file", driver), e);
			}
		}
		try {
			Driver driver = (Driver)driverClass.newInstance();
			return driver;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public URL getDriverUrl() {
		return driverUrl;
	}

	public void setDriverPath(String driverPath) {
		this.driverPath = driverPath;
		File file = new File(driverPath);
		if (!file.exists()) {
			throw new RuntimeException(format("Cannot read the driver file %s: file does not exist", driverPath));
		}
		if (!file.canRead()) {
			throw new RuntimeException(format("Cannot read the driver file %s: access denied", driverPath));
		}
		try {
			this.driverUrl = file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}
}
