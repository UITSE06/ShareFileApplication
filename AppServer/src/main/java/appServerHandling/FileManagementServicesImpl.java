package appServerHandling;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import DataTranferObject.FileDTO;

public class FileManagementServicesImpl extends UnicastRemoteObject implements FileManagementServices {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectDatatbase connectDB = new ConnectDatatbase();

	public FileManagementServicesImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String UploadFile(String userName, String urlFile) throws RemoteException{
		
		return "";
	}
	public String DownloadFile(String IP, String urlFile)
			throws RemoteException {
		// TODO Auto-generated method stub
		return "";
	}
	
	public int InsertFileInfo(String userName, FileDTO fileDetail) throws RemoteException{
		String sqlInsertFile = "INSERT INTO `file` (`filename`, `user_id`, `file_state_id`, `urlfile`, `file_role_id`, `dateupload`, `size`, `checksum`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = connectDB.GetPrepareStatement(sqlInsertFile);
			//statement.setInt(1, fileDetail.getFileId());
			statement.setString(1, fileDetail.getFileName());
			statement.setInt(2, fileDetail.getUserId());
			statement.setInt(3, fileDetail.getFileStateId());
			statement.setString(4, fileDetail.getUrlFile());
			statement.setInt(5, fileDetail.getFileRoleId());
			statement.setObject(6, fileDetail.getDateUpload());
			statement.setLong(7, fileDetail.getSize());
			statement.setString(8, fileDetail.getCheckSum());
			if(!statement.execute()){// it's a insert sql, so this function return false if it execute success
				return 1;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		return 0;
	}

	public String Login(String userName, String passWord)
			throws RemoteException {
		try {
			PreparedStatement statement =
					connectDB.GetPrepareStatement("SELECT `username`, `password` FROM `user` WHERE `username` = ?");
			
			statement.setString(1, userName);			
			ResultSet rs = statement.executeQuery();
			if(rs.next()){
				if(passWord.equals(rs.getString("password"))){
					return rs.getString("username");
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
