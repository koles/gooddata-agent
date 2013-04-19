package com.gooddata.agent;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;

public class Uploader {
	private final HttpClient client;
	private final String baseUrl;

	public Uploader(final String baseUrl, final String username, final String password) {
		this.client = new HttpClient();
	    Credentials creds = new UsernamePasswordCredentials(username, password);
	    client.getState().setCredentials(AuthScope.ANY, creds);
        this.baseUrl = baseUrl;
    }

	public void upload(final File fileToUpload, final String remoteDir, final String remoteFileName) throws IOException {
		final String slash = baseUrl.endsWith("/") ? "" : "/";
	    PutMethod method = new PutMethod(baseUrl + slash + remoteFileName);
	    RequestEntity requestEntity = new InputStreamRequestEntity(new FileInputStream(fileToUpload));
	    method.setRequestEntity(requestEntity);
	    client.getParams().setAuthenticationPreemptive(true);
	    client.executeMethod(method);
	    if (method.getStatusCode() != HttpStatus.SC_CREATED) {
	    	throw new RuntimeException(format("Upload failed: %s (status code = %d)",
	    			method.getStatusText(), method.getStatusCode()));
	    }
	}
}
