package alarmjavademo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.experimental.results.FailureList;
import org.junit.internal.runners.statements.Fail;

import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class JDBCPool {

	public static int initConnectionCount = 6; // 连接池最大连接数
	public static int maxConnectionCount = 10; // 允许的最大连接数
	public static int currentConnectionCount = 0; // 当前的连接数

	public static final String DBurl = "jdbc:sqlite:sample.db";

	// 数据库连接池
	public static LinkedList<Connection> Connections = new LinkedList<>();
	// 向服务器发送失败的信息集
	public static LinkedList<FailureInfo> failureList = new LinkedList<>();

	public static Timer timer;
	public static TimerTask timerTask;

	public static void init() {
		// 执行完此方法后会在连接池中存在6个待使用的连接对象(connection)
		for (int i = 0; i < initConnectionCount; i++) {
			try {
				Connections.add(createConnection());
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}

		// 用来检测是否已经存在info表
		try {
			Connection conn = DriverManager.getConnection(DBurl);
			Statement statement = conn.createStatement();
			statement.executeUpdate("create table if not exists 'info'(userid char ,time char , ip char , id integer primary key autoincrement)");
			System.out.println("check table exists ready ! ");
		} catch (SQLException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}

		timer = new Timer();
		// 为定时任务设定其所要执行的逻辑
		/*
		 * 1、从DB中查找数据，每次20条; 2、将LinkedList中的数据发送到服务器中 3、成功则从数据库中删除对应的数据，失败不做任何操作
		 */
		timerTask = new TimerTask() {

			@Override
			public void run() {
				// TODO 自动生成的方法存根
				System.out.println("start timerTask");
				Connection conn = null;
				Statement statement = null;
				ResultSet resultset = null;
				try {
					conn = JDBCPool.createConnection();
					if (conn == null) {
						conn = DriverManager.getConnection(JDBCPool.DBurl);
						JDBCPool.currentConnectionCount++;
					}
					statement = conn.createStatement();
					resultset = statement.executeQuery(JDBCPool.QUERYSQL());
					while (resultset.next()) {
						// 将查找到的结果都保存到FailureInfo对象中，再将对象保存到LinkedList中
						FailureInfo info = new FailureInfo(resultset.getString("id"), resultset.getString("time"),
								resultset.getString("ip"),resultset.getString("id"));
						JDBCPool.failureList.add(info);
					}
				} catch (SQLException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				} finally {
					try {
						close(resultset, statement, conn);
					} catch (SQLException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}
					
				System.out.println("failureList size :"+failureList.size());
				// 以上已将获取到的Info存入List中
				if (!JDBCPool.failureList.isEmpty()) {
					Gson gson = new Gson();
					for (FailureInfo info : failureList) {
						HttpUtils.PostToService(info, new Callback() {

							@Override
							public void onResponse(Call arg0, Response arg1) throws IOException {
								// TODO 自动生成的方法存根
								GsonAuthResult result = gson.fromJson(arg1.body().string(), GsonAuthResult.class);
								// 如果成功则从数据库中删除
								if (result!=null&& result.message!=null&& result.message.contains("成功")) {
									Connection conn = null;
									Statement statement = null;
									try {
										conn = JDBCPool.createConnection();
										if (conn == null) {
											conn = DriverManager.getConnection(JDBCPool.DBurl);
										}
										statement = conn.createStatement();
										statement.executeUpdate(DeleteSQL(info));
									} catch (SQLException e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
									} finally {
										try {
											close(null, statement, conn);
										} catch (SQLException e) {
											// TODO 自动生成的 catch 块
											e.printStackTrace();
										}
									}
									
									System.out.println("timer程序跑到成功这了");
									
								}
								else{
									System.out.println("timer程序跑到失败这了"+"  "+info.id);
									System.out.println(info.id+" "+info.ip+" "+info.time+" "+info.userid);
								}

							}

							@Override
							public void onFailure(Call arg0, IOException arg1) {
								// TODO 自动生成的方法存根

							}
						});
					}
					failureList.clear();
					System.out.println(failureList.size());
				}
			}
		};
	}

	/****************************************************************/

	// 测试：清空数据库
	public static void clearDB() {
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			conn = JDBCPool.getConnection();
			statement = conn.createStatement();
			statement.executeUpdate("delete  from info");
			rs = statement.executeQuery("select * from info");
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {

			try {
				while (rs.next()) {
					System.out.println(rs.getString("time"));
				}
				JDBCPool.close(rs, statement, conn);
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}

	// 测试：删除表
	public static void deleteTable(){
		Connection conn = null ;
		Statement statement = null ;
		try {
			conn = JDBCPool.createConnection();
			statement = conn .createStatement();
			statement.executeUpdate("drop table if exists info");
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		finally {
			try {
				JDBCPool.close(null, statement, conn);
				System.out.println("delete table info success !");
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	
	// 测试：删除某条
	public static void deleteColumn(String id){
		Connection conn = null ;
		Statement statement = null ;
		try {
			conn = JDBCPool.createConnection();
			statement = conn .createStatement();
			statement.executeUpdate("delete from info where id = "+id);
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		finally {
			try {
				JDBCPool.close(null, statement, conn);
				System.out.println("delete column info success !");
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	// 测试：查询数据
	public static void queryInfo() {
		int a = 0;
		Connection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			conn = JDBCPool.createConnection();
			statement = conn.createStatement();
			rs = statement.executeQuery("select * from info");
		} catch (SQLException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} finally {
			try {
				while (rs.next()) {
					System.out.println(rs.getString("id") +" "+rs.getString("userid") +" "+rs.getString("time") +"  "+ rs.getString("ip") + " count: "+  a++);
					
					
				}
			} catch (SQLException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
	}
	
	// 测试：网络测试
//	public static void post() {
//			Gson gson = new Gson();
//
//			for (int i = 0; i < 300; i++) {
//
//				HttpUtils.PostToService("0000010000", "15:00", "192.168.1.1", new Callback() {
//					
//					Connection conn = null;
//					Statement statement = null;	
//					
//					@Override
//					public void onResponse(Call arg0, Response arg1) throws IOException {
//						// TODO 自动生成的方法存根
//						GsonAuthResult result = gson.fromJson(arg1.body().string(), GsonAuthResult.class);
//						if (result.message.contains("失败")) {
//							try {
//								conn = JDBCPool.getConnection();
//								statement = conn.createStatement();
////								statement.executeUpdate("insert into info values('0000010000','15:00','192.168.1.1')");
//								statement.executeUpdate(InsertSQL("0000010000", "15:00", "192.168.1.1"));
//							} catch (SQLException e) {
//								// TODO 自动生成的 catch 块
//								e.printStackTrace();
//							} finally {
//								try {
//									JDBCPool.close(null, statement, conn);
//								} catch (SQLException e) {
//									// TODO 自动生成的 catch 块
//									e.printStackTrace();
//								}
//							}
//							
//							System.out.println(++HttpUtils.totalCount + "onsuccess 签到失败");
//						} else if (result.message.contains("成功")) {
//							System.out.println(++HttpUtils.totalCount + "onsuccess 签到成功");
//						}
//
//					}
//
//					@Override
//					public void onFailure(Call arg0, IOException arg1) {
//						// TODO 自动生成的方法存根
//						try {
//							conn = JDBCPool.getConnection();
//							statement = conn.createStatement();
//							statement.executeUpdate("insert into info values('0000010000','15:00','192.168.1.1')");
//						} catch (SQLException e) {
//							// TODO 自动生成的 catch 块
//							e.printStackTrace();
//						} finally {
//							try {
//								JDBCPool.close(null, statement, conn);
//							} catch (SQLException e) {
//								// TODO 自动生成的 catch 块
//								e.printStackTrace();
//							}
//						}
//						System.out.println(++HttpUtils.totalCount + "onfailure 签到失败");
//					}
//				});
//				
//				
//				
//					
//
//			}
//			
//			System.out.println(String .valueOf(JDBCPool.Connections.size()));
//
//		}

	/****************************************************************/

	// 检查是否要将SQLite中的信息提交到服务器
	// public class CheckThread extends Thread{
	// @Override
	// public void run() {
	//
	// while(flag){
	// // 这里执行想要做的代码
	// if(!failureList.isEmpty()){
	// FailureInfo info = failureList.get(0);
	// failureList.remove(0);
	// try {
	// Connection conn = getConnection();
	// if (conn !=null){
	// Statement statement = conn.createStatement();
	// statement.executeUpdate(InsertSQL(info));
	// }
	// } catch (SQLException e) {
	// // TODO 自动生成的 catch 块
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	// //线程循环的控制flag
	// public volatile boolean flag = true;
	// // 线程的停止方法
	// public void stopTask(){
	// flag = false ;
	// }
	// }

	// 创建连接 然后放入连接池的linkedlist中
	//
	public static synchronized Connection createConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(DBurl);
		return connection;
	}

	// 从连接池获取可用连接，若返回null则代表当前使用的连接数量已为最大值

	// 加锁是为了防止某一线程在访问此方法时另一线程也访问此方法
	public static synchronized Connection getConnection() throws SQLException {
		// 如果连接池中有可用的连接
		if (Connections.size() > 0) {
			Connection conn = Connections.get(0); // 获取
			currentConnectionCount++; // 当前使用的连接数量+1
			Connections.remove(0); // 从池中删除
			return conn;
		}
		// 如果池中不存在可用连接了，且当前可用的连接小于允许存在的最大连接数
		else if (Connections.size() == 0 && currentConnectionCount < 10) {
			Connection conn = createConnection(); // 新建连接暂时使用
			return conn;
		} else {
			return null; // null 为当所有条件都不允许再重新创建数据库连接时
		}
	}

	// 当执行完SQL语句后,关闭所有连接，释放资源
	public static synchronized void close(ResultSet resultset, Statement statement, Connection conn)
			throws SQLException {
		if (resultset != null&&!resultset.isClosed()) {
			resultset.close();
		}
		if (statement != null&&!statement.isClosed()) {
			statement.close();
		}
		if (conn != null&&!conn.isClosed()) {
			if (Connections.size() < 6) { // 如果连接池中的可用连接不满，则加如到连接池中，否则释放
				Connections.add(conn);
			} else {
				conn.close();
			}
		}
		checkCurrentConnections();
	}

	// 关闭连接时检查当前连接数目
	public static synchronized void checkCurrentConnections() {
		currentConnectionCount--;
	}

	// 关闭程序前的释放资源
	public static void releasConnections() throws SQLException {
		for (Connection conn : Connections) {
			conn.close();
		}
		System.out.println("release ! " + JDBCPool.Connections.size());
	}

	public static String InsertSQL(String userid , String time ,String ip){
		return "insert into info (userid , time , ip) values('" + userid + "' ,'" + time + "' ,'" + ip + "')";
	}
	
	// 生成符合格式的 INSERTSQL语句
	public static String InsertSQL(FailureInfo info) {
		return "insert into info (userid , time , ip) values('" + info.userid + "' ,'" + info.time + "' ,'" + info.ip + "')";
	}

	public static String DeleteSQL(String id ) {
		return "delete from info where id = '" + id + "'";
	}
	
	// 生成符合格式的 DELETESQL语句
	public static String DeleteSQL(FailureInfo info) {
		return "delete from info where id = '" + info.id + "'";
	}

	// 每次查找10个结果
	public static String QUERYSQL() {
		return "select * from info limit 50";
	}

}
