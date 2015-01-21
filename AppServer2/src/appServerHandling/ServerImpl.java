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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import DataTranferObject.FileDTO;

public class ServerImpl extends UnicastRemoteObject implements ServerInterf {

	/**
	 * 
	 */
	final int MAXTHREAD = 20;
	private String ipThisServer = "";

	private static final long serialVersionUID = 1L;
	private Registry rmiRegistry;
	private ConnectDatatbase connectDB = new ConnectDatatbase();
	private HashMap<Integer, FileOutputStream> listThreadUpload;
	private ArrayList<Integer> listFreeThreadUpload;
	private int numOfBusyThread;
	private int indexUpload = 0;

	public void start() throws Exception {
		rmiRegistry = LocateRegistry.createRegistry(1099);
		rmiRegistry.bind("server", this);
		System.out.println("Server started");
		try {
			ipThisServer = CheckIpTool.getIp();
		} catch (Exception ex) {
			System.out.println("Can't check server IP!");
		}
		System.out.println(ipThisServer);
	}

	public void stop() throws Exception {
		rmiRegistry.unbind("server");
		unexportObject(this, true);
		unexportObject(rmiRegistry, true);
		System.out.println("Server stopped");
	}

	public ServerImpl() throws RemoteException {
		super();
		listThreadUpload = new HashMap<Integer, FileOutputStream>();
		listFreeThreadUpload = new ArrayList<Integer>();
	}

	public int getNumOfBusyThread() throws RemoteException {
		return numOfBusyThread;
	}

	public String getServerIp() throws RemoteException {
		return ipThisServer;
	}

	public void increaseBusyThread() throws RemoteException {
		numOfBusyThread++;
	}

	public void decreaseBusyThread() throws RemoteException {
		if (numOfBusyThread > 0) {
			numOfBusyThread--;
		}
	}

	public int sendFileInfoToServer(FileDTO fileDetail) throws RemoteException {
		try {
			increaseBusyThread();
			String userName = fileDetail.getUserName();

			if (Files.notExists(Paths.get(userName))) {
				Files.createDirectories(Paths.get(userName));
			}
			if (listFreeThreadUpload.size() < MAXTHREAD) {
				indexUpload++;
				FileOutputStream fout = new FileOutputStream(userName + "/"
						+ fileDetail.getFileTitle());
				listThreadUpload.put(indexUpload, fout);

				// insert file info to database
				if (InsertFileInfo(fileDetail)) {
					return indexUpload;
				}
			} else if (listFreeThreadUpload.size() != 0) {// have a free thread?
				int thread = listFreeThreadUpload.get(0);// get thread ID
				// thread will busy, so remove it from free list
				listFreeThreadUpload.remove(0);
				FileOutputStream fout = new FileOutputStream(userName + "/"
						+ fileDetail.getFileTitle());
				listThreadUpload.put(thread, fout);

				// insert file info to database
				if (InsertFileInfo(fileDetail)) {
					return indexUpload;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public boolean InsertFileInfo(FileDTO fileDetail) {

		try {
			String sqlInsertFile = "INSERT INTO `file` (`filename`, `file_title`, `username`, `file_state_id`, "
					+ "`urlfile`, `file_role_id`, `dateupload`, `size`, `checksum`) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, fileDetail.getFileName());
			statement.setString(2, fileDetail.getFileTitle());
			statement.setString(3, fileDetail.getUserName());
			statement.setInt(4, fileDetail.getFileStateId());
			statement.setString(5, fileDetail.getUrlFile());
			statement.setInt(6, fileDetail.getFileRoleId());
			statement.setObject(7, fileDetail.getDateUpload());
			statement.setFloat(8, fileDetail.getSize());
			statement.setString(9, fileDetail.getCheckSum());
			if (!statement.execute()) {// it's a insert sql, so this function
										// return false if it execute success
				System.out.println("insert file info success!");
				return true;
			}
		} catch (Exception e) {
			System.out.println("insert file info fail!");
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public int getLatestFileId() throws RemoteException {
		try {
			String sql = "SELECT MAX(file_id) FROM `file`";
			Statement statement = connectDB.GetStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs.next()) {
				int fileId = rs.getInt(1);
				return fileId + 1;
			} else {
				return 1;// the first file
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
		return -1;
	}

	public void sendDataToServer(byte[] data, int offset, int length, int thread)
			throws RemoteException {
		if (thread != -1) {
			try {
				FileOutputStream fout = listThreadUpload.get(thread);
				fout.write(data, offset, length);
				fout.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean finishUpload(final FileDTO fileDetail, int thread)
			throws RemoteException {
		if (thread != -1) {
			try {
				listThreadUpload.get(thread).close();
				listFreeThreadUpload.add(thread);
				decreaseBusyThread();
				System.out.println("upload file done!");
				// update database, update status of file: uploaded
				String sql = "UPDATE `file` SET `file_state_id` = 2 WHERE `filename` = ?";
				PreparedStatement statement = connectDB
						.GetPrepareStatement(sql);
				statement.setString(1, fileDetail.getFileName());
				// boolean rs = statement.execute();
				if (!statement.execute()) {
					// upload file to others server
					System.out.println("begin thread!");
					Runnable run_Transfer = new Runnable() {

						@Override
						public void run() {
							transferFileToOthers(fileDetail);
						}
					};
					Thread thr1 = new Thread(run_Transfer);
					thr1.start();
					// thr1.join();
					System.out.println("called thread!");
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
		}
		return false;
	}

	public byte[] downloadFile(String fileTitle, String userName)
			throws RemoteException {
		try {
			File file = new File(userName + "/" + fileTitle);
			byte buffer[] = new byte[(int) file.length()];
			BufferedInputStream input = new BufferedInputStream(
					new FileInputStream(userName + "/" + fileTitle));
			input.read(buffer, 0, buffer.length);
			input.close();
			return (buffer);
		} catch (Exception e) {
			System.out.println("Server download file: " + e.getMessage());
			return (null);
		}
	}

	public boolean deleteFile(String userName, String fileTitle)
			throws RemoteException {
		try {
			File file = new File(userName + "/" + fileTitle);
			if (file != null) {
				return file.delete();
			}
			// return (buffer);
		} catch (Exception e) {
			System.out.println("Server delete file: " + e.getMessage());
		}
		return false;
	}

	public String getNameByTitle(String fileTitle) throws RemoteException {
		try {
			String sql = "SELECT `filename` FROM `file` WHERE `file_title` = ?";
			PreparedStatement statement = connectDB.GetPrepareStatement(sql);
			statement.setString(1, fileTitle);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
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
		return null;
	}

	public boolean checkFileExist(String fileName, String userName)
			throws RemoteException {

		String sqlInsertFile = "SELECT `file_id` FROM `file` WHERE `username` = ? and `filename` = ?";
		try {
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, userName);
			statement.setString(2, fileName);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {// had have file in database
				connectDB.Close();
				// check file on disk
				return true;
			}
		} catch (Exception ex) {
			return false;
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public ArrayList<String> getListServer() throws RemoteException {
		ArrayList<String> result = new ArrayList<String>();
		String sqlInsertFile = "SELECT `IP_server` FROM `server`";
		try {
			Statement statement = connectDB.GetStatement();
			ResultSet rs = statement.executeQuery(sqlInsertFile);
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Can't read list server form database");
		return null;
	}

	public int transferFileToOthers(FileDTO fileDetail) {
		// read all available server IP
		//String sqlInsertFile = "SELECT `IP_server` FROM `server`";
		try {
			/*Statement statement = connectDB.GetStatement();
			ResultSet rs = statement.executeQuery(sqlInsertFile);*/
			Registry myRegis;
			ServerInterf serverI;
			int numServer = 0;
			ArrayList<String> listIpServer = getListServer();
			/*while (rs.next()) {
				String serverIP = rs.getString(1);
				if (!ipThisServer.equals(serverIP)) {
					listIpServer.add(serverIP);
				}
			}*/
			if(listIpServer == null){
				return 0;
			}
			listIpServer.remove(ipThisServer);
			ServerInterf freeServer = null;
			int minThreadServer = this.getNumOfBusyThread();
			for (String serverIP : listIpServer) {
				try {
					myRegis = LocateRegistry.getRegistry(serverIP);
					// search for server
					serverI = (ServerInterf) myRegis.lookup("server");
				} catch (Exception ex) {
					// System.out.println("server: " + serverIP +
					// " not found!");
					// listIpServer.remove(serverIP);
					continue;
				}
				if (serverI != null) {
					int otherServerThread = serverI.getNumOfBusyThread();
					if (otherServerThread < minThreadServer) {
						freeServer = serverI;
						minThreadServer = otherServerThread;
					}
				}
			}
			if (freeServer != null) {// transfer file to this free server
				// check file in another server
				// if this server haven't this file yet then transfer it
				if (!freeServer.checkFileExist(fileDetail.getFileName(),
						fileDetail.getUserName())) {
					int thread = freeServer.sendFileInfoToServer(fileDetail);

					// transfer file by bytes
					byte[] data = new byte[65536];// 1024*64 , 64 bytes
					int byteReads;
					FileInputStream fis = new FileInputStream(new File(
							fileDetail.getUserName() + "/"
									+ fileDetail.getFileTitle()));
					byteReads = fis.read(data);
					while (byteReads != -1) {
						freeServer.sendDataToServer(data, 0, byteReads, thread);
						byteReads = fis.read(data);
					}
					fis.close();

					// finish upload and update file's state to Uploaded
					if (freeServer.finishTransferOneServer(fileDetail, thread)) {
						System.out
								.println("transfered file to another free server success!");
						numServer++;
					}
				}
				listIpServer.remove(freeServer.getServerIp());
				// call this free server to transfer file to others server
				freeServer.transferFile(fileDetail, listIpServer);
			} else {
				// there is no server freer than this server, so transfer file
				// to all others
				transferFile(fileDetail, listIpServer);
			}

			return numServer;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public void transferFile(FileDTO fileDetail, ArrayList<String> listIpServer)
			throws RemoteException {

		Registry myRegis;
		ServerInterf serverI;
		byte[] data = new byte[65536];// 1024*64 , 64 bytes
		int byteReads;
		try {
			for (String serverIP : listIpServer) {
				try {
					myRegis = LocateRegistry.getRegistry(serverIP);
					// search for server
					serverI = (ServerInterf) myRegis.lookup("server");
				} catch (Exception ex) {
					System.out.println("Server " + serverIP + " not found!");
					continue;
				}
				if (serverI != null) {
					// check file in another server
					// if this server haven't this file yet then transfer it
					if (!serverI.checkFileExist(fileDetail.getFileName(),
							fileDetail.getUserName())) {
						int thread = serverI.sendFileInfoToServer(fileDetail);

						// transfer file by bytes
						FileInputStream fis = new FileInputStream(new File(
								fileDetail.getUserName() + "/"
										+ fileDetail.getFileTitle()));
						byteReads = fis.read(data);
						while (byteReads != -1) {
							serverI.sendDataToServer(data, 0, byteReads, thread);
							byteReads = fis.read(data);
						}
						fis.close();

						// finish upload and update file's state to Uploaded
						if (serverI.finishTransferOneServer(fileDetail, thread)) {
							System.out.println("transfer file success + 1");
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean finishTransferOneServer(final FileDTO fileDetail, int thread)
			throws RemoteException {
		if (thread != -1) {
			try {
				listThreadUpload.get(thread).close();
				listFreeThreadUpload.add(thread);
				decreaseBusyThread();
				System.out.println("upload file done!");
				// update database, update status of file: uploaded
				String sql = "UPDATE `file` SET `file_state_id` = 2 WHERE `filename` = ?";
				PreparedStatement statement = connectDB
						.GetPrepareStatement(sql);
				statement.setString(1, fileDetail.getFileName());
				if (!statement.execute()) {
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
		}
		return false;
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
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public ArrayList<FileDTO> getListOfFile(String userName)
			throws RemoteException {
		ArrayList<String> listFileOnDisk = new ArrayList<String>();
		ArrayList<FileDTO> listFileInfo = new ArrayList<FileDTO>();
		try {
			if (Files.exists(Paths.get(userName))) {
				File folder = new File(userName + "/");
				File[] listOfFileTitle = folder.listFiles();
				for (File file : listOfFileTitle) {
					listFileOnDisk.add(file.getName());
				}
			}
			// get list file of user from database
			String sqlInsertFile = "SELECT `file_title`, `filename`, `size`, `dateupload` FROM `file` WHERE `username` = ?";
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			FileDTO fileInfo;// = new FileDTO();
			while (rs.next()) {
				fileInfo = new FileDTO();
				fileInfo.setFileTitle(rs.getString(1));
				fileInfo.setFileName(rs.getString(2));
				fileInfo.setSize((float) rs.getLong(3) / 1048576);// get size by
																	// MB
				fileInfo.setDateUpload(rs.getTime(4));
				fileInfo.setDateUploadString("" + rs.getTime(4) + " "
						+ rs.getDate(4));
				// if file exist on database and also exist on disk, then put it
				// into hashmap to return
				if (listFileOnDisk.indexOf(fileInfo.getFileTitle()) >= 0) {
					listFileInfo.add(fileInfo);
				}
			}
			return listFileInfo;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String hello() throws RemoteException {
		return "Hi client!";
	}
}
