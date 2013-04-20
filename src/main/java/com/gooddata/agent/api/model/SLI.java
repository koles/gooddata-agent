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

package com.gooddata.agent.api.model;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.gooddata.agent.util.Constants;

/**
 * GoodData Data Loading Interface (SLI)
 *
 * @author zd <zd@gooddata.com>
 * @version 1.0
 */
public class SLI {

    private final String id;
    private final String name;
    private final String link;
    private String format;

    /**
     * Constructs the new SLI
     */
    public SLI(String id, String name, String link) {
        super();
        this.id = id;
        this.name = name;
        this.link = link;
    }

    /**
     * Constructs the new SLI
     *
     * @param dli the JSON object from the GoodData REST API
     */
    public SLI(JSONObject dli) {
        this(dli.getString("identifier"),
                dli.getString("title"),
                dli.getString("link"));
    }

    /**
     * Returns the SLI's name
     *
     * @return the SLI's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the SLI's URI
     *
     * @return the SLI's URI
     */
    public String getUri() {
        return link;
    }

    /**
     * Returns the SLI's ID
     *
     * @return the SLI's ID
     */
    public String getId() {
        return id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }


    /**
     * Returns the SLI manifest that determines how the data are loaded to the GDC platform.
     * The manifest must replace the default manifest in the template.
     *
     * @param columns SLI columns
     * @return the SLI manifest content.
     */
    public String getSLIManifest(List<Column> columns) {
        JSONObject omf = new JSONObject();
        JSONObject oDataSetManifest = new JSONObject();
        JSONArray oParts = new JSONArray();
        for (Column column : columns) {
            JSONObject oPart = new JSONObject();
            oPart.put("columnName", column.getName());
            oPart.put("mode", column.getMode());
            oPart.put("populates", column.getPopulates());
            String fmt = column.getFormat();
            if (fmt != null && fmt.length() > 0) {
                JSONObject constraints = new JSONObject();
                if (Constants.UNIX_DATE_FORMAT.equalsIgnoreCase(fmt) || Constants.GOODDATA_DATE_FORMAT.equalsIgnoreCase(fmt)) {
                    fmt = Constants.DEFAULT_DATETIME_FMT_STRING;
                }
                constraints.put("date", fmt);
                oPart.put("constraints", constraints);
            }
            int referenceKey = column.getReferenceKey();
            if (referenceKey > 0)
                oPart.put("referenceKey", referenceKey);

            oParts.add(oPart);
        }
        oDataSetManifest.put("parts", oParts);
        oDataSetManifest.put("file", "data.csv");
        oDataSetManifest.put("dataSet", id);
        JSONObject params = new JSONObject();
        params.put("quoteChar", "\"");
        params.put("escapeChar", "\"");
        params.put("separatorChar", ",");
        params.put("endOfLine", "\n");
        oDataSetManifest.put("csvParams", params);
        omf.put("dataSetSLIManifest", oDataSetManifest);
        return omf.toString(2);
    }


}
