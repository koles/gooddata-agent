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
