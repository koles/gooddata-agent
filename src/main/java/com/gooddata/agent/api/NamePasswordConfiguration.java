/*
 * Copyright (c) 2009, GoodData Corporation. All rights reserved.
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

package com.gooddata.agent.api;

import java.net.MalformedURLException;
import java.net.URL;

import com.gooddata.agent.util.Constants;

/**
 * Credentials configuration for the GoodData REST and FTP APIs
 *
 * @author Jiri Zaloudek
 * @version 1.0
 */
public class NamePasswordConfiguration {

    /**
     * default GDC host
     */
    public static final String DEFAULT_GDC_HOST = Constants.DEFAULT_HOST;

    // GDC protocol
    private String protocol;
    // GDC host
    private String gdcHost;
    // GDC username
    private String username;
    // GDC password
    private String password;
    // GDC port
    private int port = 0;

    /**
     * Constructor
     *
     * @param protocol GoodData protocol (HTTP | FTP)
     * @param gdcHost  GoodData host (e.g. secure.gooddata.com)
     * @param username GoodData username
     * @param password GoodData password
     * @param port GoodData port
     */
    public NamePasswordConfiguration(String protocol, String gdcHost, String username, String password, int port) {
        super();
        this.protocol = protocol;
        this.gdcHost = gdcHost;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    /**
     * Constructor
     *
     * @param protocol GoodData protocol (HTTP | FTP)
     * @param gdcHost  GoodData host (e.g. secure.gooddata.com)
     * @param username GoodData username
     * @param password GoodData password
     */
    public NamePasswordConfiguration(String protocol, String gdcHost, String username, String password) {
        this(protocol,gdcHost,username,password,0);
    }

    /**
     * Returns the GoodData server URL
     *
     * @return GoodData server URL
     */
    public String getUrl() {
        try {
            URL url;
            if(port > 0) {
                url = new URL(protocol, gdcHost, port, "");
            }
            else {
                url = new URL(protocol, gdcHost, "");
            }
            return url.toString();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * GoodData protocol getter
     *
     * @return GoodData protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * GoodData host getter
     *
     * @return GoodData host
     */
    public String getGdcHost() {
        return gdcHost;
    }

    /**
     * GoodData username getter
     *
     * @return GoodData username
     */
    public String getUsername() {
        return username;
    }

    /**
     * GoodData password getter
     *
     * @return GoodData password
     */
    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setGdcHost(String gdcHost) {
        this.gdcHost = gdcHost;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
