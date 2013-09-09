package com.gooddata.agent.jdbc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import au.com.bytecode.opencsv.CSVWriter;

public class JdbcExtractor {
	private final JdbcConnector connector;

	public JdbcExtractor(JdbcConnector connector) {
		this.connector = connector;
	}

	/**
	 * Creates a temp directory and extracts data into CSV files within
	 * @param datasets maps data set names (used to derive file names) to SQL queries
	 * @return temp folder where the extracted files are stored or <tt>null</tt>
	 * 		if nothing was extracted
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public File extract(Map<String,String> datasets) throws SQLException, IOException {
		File tmpdir = createTempDir();
		int extracts = 0;
		Connection conn = connector.connect();
		try {
			for (Map.Entry<String, String> entry : datasets.entrySet()) {
				writeResultSet(conn, entry.getValue(), tmpdir, entry.getKey() + ".csv");
				extracts++;
			}
		} finally {
			conn.close();
		}
		return (extracts == 0) ? null : tmpdir;
	}

	private void writeResultSet(Connection conn, String sql, File tmpdir, String filename) throws SQLException, IOException {
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery(sql);
			try {
				Writer writer = new FileWriter(tmpdir.getAbsolutePath() + File.separator + filename);
				CSVWriter csv = new CSVWriter(writer, ',', '"', '"', "\n");
				try {
					csv.writeAll(rs, true);
				} finally {
					csv.close();
				}
			} finally {
				rs.close();
			}
		} finally {
			stmt.close();
		}
		
	}

	private File createTempDir() {
		try {
			File tmpdir = File.createTempFile("gdca-jdbc-", "");
			tmpdir.delete();
			tmpdir.mkdir();
			return tmpdir;
		} catch (IOException e) {
			throw new RuntimeException("Error creating a temp directory", e);
		}
	}
}
