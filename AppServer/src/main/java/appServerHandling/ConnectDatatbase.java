package appServerHandling;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

//import com.mysql.jdbc.Driver;

public class ConnectDatatbase {
	
	Connection connection = null;          	// Khoi tao connector
	Statement statement = null;         	// Khoi tao statement thuc thi cau lenh sql
	CallableStatement callableSta = null; 	// Khoi tao callableStatement
	PreparedStatement preState = null;
	ResultSet result = null;            	// Khoi tao ResultSet de chua du lieu sau khi thuc thi cau lenh sql
	
	public Connection CreateConnect() throws SQLException{
			Driver driver = new org.gjt.mm.mysql.Driver();
			DriverManager.registerDriver(driver);
			String conString = "jdbc:mysql://localhost/db_sharefile";
			Properties info = new Properties();
			info.setProperty("characterEncoding", "utf8");
			info.setProperty("user", "root");
			info.setProperty("password", "");
			connection = DriverManager.getConnection(conString,info);
			return connection;
	}
	
	// Tao Statement de thuc thi cau Query
	protected Statement GetStatement() throws SQLException, Exception {
        // Kiem tra statement co null hoac da bi dong hay chua
        if (this.statement == null ? true : this.statement.isClosed()) {
            this.statement = this.CreateConnect().createStatement();
        }
        // Tra Statement ra ngoai.
        return this.statement;
    }


    // Tao Prepare Statement de thuc thi cau lenh SQL voi tham so
    protected PreparedStatement GetPrepareStatement(String sql) throws SQLException, Exception {
        // Kiem tra statement co null hoac da bi dong hay chua
        if (this.preState == null ? true : this.preState.isClosed()) {
            this.preState = this.CreateConnect().prepareStatement(sql);
        }
        // Tra Statement ra ngoai.
        return this.preState;
    }

    // Ham thuc thi cau lenh Select de lay du lieu
    public ResultSet ExcuteQuery(String Query) throws Exception {
        try {
            this.result = GetStatement().executeQuery(Query);
        } catch (Exception e) {
            throw new Exception("Error: " + e.getMessage());
        }
        return this.result;
    }
    
 // Ham thuc thi cac cau lenh Insert, Update, Delete binh thuong
    public int ExcuteUpdate(String Query) throws Exception {
        int res = Integer.MIN_VALUE;
        try {
            //Thuc thi cau lenh
            res = GetStatement().executeUpdate(Query);
        } catch (Exception e) {
            throw new Exception("Error: " + e.getMessage());
        } // Sau khi thuc hien cau lenh se tien hanh dong ket noi.
        finally {
           this.Close();
        }
        return res;
    }

    public void Close() throws SQLException {
        // Kiem tra ResultSet
        if (this.result != null && this.result.isClosed()) {
            this.result.close();
            this.result = null;
        }
        
        if (this.connection != null && this.connection.isClosed()) {
            this.connection.close();
            this.connection = null;
        }
    }
}
