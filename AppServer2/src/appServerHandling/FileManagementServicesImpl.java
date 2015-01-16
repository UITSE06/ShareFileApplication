package appServerHandling;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import DataTranferObject.FileDTO;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FileManagementServicesImpl extends UnicastRemoteObject implements
		FileManagementServices {

	/**
	 * 
	 */
	final int MAXTHREAD = 10;

	private static final long serialVersionUID = 1L;
	private ConnectDatatbase connectDB = new ConnectDatatbase();
	// private FileOutputStream fout = null;
	private HashMap<Integer, FileOutputStream> listThread;
	private ArrayList<Integer> listFreeThread;
	private int index = 0;

	public FileManagementServicesImpl() throws RemoteException {
		super();
		listThread = new HashMap<Integer, FileOutputStream>();
		listFreeThread = new ArrayList<Integer>();

		// TODO Auto-generated constructor stub
	}
	
	////
	public OutputStream getOutputStream(File f) throws IOException {
	    return new RMIOutputStream(new RMIOutputStreamImpl(new 
	    FileOutputStream(f)));
	}

	public InputStream getInputStream(File f) throws IOException {
	    return new RMIInputStream(new RMIInputStreamImpl(new 
	    FileInputStream(f)));
	}
	////

	public int sendFileInfoToServer(FileDTO fileDetail) throws RemoteException {
		try {
			String userName = fileDetail.getUserName();
			String fileName = fileDetail.getFileName();
			if (Files.notExists(Paths.get(userName))) {
				Files.createDirectories(Paths.get(userName));
			}
			if (listThread.size() < MAXTHREAD) {
				index++;
				FileOutputStream fout =
						new FileOutputStream(userName + "/"	+ fileName);
				listThread.put(index, fout);
				
				//insert file info to database
				InsertFileInfo(fileDetail);
				return index;
			} else if (listFreeThread.size() != 0) {//have a free thread?
				int thread = listFreeThread.get(0);//get thread ID
				listFreeThread.remove(0);//thread will busy, so remove it from free list
				FileOutputStream fout = 
						new FileOutputStream(userName + "/"	+ fileName);
				listThread.put(thread, fout);
				
				//insert file info to database
				InsertFileInfo(fileDetail);
				return thread;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}

	public void sendDataToServer(byte[] data, int offset, int length, int thread)
			throws RemoteException {
		if (thread != -1) {
			try {
				FileOutputStream fout = listThread.get(thread);
				fout.write(data, offset, length);
				fout.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean finishUpload(String fileName, int thread) throws RemoteException {
		if (thread != -1) {
			try {
				listThread.get(thread).close();
				listFreeThread.add(thread);
				// update database, update status of file: uploaded
				String sqlInsertFile = "UPDATE `file` SET `file_state_id` = 2 WHERE `filename` = ?";
				try {
					PreparedStatement statement = connectDB
							.GetPrepareStatement(sqlInsertFile);
					statement.setString(1, fileName);
					// boolean rs = statement.execute();
					if (!statement.execute()) {
						// upload file to others server
						// transferFileToOthers();
						return true;
					}
					return false;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						connectDB.Close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public int transferFileToOthers() {
		// read all available server IP
		String sqlInsertFile = "SELECT `IP_server` FROM `server`";
		try {
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				// upload file to others server
				Registry myRegis = LocateRegistry.getRegistry(rs.getString(1));

				// search for FileManagementServices
				FileManagementServices fmServiceInterface = (FileManagementServices) myRegis
						.lookup("FileManagementServices");
				if (fmServiceInterface != null) {
					// return true;
				}
			}
			// return false;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 1;
	}

	public byte[] downloadFile(String fileName, String userName) {
		try {
			File file = new File(userName + "/" + fileName);
			byte buffer[] = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(userName + "/" + fileName));
			input.read(buffer, 0, buffer.length);
			input.close();
			return (buffer);
		} catch (Exception e) {
			System.out.println("Server download file: " + e.getMessage());
			return (null);
		}
	}

	public boolean InsertFileInfo(FileDTO fileDetail) {
		
		try {
			String sqlInsertFile = "INSERT INTO `file` (`filename`, `username`, `file_state_id`, "
					+ "`urlfile`, `file_role_id`, `dateupload`, `size`, `checksum`) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, fileDetail.getFileName());
			statement.setString(2, fileDetail.getUserName());
			statement.setInt(3, fileDetail.getFileStateId());
			statement.setString(4, fileDetail.getUrlFile());
			statement.setInt(5, fileDetail.getFileRoleId());
			statement.setObject(6, fileDetail.getDateUpload());
			statement.setLong(7, fileDetail.getSize());
			statement.setString(8, fileDetail.getCheckSum());
			if (!statement.execute()) {// it's a insert sql, so this function
										// return false if it execute success
				return true;
			}
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
		return false ;
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
	public ArrayList<String> getListOfFile(String userName)
			throws RemoteException {

		ArrayList<String> fileOfUser = new ArrayList<String>();

		// get list file of user from database
		String sqlInsertFile = "SELECT filename FROM `file` WHERE `username` = ?";
		try {
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				fileOfUser.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// check if have any file of user, if have, return it
		ArrayList<String> rs = new ArrayList<String>();
		if (Files.exists(Paths.get(userName))) {
			File folder = new File(userName + "/");
			File[] listOfFile = folder.listFiles();
			for (File file : listOfFile) {
				if (fileOfUser.contains(file.getName())) {
					rs.add(file.getName());
				}
			}
			return rs;
		}
		return null;
	}

	@Override
	public String hello() throws RemoteException {
		// TODO Auto-generated method stub
		return "Hi client!";
	}
}
