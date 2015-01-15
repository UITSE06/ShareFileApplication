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


public class ConnectDatatbase {
	
	Connection connection = null;          
	Statement statement = null;         	
	CallableStatement callableSta = null; 	
	PreparedStatement preState = null;
	ResultSet result = null;            
	
	public Connection CreateConnect() throws SQLException{
			Driver driver = new org.gjt.mm.mysql.Driver();
			DriverManager.registerDriver(driver);
			String conString = "jdbc:mysql://localhost:3306/db_sharefile";
			Properties info = new Properties();
			info.setProperty("characterEncoding", "utf8");
			info.setProperty("user", "root");
			info.setProperty("password", "");
			connection = DriverManager.getConnection(conString,info);
			return connection;
	}
	
	// Create statement to execute query
	protected Statement GetStatement() throws SQLException, Exception {
        // Check statement
        if (this.statement == null ? true : this.statement.isClosed()) {
            this.statement = this.CreateConnect().createStatement();
        }
        return this.statement;
    }


    // Create Prepared Statement to execute query with parameter
    protected PreparedStatement GetPrepareStatement(String sql) throws SQLException, Exception {
    	// Check statement
        if (this.preState == null ? true : this.preState.isClosed()) {
            this.preState = this.CreateConnect().prepareStatement(sql);
        }
        return this.preState;
    }

    // Execute query to get a result
    public ResultSet ExcuteQuery(String Query) throws Exception {
        try {
            this.result = GetStatement().executeQuery(Query);
        } catch (Exception e) {
            throw new Exception("Error: " + e.getMessage());
        }
        return this.result;
    }
    
    // Execute Insert, Update, Delete
    public int ExcuteUpdate(String Query) throws Exception {
        int res = Integer.MIN_VALUE;
        try {
            res = GetStatement().executeUpdate(Query);
        } catch (Exception e) {
            throw new Exception("Error: " + e.getMessage());
        } finally {
        	//Close connection
           this.Close();
        }
        return res;
    }

    public void Close() throws SQLException {
        // Check ResultSet
        if (this.result != null && this.result.isClosed()) {
            this.result.close();
            this.result = null;
        }
        
        if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
            this.connection = null;
        }
    }
}
