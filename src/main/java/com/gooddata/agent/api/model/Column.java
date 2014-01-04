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

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * GoodData SLI Column
 *
 * @author zd <zd@gooddata.com>
 * @version 1.0
 */
public class Column {

    private String name;
    private String mode;
    private int referenceKey;
    private String format;
    private String[] populates;

    /**
     * Full load mode. All the existing date will be replaced.
     */
    public static final String LM_FULL = "FULL";

    /**
     * Incremental load mode. All the existing date will be preserved. The new data are going to be appended.
     */
    public static final String LM_INCREMENTAL = "INCREMENTAL";

    /**
     * Constructs a new SLI column
     *
     * @param name the column name
     */
    public Column(String name) {
        this.setName(name);
    }

    /**
     * Constructs a new SLI column
     *
     * @param column the JSON object from the GoodData REST API
     */
    public Column(JSONObject column) {
        name = column.getString("columnName");
        mode = column.getString("mode");
        JSONArray pa = column.getJSONArray("populates");
        populates = new String[pa.size()];
        for (int i = 0; i < pa.size(); i++) {
            populates[i] = pa.getString(i);
        }
        populates = (String[]) pa.toArray(populates);
        if (column.containsKey("referenceKey"))
            referenceKey = column.getInt("referenceKey");
    }

    /**
     * Returns the column name
     *
     * @return the column name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the column name
     *
     * @param name the column name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the column mode
     *
     * @return the column mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the column mode
     *
     * @param mode the column mode
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the column referenceKey
     *
     * @return the column referenceKey
     */
    public int getReferenceKey() {
        return referenceKey;
    }

    /**
     * Sets the column referenceKey
     *
     * @param referenceKey the column referenceKey
     */
    public void setReferenceKey(int referenceKey) {
        this.referenceKey = referenceKey;
    }

    public String[] getPopulates() {
        return populates;
    }

    public void setPopulates(String[] populates) {
        this.populates = populates;
    }

    /**
     * The standard toString
     *
     * @return the string description of the object
     */
    public String toString() {
        return "{name='" + name + "', mode='" + mode + "', referenceKey='" + referenceKey + "'}";
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

}