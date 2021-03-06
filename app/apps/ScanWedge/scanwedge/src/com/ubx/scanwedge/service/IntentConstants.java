package com.ubx.scanwedge.service;
/**
 * Created by rocky on 18-11-13.
 */
public class IntentConstants {
    // create the intent
    /*Intent i = new Intent();

    // set the action to perform
        i.setAction(switchToProfile);

    // add additional info
        i.putExtra(extraData, "myProfile");

    // send the intent to DataWedge
    // request and identify the result code
    i.putExtra("SEND_RESULT","true");
    i.putExtra("COMMAND_IDENTIFIER","123456789");
        this.sendBroadcast(i);

       // register to receive the result
    public void onReceive(Context context, Intent intent){

        String command = intent.getStringExtra("COMMAND");
        String commandidentifier = intent.getStringExtra("COMMAND_IDENTIFIER");
        String result = intent.getStringExtra("RESULT");

        Bundle bundle = new Bundle();
        String resultInfo = "";
        if(intent.hasExtra("RESULT_INFO")){
            bundle = intent.getBundleExtra("RESULT_INFO");
            Set<String> keys = bundle.keySet();
            for (String key: keys) {
                resultInfo += key + ": "+bundle.getString(key) + "\n";
            }
        }

        String text = "Command: "+command+"\n" +
                      "Result: " +result+"\n" +
                      "Result Info: " +resultInfo + "\n" +
                      "CID:"+commandidentifier;

        Toast.makeText(context, text, Toast.LENGTH_LONG).show();

    };
       */
    public static final String API_ACTION = "com.symbol.datawedge.api.ACTION";
    public static final String API_RESULT_ACTION = "com.symbol.datawedge.api.RESULT_ACTION";
    public static final String SET_IGNORE_DISABLED_PROFILES = "com.symbol.datawedge.api.SET_IGNORE_DISABLED_PROFILES";//"true"
    public static final String CREATE_PROFILE_Extra = "com.symbol.datawedge.api.CREATE_PROFILE";
    public static final String SWITCH_TO_PROFILE_Extra = "com.symbol.datawedge.api.SWITCH_TO_PROFILE";
    public static final String SEND_RESULT_Extra = "SEND_RESULT";
    public static final String SEND_COMMAND_IDENTIFIER_Extra = "COMMAND_IDENTIFIER";

    public static final String RECEVE_RESULT_INFO_Bundle = "RESULT_INFO";
    public static final String RECEVE_RESULT_String = "RESULT";
    public static final String RECEVE_COMMAND_String = "COMMAND";
    public static final String RECEVE_COMMAND_IDENTIFIER_String = "COMMAND_IDENTIFIER";
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       