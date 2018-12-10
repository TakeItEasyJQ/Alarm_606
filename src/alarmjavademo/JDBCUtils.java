package alarmjavademo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCUtils {
	
	public static Connection connection ;  // 数据库的连接接口
	public static Statement statement ;	// 通过此对象的executeUpdate()方法来启用SQL语句
	public static ResultSet resultset;
	
	static {
		//连接数据库
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
	//启用数据库前先调用此方法连接数据库
	public static void getStatement(Connection conn){
		try {
			
			//获取statement对象
			statement = conn.createStatement();
			//具体的sql语句
			//若不存在info表则创建
			statement.executeUpdate("create table if not exists 'info' (id String , time String ,ip String)");
			
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}
	
	public static void closeConnection(ResultSet resultset, Statement statement ,Connection conn){
		try {
			if(resultset!=null){
				resultset.close();
			}
			if(statement!=null){
				statement.close();
			}
			if(connection!=null){
				JDBCUtils.connection.close();				
			}
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	
	// 向数据库添加信息
	public static int insertInfo(String id , String time,String ip) throws SQLException{
		return JDBCUtils.statement.executeUpdate("insert into info values ("+id+","+time+","+ip+")" );
	}
	
}
