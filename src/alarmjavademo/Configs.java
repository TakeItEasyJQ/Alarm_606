package alarmjavademo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Configs {

	public static Calendar calendar = Calendar.getInstance();
	public static Date date = new Date();
	public static String[] cameraIp = new String[1];

	/*******************************************************************/
	
	public static StringBuffer results = new StringBuffer();
	public static String dllPath;
	static {
		try {
			dllPath = (String) getParamFromDesktopPropIS("path");
			System.out.println(dllPath);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		checkConfig();
		
	}
	
	// 加载配置文件的方法，获取结果都为String
	public static Object getParamFromDesktopPropIS(String key) throws FileNotFoundException {
//		File file = new File("D:\\eclipse-workspace\\606\\606Java\\parameters.properties");
		File file = new File("config\\parameters.properties");
		InputStream is = new FileInputStream(file);
		Properties props = new Properties();
		try {
			props.load(is);
			return props.get(key);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String[]> LoginInfos = new ArrayList<>();

	// 此方法将需要注册的两个设备的信息都存到LoginInfos中了
	// 每个对象为一个字符串数组，数组包含的信息的顺序为ip,port,usname,pword
	public static void getLoginInfoList() {

		String[] loginInfo1 = new String[4];
		String[] loginInfo2 = new String[4];
		try {
			String ip1 = (String) Configs.getParamFromDesktopPropIS("ip1");
			String ip2 = (String) Configs.getParamFromDesktopPropIS("ip2");
			String port = (String) Configs.getParamFromDesktopPropIS("port");
			String username1 = (String) Configs.getParamFromDesktopPropIS("username1");
			String username2 = (String) Configs.getParamFromDesktopPropIS("username2");
			String password1 = (String) Configs.getParamFromDesktopPropIS("password1");
			String password2 = (String) Configs.getParamFromDesktopPropIS("password2");
			
			loginInfo1[0] = ip1;
			loginInfo1[1] = port;
			loginInfo1[2] = username1;
			loginInfo1[3] = password1;

			LoginInfos.add(loginInfo1);

			loginInfo2[0] = ip2;
			loginInfo2[1] = port;
			loginInfo2[2] = username2;
			loginInfo2[3] = password2;

			LoginInfos.add(loginInfo2);

		} catch (FileNotFoundException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

	public static void printIntoFileResult(String result) {
		File file = new File(".\\result.txt");
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fw);
			out.write(result, 0, result.length());
			out.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized static void addInfoToResult(String message) {
		Configs.results.append(message+"\n");
	}

	public static void checkConfig() {
		File file = new File(".\\config");
		File result = new File(".\\check.txt");
		try {
			FileWriter 	fw = new FileWriter(result);
			BufferedWriter bw = new BufferedWriter(fw);
			if (file.exists()) {
				bw.write("has config");
				bw.close();
				fw.close();
				System.out.println("has");
			}else if(!file.exists()) {
				bw.write("hasn't config");
				bw.close();
				fw.close();
				System.out.println("hasn't");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}
