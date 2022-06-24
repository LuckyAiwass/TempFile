package com.ubx.propertyparser;

import android.text.TextUtils;

/*
 * Copyright (C) 2019, Urovo Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @Author: rocky
 * @Date: 20-5-27下午3:21
 */
//<property id="46" name="FULL_READ_MODE" scannertype="5,8,13,15" category="DECODER" value_type="INTEGER" value_min="0" value_max="1">1</property>
//"{\"property\":[
// {\"id\":46,\"name\":\"FULL_READ_MODE\",\"scannertype\":\"5,8,13,15\",\"category\":\"DECODER\",\"value_type\":\"INTEGER\",\"displayName\":\"Full read mode\",\"discreteCount\":0,\"discreteEntryValues\":\"0\",\"discreteEntries\":\"0\",\"value_min\":0,\"value_max\":1,\"paramNum\":300,\"defaultValue\":\"0\"},
// {\"id\":46,\"name\":\"FULL_READ_MODE\",\"scannertype\":\"5,8,13,15\",\"category\":\"DECODER\",\"value_type\":\"INTEGER\",\"displayName\":\"Full read mode\",\"discreteCount\":0,\"discreteEntryValues\":\"0\",\"discreteEntries\":\"0\",\"value_min\":0,\"value_max\":1,\"paramNum\":300,\"defaultValue\":\"0\"},
// ]
// }"
public class Property {
    private String name;
    private String value;
    private int id;
    private int min;
    private int max;
    private String category;
    private String valueType;
    private boolean isSupport;
    private String supportType;
    private String displayName;
    private String defaultValue;
    private int discreteCount;
    private String discreteEntries;
    private String discreteEntryValues;
    private int paramNum;

    public String getName() {
        return name;
    }

    public String getSupportType() {
        return supportType;
    }

    public void setSupportType(String supportType) {
        this.supportType = supportType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public boolean isSupport() {
        return isSupport;
    }

    public void setSupport(boolean support) {
        isSupport = support;
    }

    public boolean isSupport(String type) {
        if (!TextUtils.isEmpty(supportType)) {
            if (supportType.equals("*")) {
                isSupport = true;
                return true;
            } else {
                String[] list = supportType.split(",");
                for (String id : list) {
                    if (type.equals(id)) {
                        isSupport = true;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getParamNum() {
        return paramNum;
    }

    public void setParamNum(int paramNum) {
        this.paramNum = paramNum;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getDiscreteCount() {
        return discreteCount;
    }

    public void setDiscreteCount(int discreteCount) {
        this.discreteCount = discreteCount;
    }

    public String getDiscreteEntries() {
        return discreteEntries;
    }

    public void setDiscreteEntries(String discreteEntries) {
        this.discreteEntries = discreteEntries;
    }

    public String getDiscreteEntryValues() {
        return discreteEntryValues;
    }

    public void setDiscreteEntryValues(String discreteEntryValues) {
        this.discreteEntryValues = discreteEntryValues;
    }
}
