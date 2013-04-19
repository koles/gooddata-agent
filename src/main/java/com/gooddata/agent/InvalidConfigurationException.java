package com.gooddata.agent;

import java.util.Map;

public class InvalidConfigurationException extends Exception {
	private static final long serialVersionUID = 2509179198811923659L;

	private final Map<String, Exception> errors;

	public InvalidConfigurationException(Map<String, Exception> errors) {
		this.errors = errors;
	}

	public Map<String, Exception> getErrors() {
		return errors;
	}
}
