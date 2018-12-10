package alarmjavademo;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtils {

	public static String door_ip1 = "10.123.192.201";
	public static String door_ip2 = "10.123.192.202";

	// http://zh.pc.api.smart.ordosmz.cn/api/safety/v1/certification/auth
	// ?auth_time=2018-03-23 11:32:22&card_code=3066611872&device_ip=10.123.211.100

	// http://192.168.2.22:2026/api/attendance/v1/attendance/post?attendanceID=0003&attendanceTime=2018-10-25
	// 11:11:11&deviceIP=192.168.1.4

	// 蒙中教师签到
//	public static final String requestHeader = "http://zh.pc.api.smart.ordosmz.cn/api/safety/v1/certification/auth";
	//
//	public static final String requestHeader = "http://192.168.2.22:2026/api/attendance/v1/attendance/post";
//	public static final String requestHeader = "http://zh.pc.api.angsuxx.smart.orhontech.com/api/safety/v1/dormitory/dormitoryauth";
	
	public static String requestHeader;
	static {
		try { 
			requestHeader = (String) Configs.getParamFromDesktopPropIS("api");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 教师签到
	 * http://zh.pc.api.smart.ordosmz.cn/api/safety/v1/certification/auth?auth_time=
	 * 2018-03-23 11:32:22&card_code=3066611872&device_ip=10.123.211.100 学生的宿舍签到
	 * http://zh.pc.api.smart.ordosmz.cn/api/safety/v1/dormitory/dormitoryauth?
	 * auth_time=2018-03-23 11:32:22&card_code=3066611872&device_ip=10.123.211.100
	 * 域名 zh.pc.api.angsuxx.smart.orhontech.com/
	 */

	public static OkHttpClient client = new OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
			.readTimeout(5, TimeUnit.SECONDS).writeTimeout(5, TimeUnit.SECONDS).build();

	public static int totalCount = 0;

	// 向服务器发送数据的重载方法
	public static void PostToService(String id, String time, String ip, Callback callback) {

		RequestBody body = new FormBody.Builder().add("auth_time", time).add("card_code", id).add("device_ip", ip)
				.build();
		Request request = new Request.Builder().url(requestHeader).post(body).build();
		client.newCall(request).enqueue(callback);
	}

	public static void PostToService(FailureInfo info, Callback callback) {
		RequestBody body = new FormBody.Builder().add("auth_time", info.time).add("card_code", info.id)
				.add("device_ip", info.ip).build();
		Request request = new Request.Builder().url(requestHeader).post(body).build();
		client.newCall(request).enqueue(callback);
	}
}
