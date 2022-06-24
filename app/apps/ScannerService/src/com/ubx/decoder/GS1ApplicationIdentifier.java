package com.ubx.decoder;

import java.util.Hashtable;

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
 * @Date: 20-6-16下午7:50
 */
public class GS1ApplicationIdentifier {
    private static Hashtable<String, GS1ApplicationIdentifier> aiTable;

    private String ai;
    private String sepAi;
    private String description;

    private int aiLength;

    private int valueLength;

    private String value;

    private String decimalPointPlace;

    static {
        loadAITable();
    }

    public GS1ApplicationIdentifier() {
        this.ai = "";
        this.description = "";
        this.aiLength = 0;
        this.valueLength = 0;
        this.value = "";
        this.decimalPointPlace = "";
    }

    public GS1ApplicationIdentifier(String ai,/* String description,*/ int aiLength, int valueLength) {
        this.ai = ai;
        //this.description = description;
        this.aiLength = aiLength;
        this.valueLength = valueLength;
        this.value = "";
        this.decimalPointPlace = "";
    }

    public String getAI() {
        return this.ai;
    }
    public String getSepAI() {
        return this.sepAi;
    }
    public void setSepAI(String sepAI) {
        this.sepAi = sepAI;
    }
    public String getAIWithoutStars() {
        return this.ai.replace("*", "");
    }

    public String getDescription() {
        return this.description;
    }

    public int getAILength() {
        return this.aiLength;
    }

    public int getValueMaxLength() {
        return this.valueLength;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
        //convertValueIfRequired();
    }

    public String getDecimalPointPlace() {
        return this.decimalPointPlace;
    }

    public void setDecimalPointPlace(String decimalPointPlace) {
        this.decimalPointPlace = decimalPointPlace;
    }

    private void convertValueIfRequired() {
        try {
            int starCount = 0;
            for (int i = 0; i < this.ai.length(); i++) {
                if (this.ai.charAt(i) == '*')
                    starCount++;
            }
            if (starCount != 1)
                if (starCount == 2) {
                    if (this.value.length() == 4)
                        this.value += "00";
                } else if (starCount == 3) {
                    if (this.decimalPointPlace != null && this.decimalPointPlace.length() > 0) {
                        Integer decPlace = Integer.valueOf(Integer.parseInt(this.decimalPointPlace));
                        Double val = Double.valueOf(Double.parseDouble(this.value));
                        val = Double.valueOf(val.doubleValue() / Math.pow(10.0D, decPlace.intValue()));
                        this.value = val.toString();
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GS1ApplicationIdentifier getByIdentifier(String ai) {
        return aiTable.get(ai);
    }

    private static void loadAITable() {
        aiTable = new Hashtable<>();
        aiTable.put("00", new GS1ApplicationIdentifier("00", 2, 18));
        aiTable.put("01", new GS1ApplicationIdentifier("01", 2, 14));
        aiTable.put("02", new GS1ApplicationIdentifier("02", 2, 14));
        aiTable.put("10", new GS1ApplicationIdentifier("10", 2, 20));
        aiTable.put("11", new GS1ApplicationIdentifier("11**", 2, 6));
        aiTable.put("12", new GS1ApplicationIdentifier("12**", 2, 6));
        aiTable.put("13", new GS1ApplicationIdentifier("13**", 2, 6));
        aiTable.put("15", new GS1ApplicationIdentifier("15**", 2, 6));
        aiTable.put("16", new GS1ApplicationIdentifier("16**", 2, 6));
        //16 	Sell by date (YYMMDD) 	N2+N6 	SELL BY 	No  ^16(\d{6})$
        aiTable.put("17", new GS1ApplicationIdentifier("17**", 2, 6));
        aiTable.put("20", new GS1ApplicationIdentifier("20", 2, 2));
        aiTable.put("21", new GS1ApplicationIdentifier("21", 2, 20));
        aiTable.put("22", new GS1ApplicationIdentifier("22", 2, 29));
        //235 	Third Party Controlled, Serialised Extension of GTIN (TPX) 	N3+X..28 	TPX 	Yes
        //^235([\x21-\x22\x25-\x2F\x30-\x39\x3A-\x3F\x41-\x5A\x5F\x61-\x7A]{0,28})$
        aiTable.put("240", new GS1ApplicationIdentifier("240", 3, 30));
        aiTable.put("241", new GS1ApplicationIdentifier("241", 3, 30));
        aiTable.put("242", new GS1ApplicationIdentifier("242", 2, 6));
        aiTable.put("250", new GS1ApplicationIdentifier("250", 3, 30));
        aiTable.put("251", new GS1ApplicationIdentifier("251", 3, 30));
        aiTable.put("253", new GS1ApplicationIdentifier("253", 3, 17));
        aiTable.put("254", new GS1ApplicationIdentifier("254", 3, 20));
        //255 	Global Coupon Number (GCN) 	N3+N13+N..12 	GCN 	Yes ^255(\d{13})(\d{0,12})$
        aiTable.put("30", new GS1ApplicationIdentifier("30", 2, 8));
        aiTable.put("310", new GS1ApplicationIdentifier("310***", 4, 6));
        aiTable.put("311", new GS1ApplicationIdentifier("311***", 4, 6));
        aiTable.put("312", new GS1ApplicationIdentifier("312***", 4, 6));
        aiTable.put("313", new GS1ApplicationIdentifier("313***", 4, 6));
        aiTable.put("314", new GS1ApplicationIdentifier("314***", 4, 6));
        aiTable.put("315", new GS1ApplicationIdentifier("315***", 4, 6));
        aiTable.put("316", new GS1ApplicationIdentifier("316***", 4, 6));
        aiTable.put("320", new GS1ApplicationIdentifier("320***", 4, 6));
        aiTable.put("321", new GS1ApplicationIdentifier("321***", 4, 6));
        aiTable.put("322", new GS1ApplicationIdentifier("322***", 4, 6));
        aiTable.put("323", new GS1ApplicationIdentifier("323***", 4, 6));
        aiTable.put("324", new GS1ApplicationIdentifier("324***", 4, 6));
        aiTable.put("325", new GS1ApplicationIdentifier("325***", 4, 6));
        aiTable.put("326", new GS1ApplicationIdentifier("326***", 4, 6));
        aiTable.put("327", new GS1ApplicationIdentifier("327***", 4, 6));
        aiTable.put("328", new GS1ApplicationIdentifier("328***", 4, 6));
        aiTable.put("329", new GS1ApplicationIdentifier("329***", 4, 6));
        aiTable.put("330", new GS1ApplicationIdentifier("330***", 4, 6));
        aiTable.put("331", new GS1ApplicationIdentifier("331***", 4, 6));
        aiTable.put("332", new GS1ApplicationIdentifier("332***", 4, 6));
        aiTable.put("333", new GS1ApplicationIdentifier("333***", 4, 6));
        aiTable.put("334", new GS1ApplicationIdentifier("334***", 4, 6));
        aiTable.put("335", new GS1ApplicationIdentifier("335***", 4, 6));
        aiTable.put("336", new GS1ApplicationIdentifier("336***", 4, 6));
        aiTable.put("337", new GS1ApplicationIdentifier("337***", 4, 6));
        aiTable.put("340", new GS1ApplicationIdentifier("340***", 4, 6));
        aiTable.put("341", new GS1ApplicationIdentifier("341***", 4, 6));
        aiTable.put("342", new GS1ApplicationIdentifier("342***", 4, 6));
        aiTable.put("343", new GS1ApplicationIdentifier("343***", 4, 6));
        aiTable.put("344", new GS1ApplicationIdentifier("344***", 4, 6));
        aiTable.put("345", new GS1ApplicationIdentifier("345***", 4, 6));
        aiTable.put("346", new GS1ApplicationIdentifier("346***", 4, 6));
        aiTable.put("347", new GS1ApplicationIdentifier("347***", 4, 6));
        aiTable.put("348", new GS1ApplicationIdentifier("348***", 4, 6));
        aiTable.put("349", new GS1ApplicationIdentifier("349***", 4, 6));
        aiTable.put("350", new GS1ApplicationIdentifier("350***", 4, 6));
        aiTable.put("351", new GS1ApplicationIdentifier("351***", 4, 6));
        aiTable.put("352", new GS1ApplicationIdentifier("352***", 4, 6));
        aiTable.put("353", new GS1ApplicationIdentifier("353***", 4, 6));
        aiTable.put("354", new GS1ApplicationIdentifier("354***", 4, 6));
        aiTable.put("355", new GS1ApplicationIdentifier("355***", 4, 6));
        aiTable.put("356", new GS1ApplicationIdentifier("356***", 4, 6));
        aiTable.put("357", new GS1ApplicationIdentifier("357***", 4, 6));
        aiTable.put("360", new GS1ApplicationIdentifier("360***", 4, 6));
        aiTable.put("361", new GS1ApplicationIdentifier("361***", 4, 6));
        aiTable.put("362", new GS1ApplicationIdentifier("362***", 4, 6));
        aiTable.put("363", new GS1ApplicationIdentifier("363***", 4, 6));
        aiTable.put("364", new GS1ApplicationIdentifier("364***", 4, 6));
        aiTable.put("365", new GS1ApplicationIdentifier("365***", 4, 6));
        aiTable.put("366", new GS1ApplicationIdentifier("366***", 4, 6));
        aiTable.put("367", new GS1ApplicationIdentifier("367***", 4, 6));
        aiTable.put("368", new GS1ApplicationIdentifier("368***", 4, 6));
        aiTable.put("369", new GS1ApplicationIdentifier("369***", 4, 6));
        aiTable.put("37", new GS1ApplicationIdentifier("37", 2, 8));
        aiTable.put("390", new GS1ApplicationIdentifier("390***", 4, 15));
        aiTable.put("391", new GS1ApplicationIdentifier("391***", 4, 18));
        aiTable.put("392", new GS1ApplicationIdentifier("392***", 4, 15));
        aiTable.put("393", new GS1ApplicationIdentifier("393***", 4, 18));
        aiTable.put("400", new GS1ApplicationIdentifier("400", 3, 30));
        aiTable.put("401", new GS1ApplicationIdentifier("401", 3, 30));
        aiTable.put("402", new GS1ApplicationIdentifier("402", 3, 17));
        aiTable.put("403", new GS1ApplicationIdentifier("403", 3, 30));
        aiTable.put("410", new GS1ApplicationIdentifier("410", 3, 13));
        aiTable.put("411", new GS1ApplicationIdentifier("411", 3, 13));
        aiTable.put("412", new GS1ApplicationIdentifier("412", 3, 13));
        aiTable.put("413", new GS1ApplicationIdentifier("413", 3, 13));
        aiTable.put("414", new GS1ApplicationIdentifier("414", 3, 13));
        aiTable.put("415", new GS1ApplicationIdentifier("415", 3, 13));
        aiTable.put("420", new GS1ApplicationIdentifier("420", 3, 20));
        aiTable.put("421", new GS1ApplicationIdentifier("421", 3, 12));
        aiTable.put("422", new GS1ApplicationIdentifier("422", 3, 3));
        aiTable.put("423", new GS1ApplicationIdentifier("423", 3, 15));
        aiTable.put("424", new GS1ApplicationIdentifier("424", 3, 3));
        aiTable.put("425", new GS1ApplicationIdentifier("425", 3, 3));
        aiTable.put("426", new GS1ApplicationIdentifier("426", 3, 3));
        aiTable.put("7001", new GS1ApplicationIdentifier("7001", 4, 13));
        aiTable.put("7002", new GS1ApplicationIdentifier("7002", 4, 30));
        aiTable.put("7030", new GS1ApplicationIdentifier("7030", 4, 30));
        aiTable.put("7031", new GS1ApplicationIdentifier("7031", 4, 30));
        aiTable.put("7032", new GS1ApplicationIdentifier("7032", 4, 30));
        aiTable.put("7033", new GS1ApplicationIdentifier("7033", 4, 30));
        aiTable.put("7034", new GS1ApplicationIdentifier("7034", 4, 30));
        aiTable.put("7035", new GS1ApplicationIdentifier("7035", 4, 30));
        aiTable.put("7036", new GS1ApplicationIdentifier("7036", 4, 30));
        aiTable.put("7037", new GS1ApplicationIdentifier("7037", 4, 30));
        aiTable.put("7038", new GS1ApplicationIdentifier("7038", 4, 30));
        aiTable.put("7039", new GS1ApplicationIdentifier("7039", 4, 30));
        aiTable.put("8001", new GS1ApplicationIdentifier("8001", 4, 14));
        aiTable.put("8002", new GS1ApplicationIdentifier("8002", 4, 20));
        aiTable.put("8003", new GS1ApplicationIdentifier("8003", 4, 30));
        aiTable.put("8004", new GS1ApplicationIdentifier("8004", 4, 30));
        aiTable.put("8005", new GS1ApplicationIdentifier("8005", 4, 6));
        aiTable.put("8006", new GS1ApplicationIdentifier("8006", 4, 18));
        aiTable.put("8007", new GS1ApplicationIdentifier("8007", 4, 30));
        aiTable.put("8008", new GS1ApplicationIdentifier("8008", 4, 12));
        aiTable.put("8009", new GS1ApplicationIdentifier("8009", 4, 50));
        aiTable.put("8010", new GS1ApplicationIdentifier("8010", 4, 30));
        aiTable.put("8018", new GS1ApplicationIdentifier("8018", 4, 18));
        aiTable.put("8020", new GS1ApplicationIdentifier("8020", 4, 25));
        aiTable.put("8100", new GS1ApplicationIdentifier("8100", 4, 6));
        aiTable.put("8101", new GS1ApplicationIdentifier("8101", 4, 10));
        aiTable.put("8102", new GS1ApplicationIdentifier("8102", 4, 2));
        aiTable.put("8110", new GS1ApplicationIdentifier("8110", 4, 70));
        aiTable.put("8111", new GS1ApplicationIdentifier("8111", 4, 4));
        aiTable.put("8112", new GS1ApplicationIdentifier("8112", 4, 70));
        aiTable.put("8200", new GS1ApplicationIdentifier("8200", 4, 70));
        aiTable.put("90", new GS1ApplicationIdentifier("90", 2, 30));
        aiTable.put("91", new GS1ApplicationIdentifier("91", 2, 30));
        aiTable.put("92", new GS1ApplicationIdentifier("92", 2, 30));
        aiTable.put("93", new GS1ApplicationIdentifier("93", 2, 30));
        aiTable.put("94", new GS1ApplicationIdentifier("94", 2, 30));
        aiTable.put("95", new GS1ApplicationIdentifier("95", 2, 30));
        aiTable.put("96", new GS1ApplicationIdentifier("96", 2, 30));
        aiTable.put("97", new GS1ApplicationIdentifier("97", 2, 30));
        aiTable.put("98", new GS1ApplicationIdentifier("98", 2, 30));
        aiTable.put("99", new GS1ApplicationIdentifier("99", 2, 30));
    }
}
