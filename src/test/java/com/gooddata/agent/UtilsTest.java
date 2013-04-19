package com.gooddata.agent;

import java.util.Calendar;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
	public void testGenerateRemoteFileName() {
		Calendar c = Calendar.getInstance();
		c.set(2013, 0, 31, 3, 14, 0);
		String template = "date-${yyyyMMddHHmmss}.zip";
		String generated = Utils.generateRemoteFileName(template, c.getTime());
		String expected = "date-20130131031400.zip";
		assertEquals(expected, generated);
	}

	public void testGenerateRemoteFileNameWithDashes() {
		Calendar c = Calendar.getInstance();
		c.set(2013, 0, 31, 3, 14, 0);
		String template = "date-${yyyy-MM-dd-HH-mm-ss}.zip";
		String generated = Utils.generateRemoteFileName(template, c.getTime());
		String expected = "date-2013-01-31-03-14-00.zip";
		assertEquals(expected, generated);
	}
	
	public void testGenerateRemoteFileNameNoSubst() {
		String template = "date-yyyyMMdd.zip";
		String generated = Utils.generateRemoteFileName(template);
		assertEquals(template, generated);
	}

}
