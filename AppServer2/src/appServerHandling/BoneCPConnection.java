package appServerHandling;

import com.jolbox.bonecp.BoneCPConfig;

public class BoneCPConnection {
	
	private static com.jolbox.bonecp.BoneCP boneCPConnection = null;
	
	public static com.jolbox.bonecp.BoneCP getBoneCPConnection(){
		return boneCPConnection;
	}
	
	public static void setBoneCPConnection(com.jolbox.bonecp.BoneCP connectionPool){
		BoneCPConnection.boneCPConnection = connectionPool;
	}
	
	public static void configrureBoneCP(){
		
		try {
			
			/*
			 * load the database driver (make sure this is in your classpath!)
			 */
			Class.forName("com.mysql.jdbc.Driver");

			/*
			 * setup the connection pool
			 */
			BoneCPConfig boneConfig = new BoneCPConfig();
			boneConfig.setJdbcUrl("jdbc:mysql://localhost:3306/db_sharefile");
			boneConfig.setUser("root");
			boneConfig.setPassword("anhquan1234");
			boneConfig.setMinConnectionsPerPartition(5);
			boneConfig.setMaxConnectionsPerPartition(20);
			boneConfig.setPartitionCount(1);
			boneCPConnection = new com.jolbox.bonecp.BoneCP(boneConfig);
			
			setBoneCPConnection(boneCPConnection);
			//System.out.println("contextInitialized.....Connection Pooling is configured");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void shutdownBoneCPConnectionPool()
	{
		try
		{
			com.jolbox.bonecp.BoneCP connectionPool = BoneCPConnection.getBoneCPConnection();
			if (connectionPool != null)
			{
				/*
				 * This method must be called only once when the application
				 * stops. you don't need to call it every time when you get a
				 * connection from the Connection Pool
				 */
				connectionPool.shutdown();
				System.out.println("contextDestroyed.....Connection Pooling shut downed!");
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
