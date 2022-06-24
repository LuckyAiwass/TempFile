package com.qti.factory.Framework;

import java.io.BufferedReader; 
import java.io.DataOutputStream; 
import java.io.IOException; 
import java.io.InputStreamReader; 
import java.util.List; 
import android.util.Log;

/** 
* ShellUtils 
*/ 
public class ShellUtils { 

	private static final String LOG_TAG="zxj";
	public static final String COMMAND_SU = "/system/xbin/su"; 
	public static final String COMMAND_SH = "sh"; 
	public static final String COMMAND_EXIT = "exit\n"; 
	public static final String COMMAND_LINE_END = "\n"; 
	public static final String MY_ROOT_FILE = "system/bin/superman";

	private ShellUtils() { 
		throw new AssertionError(); 
	} 


	/** 
	* check whether has root permission 
	* 
	* @return 
	*/ 
	public static boolean checkRootPermission() { 
		return execCommand("echo root", true, false).result == 0; 
	} 


	/** 
	* execute shell command, default return result msg 
	* 
	* @param command command 
	* @param isRoot whether need to run with root 
	* @return 
	* @see ShellUtils#execCommand(String[], boolean, boolean) 
	*/ 
	public static CommandResult execCommand(String command, boolean isRoot) { 
		return execCommand(new String[] {command}, isRoot, true); 
	} 

	/** 
	* execute shell commands, default return result msg 
	* 
	* @param commands command array 
	* @param isRoot whether need to run with root 
	* @return 
	* @see ShellUtils#execCommand(String[], boolean, boolean) 
	*/ 
	public static CommandResult execCommand(String[] commands, boolean isRoot) { 
		return execCommand(commands, isRoot, true); 
	} 


	/** 
	* execute shell command 
	* 
	* @param command command 
	* @param isRoot whether need to run with root 
	* @param isNeedResultMsg whether need result msg 
	* @return 
	* @see ShellUtils#execCommand(String[], boolean, boolean) 
	*/ 
	public static CommandResult execCommand(String command, boolean isRoot, boolean isNeedResultMsg) { 
		return execCommand(new String[] {command}, isRoot, isNeedResultMsg); 
	} 


	/** 
	* execute shell commands 
	* 
	* @param commands command array 
	* @param isRoot whether need to run with root 
	* @param isNeedResultMsg whether need result msg 
	* @return

	*
	if isNeedResultMsg is false, {@link CommandResult#successMsg} is null and
	* {@link CommandResult#errorMsg} is null.

	*
	if {@link CommandResult#result} is -1, there maybe some excepiton.

	*

	*/ 
	public static CommandResult execCommand(String[] commands, boolean isRoot, boolean isNeedResultMsg) { 
		int result = -2; 
		if (commands == null || commands.length == 0) { 
			return new CommandResult(result, null, null); 
		} 


		Process process = null; 
		BufferedReader successResult = null; 
		BufferedReader errorResult = null; 
		StringBuilder successMsg = null; 
		StringBuilder errorMsg = null; 


		DataOutputStream os = null; 
		try { 
			process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH); 
			os = new DataOutputStream(process.getOutputStream()); 
				for (String command : commands) { 
					if (command == null) { 
					continue; 
					} 
				Log.d("zxj","command="+command);
				// donnot use os.writeBytes(commmand), avoid chinese charset error 
				os.write(command.getBytes()); 
				os.writeBytes(COMMAND_LINE_END); 
				os.flush(); 
				} 
			os.writeBytes(COMMAND_EXIT); 
			os.flush(); 

			result = process.waitFor(); 
			if (isNeedResultMsg) { 
				successMsg = new StringBuilder(); 
				errorMsg = new StringBuilder(); 
				successResult = new BufferedReader(new InputStreamReader(process.getInputStream())); 
				errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream())); 
				String s; 
				while ((s = successResult.readLine()) != null) { 
					successMsg.append(s); 
				} 
				while ((s = errorResult.readLine()) != null) { 
					errorMsg.append(s); 
				} 
			} 
		} catch (IOException e) { 
			e.printStackTrace(); 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} finally { 
			try { 
				if (os != null) { 
				os.close(); 
				} 
				if (successResult != null) { 
				successResult.close(); 
				} 
				if (errorResult != null) { 
				errorResult.close(); 
				} 
			} catch (IOException e) { 
				e.printStackTrace(); 
			} 
			if (process != null) { 
			process.destroy(); 
			} 
		}	 
		return new CommandResult(result, successMsg == null ? null : successMsg.toString(), errorMsg == null ? null : errorMsg.toString()); 
	} 


	/** 
	* result of command 
	*

	*
	{@link CommandResult#result} means result of command, 0 means normal, else means error, same to excute in
	* linux shell

	*
	{@link CommandResult#successMsg} means success message of command result

	*
	{@link CommandResult#errorMsg} means error message of command result

	*/ 
	public static class CommandResult { 


		/** result of command **/ 
		public int result; 
		/** success message of command result **/ 
		public String successMsg; 
		/** error message of command result **/ 
		public String errorMsg; 


		public CommandResult(int result) { 
			this.result = result; 
		} 


		public CommandResult(int result, String successMsg, String errorMsg) { 
			this.result = result; 
			this.successMsg = successMsg; 
			this.errorMsg = errorMsg; 
		} 
	} 
} 