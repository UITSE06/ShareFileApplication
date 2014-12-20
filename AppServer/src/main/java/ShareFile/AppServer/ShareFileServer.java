package ShareFile.AppServer;

import java.sql.SQLException;

/**
 * Hello world!
 *
 */
import appServerHandling.*;
import java.sql.Connection;

public class ShareFileServer 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ConnectDatatbase con = new ConnectDatatbase();
        try {
			Connection connect = con.CreateConnect();
			if(connect != null){
				System.out.println( "connected!" );
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
