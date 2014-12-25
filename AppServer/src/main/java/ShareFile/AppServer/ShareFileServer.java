package ShareFile.AppServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.SQLException;
import appServerHandling.*;
import java.sql.Connection;

public class ShareFileServer 
{
	private void startServer(){
		try{
			
			//create registry on port 1993
			Registry regis = LocateRegistry.createRegistry(1993);
			
			//create a new service
			regis.rebind("FileManagementServices", new FileManagementServicesImpl());
			System.out.println("Server started!");
			
			FileManagementServicesImpl demo = new FileManagementServicesImpl();
			String rs = demo.Login("quanta", "anhquan");
			System.out.println(rs);
			
			///
			/*
			//insert database
			FileDTO fileDetail = new FileDTO();
			fileDetail.setCheckSum("checkSum001");
			fileDetail.setDateUpload(Calendar.getInstance().getTime());
			fileDetail.setFileId(1);
			fileDetail.setFileName("fileNameDemo");
			fileDetail.setFileRoleId(1);
			fileDetail.setSize(123456);
			fileDetail.setFileStateId(1);
			fileDetail.setUrlFile("path" + "\\SaveFile\\");
			fileDetail.setUserId(1);
			int rs1 = demo.InsertFileInfo("AnhQuan", fileDetail);
			if(rs1 == 1){
				System.out.println("Insert Database success!" + rs1);
			} else {
				System.out.println("Insert Database fail!" + rs1);
			}
			*/
			///
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ConnectDatatbase con = new ConnectDatatbase();
        try {
			Connection connect = con.CreateConnect();
			if(connect != null){
				System.out.println( "connected to MySQL database!" );
			}
			connect.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        ShareFileServer sfServer = new ShareFileServer();
        sfServer.startServer();
    }
}
