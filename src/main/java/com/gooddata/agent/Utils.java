package com.gooddata.agent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	static String generateRemoteFileName(String gdcUploadFile) {
		return generateRemoteFileName(gdcUploadFile, new Date());
	}

	static String generateRemoteFileName(String gdcUploadFile, Date date) {
		if (gdcUploadFile != null) {
			Pattern p = Pattern.compile("\\$\\{([^\\}]*)\\}");
			Matcher m = p.matcher(gdcUploadFile);
			if (m.find()) {
				return m.replaceAll(new SimpleDateFormat(m.group(1)).format(date));
			}
		}
		return gdcUploadFile;
	}
}
