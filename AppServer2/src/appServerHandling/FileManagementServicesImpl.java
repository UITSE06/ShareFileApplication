package appServerHandling;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import DataTranferObject.FileDTO;

public class FileManagementServicesImpl extends UnicastRemoteObject implements
		FileManagementServices {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConnectDatatbase connectDB = new ConnectDatatbase();
	private FileOutputStream fout = null;

	public FileManagementServicesImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	public void sendFileNameToServer(String fileName) throws RemoteException {
		try {
			if (Files.notExists(Paths.get("FileUploaded\\"))) {
				Files.createDirectories(Paths.get("FileUploaded\\"));
			}
			fout = new FileOutputStream("FileUploaded\\" + fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendDataToServer(byte[] data, int offset, int length)
			throws RemoteException {
		if (fout != null) {
			try {
				fout.write(data, offset, length);
				fout.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void finishUpload() throws RemoteException {
		if (fout != null) {
			try {
				fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] downloadFile(String fileName) {
		try {
			File file = new File(fileName);
			byte buffer[] = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(fileName));
			input.read(buffer, 0, buffer.length);
			input.close();
			return (buffer);
		} catch (Exception e) {
			System.out.println("FileManegementServicesImpl: " + e.getMessage());
			e.printStackTrace();
			return (null);
		}
	}

	public int InsertFileInfo(String userName, FileDTO fileDetail)
			throws RemoteException {
		String sqlInsertFile = "INSERT INTO `file` (`filename`, `user_id`, `file_state_id`, `urlfile`, `file_role_id`, `dateupload`, `size`, `checksum`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			// statement.setInt(1, fileDetail.getFileId());
			statement.setString(1, fileDetail.getFileName());
			statement.setInt(2, fileDetail.getUserId());
			statement.setInt(3, fileDetail.getFileStateId());
			statement.setString(4, fileDetail.getUrlFile());
			statement.setInt(5, fileDetail.getFileRoleId());
			statement.setObject(6, fileDetail.getDateUpload());
			statement.setLong(7, fileDetail.getSize());
			statement.setString(8, fileDetail.getCheckSum());
			if (!statement.execute()) {// it's a insert sql, so this function
										// return false if it execute success
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
			PreparedStatement statement = connectDB
					.GetPrepareStatement("SELECT `username`, `password` FROM `user` WHERE `username` = ?");

			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				if (passWord.equals(rs.getString("password"))) {
					return rs.getString("username");
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public ArrayList<String> getListOfFile() throws RemoteException {
		ArrayList<String> rs = new ArrayList<String>();
		if (Files.exists(Paths.get("FileUploaded\\"))) {
			File folder = new File("FileUploaded\\");
			File[] listOfFile = folder.listFiles();
			for (File file : listOfFile) {
				rs.add(file.getName());
			}
			return rs;
		}
		return null;
	}
}
