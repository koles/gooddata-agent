package com.gooddata.agent;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;

public class Uploader {
	private final HttpClient client;
	private final String baseUrl;

	public Uploader(final String baseUrl, final String username, final String password) {
		this.client = new HttpClient();
	    Credentials creds = new UsernamePasswordCredentials(username, password);
	    client.getState().setCredentials(AuthScope.ANY, creds);
	    final String slash = baseUrl.endsWith("/") ? "" : "/";
        this.baseUrl = baseUrl + slash;
    }

	public void upload(final Map<File,String> filesToUpload, final String remoteDir) throws IOException {
		for (Map.Entry<File, String> e : filesToUpload.entrySet()) {
			final String uploadedUrl = uploadTemp(e.getKey(), remoteDir, e.getValue());
			move(remoteDir, uploadedUrl, e.getValue());
		}
	}

	public String uploadTemp(final File fileToUpload, final String remoteDir, final String remoteFileName) throws IOException {
		final String tempUrl = toTempPath(baseUrl + remoteFileName);
	    PutMethod method = new PutMethod(tempUrl);
	    RequestEntity requestEntity = new InputStreamRequestEntity(new FileInputStream(fileToUpload));
	    method.setRequestEntity(requestEntity);
	    client.getParams().setAuthenticationPreemptive(true);
	    client.executeMethod(method);
	    if (method.getStatusCode() != HttpStatus.SC_CREATED) {
	    	throw new RuntimeException(format("Upload failed: %s (status code = %d)",
	    			method.getStatusText(), method.getStatusCode()));
	    }
	    return tempUrl;
	}
	
	private void move(final String remoteDir, final String tempUrl, String targetName) throws HttpException, IOException {
		final String targetUrl = baseUrl + targetName;
		System.out.println(tempUrl + " -> " + targetUrl);
		MoveMethod method = new MoveMethod(tempUrl, targetUrl, true);
		client.getParams().setAuthenticationPreemptive(true);
		client.executeMethod(method);
		final int sc = method.getStatusCode();
	    if (sc != HttpStatus.SC_CREATED && sc != HttpStatus.SC_NO_CONTENT) {
	    	throw new RuntimeException(format("Move failed: %s (status code = %d); file uploaded to " + tempUrl,
	    			method.getStatusText(), method.getStatusCode()));
	    }
	}
	
	private String toTempPath(final String path) {
		return path + "." + System.currentTimeMillis();
	}
}
