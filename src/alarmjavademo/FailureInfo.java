package alarmjavademo;

public class FailureInfo {
	// 存储向服务器发送失败的信息的属性对象
	
	public String id;
	public String userid;
	public String time;
	public String ip;
	
	public FailureInfo(String userid,String time , String ip,String id) {
		// TODO 自动生成的构造函数存根
		this.id=id;
		this.userid=userid;
		this.time=time;
		this.ip=ip;
	}
}
