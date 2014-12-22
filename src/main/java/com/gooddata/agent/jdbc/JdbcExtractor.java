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
