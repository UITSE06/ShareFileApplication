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
	final int MAXTHREAD = 10;
	final String ipThisServer = "130.211.245.75";// server 4

	private static final long serialVersionUID = 1L;
	private Registry rmiRegistry;
	private ConnectDatatbase connectDB = new ConnectDatatbase();
	private HashMap<Integer, FileOutputStream> listThreadUpload;
	private ArrayList<Integer> listFreeThreadUpload;
	private int indexUpload = 0;

	/*
	 * private HashMap<Integer, BufferedInputStream> listThreadDownload; private
	 * ArrayList<Integer> listFreeThreadDownload; private int indexDownload = 0;
	 */

	public void start() throws Exception {
		rmiRegistry = LocateRegistry.createRegistry(1099);
		rmiRegistry.bind("server", this);
		System.out.println("Server started");
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

	public int sendFileInfoToServer(FileDTO fileDetail) throws RemoteException {
		try {
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
				listFreeThreadUpload.remove(0);// thread will busy, so remove it
												// from
												// free list
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
			statement.setLong(8, fileDetail.getSize());
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

	/*
	 * public int downloadFile(String fileName, String userName) { try { File
	 * file = new File(userName + "/" + fileName); if (indexDownload <
	 * MAXTHREAD) { indexDownload++; BufferedInputStream input = new
	 * BufferedInputStream( new FileInputStream(file));
	 * listThreadDownload.put(indexDownload, input); return indexDownload; }
	 * else if (listFreeThreadDownload.size() != 0) {// have a free // thread?
	 * int thread = listFreeThreadDownload.get(0);// get thread ID // thread
	 * will busy, so remove it from free list listFreeThreadDownload.remove(0);
	 * BufferedInputStream input = new BufferedInputStream( new
	 * FileInputStream(file)); listThreadDownload.put(thread, input); return
	 * thread; } } catch (Exception e) {
	 * System.out.println("Server download file: " + e.getMessage()); return -1;
	 * } return -1; }
	 */

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

	/*
	 * @SuppressWarnings("unused") public long sendFileLenToClient(String
	 * fileName, String userName) throws RemoteException { File file = new
	 * File(userName + "/" + fileName); if (file != null) { return
	 * file.length(); } else { return -1; } }
	 */

	/*
	 * public byte[] sendDataToClient(int thread) throws RemoteException { if
	 * (thread != -1) { try { byte buffer[] = new byte[65536];
	 * BufferedInputStream input = listThreadDownload.get(thread);
	 * input.read(buffer, 0, buffer.length); input.close(); return buffer; }
	 * catch (IOException e) { e.printStackTrace(); } } return null; }
	 */

	public boolean checkFileExist(String fileName, String userName)
			throws RemoteException {

		String sqlInsertFile = "SELECT `file_id` FROM `file` WHERE `username` = ? and `filename` = ?";
		try {
			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, userName);
			statement.setString(2, fileName);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {// had have file
				connectDB.Close();
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

	public int transferFileToOthers(FileDTO fileDetail) {
		// read all available server IP
		String sqlInsertFile = "SELECT `IP_server` FROM `server`";
		try {
			Statement statement = connectDB.GetStatement();
			ResultSet rs = statement.executeQuery(sqlInsertFile);
			Registry myRegis;
			ServerInterf serverI;
			int numServer = 0;
			ArrayList<String> listIpServer = new ArrayList<String>();
			while (rs.next()) {
				String serverIP = rs.getString(1);
				if (!ipThisServer.equals(serverIP)) {
					listIpServer.add(serverIP);
				}
			}
			connectDB.Close();
			for (String serverIP : listIpServer) {
				try {
					myRegis = LocateRegistry.getRegistry(serverIP);
					// search for server
					serverI = (ServerInterf) myRegis.lookup("server");
				} catch (Exception ex) {
					System.out.println("server: " + serverIP + " not found!");
					continue;
				}
				if (serverI != null) {
					// check file in another server
					// if this server haven't this file yet then transfer it
					if (!serverI.checkFileExist(fileDetail.getFileName(),
							fileDetail.getUserName())) {
						int thread = serverI.sendFileInfoToServer(fileDetail);

						// transfer file by bytes
						byte[] data = new byte[65536];// 1024*64 , 64 bytes
						int byteReads;
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
						if (serverI.finishUpload(fileDetail, thread)) {
							System.out.println("transfer file success + 1");
							numServer++;
						}
					}
				}
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
	public HashMap<String, String> getListOfFile(String userName)
			throws RemoteException {

		ArrayList<String> listFileOnDisk = new ArrayList<String>();
		HashMap<String, String> listTitleAndName = new HashMap<String, String>();
		// get list file of user from database
		String sqlInsertFile = "SELECT `file_title`, `filename` FROM `file` WHERE `username` = ?";
		try {

			if (Files.exists(Paths.get(userName))) {
				File folder = new File(userName + "/");
				File[] listOfFileTitle = folder.listFiles();
				for (File file : listOfFileTitle) {
					listFileOnDisk.add(file.getName());
				}
			}

			PreparedStatement statement = connectDB
					.GetPrepareStatement(sqlInsertFile);
			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String fileTitle = rs.getString(1);
				//if file exist on database and also exist on disk, then put it into hashmap to return
				if(listFileOnDisk.indexOf(fileTitle) > 0){
					listTitleAndName.put(fileTitle, rs.getString(2));
				}
			}
			return listTitleAndName;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connectDB.Close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// check if have any file of user in database, but don't have file on
		// disk, then remove file from return hashmap
		/*
		 * if (Files.exists(Paths.get(userName))) { File folder = new
		 * File(userName + "/"); File[] listOfFileTitle = folder.listFiles();
		 * for (File file : listOfFileTitle) { if
		 * (!listTitleAndName.containsKey(file.getName())) {
		 * listTitleAndName.remove(file.getName()); } } return listTitleAndName;
		 * }
		 */
		return null;
	}

	@Override
	public String hello() throws RemoteException {
		// TODO Auto-generated method stub
		return "Hi client!";
	}
}
