package ShareFile.AppServer;

import java.rmi.RemoteException;
import java.sql.SQLException;

import appServerHandling.*;

import java.sql.Connection;

public class ShareFileServer 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        try {
        	//System.out.println("\n---------------------------------------------");
    		//System.out.println("ContextInitialized Method has been Called......");
        	BoneCPConnection.configrureBoneCP();
        	//System.out.println("---------------------------------------------\n");
        	Connection connect = BoneCPConnection.getBoneCPConnection().getConnection();
			if(connect != null){
				System.out.println( "connected to MySQL database by BoneCP!" );
			}
			connect.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
        
        try {
			// if (System.getSecurityManager() == null)
		     //       System.setSecurityManager ( new RMISecurityManager() );
			 
			ServerImpl server = new ServerImpl();
			server.start();
			//Thread.sleep(5 * 60 * 1000); // run for 5 minutes
			//server.stop();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
