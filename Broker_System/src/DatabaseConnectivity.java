import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnectivity {

	public  Connection connectToDatabase(String dbname) throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException{
		/*
		String url = "jdbc:mysql://localhost:3306/"; 
		String dbName = "netsec";
		String driver = "com.mysql.jdbc.Driver"; 
		String userName = "root";
		String password = "root";
//		String password = "mysql";
		Class.forName(driver).newInstance(); 
		Connection connection = DriverManager.getConnection(url+dbName,userName,password);
		*/
		
		Class.forName("org.sqlite.JDBC");
		Connection connection = DriverManager.getConnection("jdbc:sqlite:"+dbname);

		return connection;
	}
}
