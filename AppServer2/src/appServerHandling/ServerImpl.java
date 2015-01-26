package appServerHandling;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import DataTranferObject.FileDTO;
import DataTranferObject.FileDataDTO;

public class ServerImpl extends UnicastRemoteObject implements ServerInterf {

	final int MAXTHREAD = 100;
	private String ipThisServer = "";

	private static final long serialVersionUID = 1L;
	private Registry rmiRegistry;
	private HashMap<Integer, FileOutputStream> listThreadUpload;
	private ArrayList<Integer> listFreeThreadUpload;
	private HashMap<Integer, Path> listPathDownload;
	private ArrayList<Integer> listFreeThreadDownload;
	private int numOfBusyThread;
	private int indexUpload = 0;
	private int indexDownload = 0;
	private com.jolbox.bonecp.BoneCP connPool;

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

	public ServerImpl() throws RemoteException, SQLException {
		super();
		try {
			listThreadUpload = new HashMap<Integer, FileOutputStream>();
			listFreeThreadUpload = new ArrayList<Integer>();
			listPathDownload = new HashMap<Integer, Path>();
			listFreeThreadDownload = new ArrayList<Integer>();
			numOfBusyThread = 0;

			// Configure BoneCP
			BoneCPConnection.configrureBoneCP();
			// Get connection from BoneCP.
			connPool = BoneCPConnection.getBoneCPConnection();
			// call thread run always to synch file
			threadAlwayRunTransfer();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
					return thread;
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
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = connPool.getConnection();
			if (conn != null) {
				String sqlInsertFile = "INSERT INTO `file` (`filename`, `file_title`, `username`, `file_state_id`, "
						+ "`urlfile`, `file_role_id`, `dateupload`, `size`, `checksum`) "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
				// 2 seconds ""

				statement = conn.prepareStatement(sqlInsertFile);
				statement.setString(1, fileDetail.getFileName());
				statement.setString(2, fileDetail.getFileTitle());
				statement.setString(3, fileDetail.getUserName());
				statement.setInt(4, fileDetail.getFileStateId());
				statement.setString(5, fileDetail.getUrlFile());
				statement.setInt(6, fileDetail.getFileRoleId());
				statement.setObject(7, fileDetail.getDateUpload());
				statement.setFloat(8, fileDetail.getSize());
				statement.setString(9, fileDetail.getCheckSum());
				if (!statement.execute()) {// it's a insert sql, so this
											// function
											// return false if it execute
											// success
					System.out.println("insert file info success!");
					return true;
				}
			}
		} catch (Exception e) {
			System.out.println("insert file info fail!");
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public int getLatestFileId() throws RemoteException {
		Connection conn = null;
		Statement statement = null;
		try {
			String sql = "SELECT MAX(file_id) FROM `file`";
			conn = connPool.getConnection();
			statement = conn.createStatement();
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
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
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
			Connection conn = null;
			PreparedStatement statement = null;
			try {
				listThreadUpload.get(thread).close();
				listFreeThreadUpload.add(thread);
				decreaseBusyThread();
				System.out.println("upload file done!");
				// update database, update status of file: uploaded
				String sql = "UPDATE `file` SET `file_state_id` = 2 WHERE `filename` = ?";
				conn = connPool.getConnection();
				statement = conn.prepareStatement(sql);
				statement.setString(1, fileDetail.getFileName());
				if (!statement.execute()) {
					// upload file to others server
					Runnable run_Transfer = new Runnable() {

						@Override
						public void run() {
							int count = transferFileToOthers(fileDetail);
							System.out.println("Transfered file to " + count
									+ " server(s)");
						}
					};
					Thread thr1 = new Thread(run_Transfer);
					thr1.start();
					System.out.println("Called transfer thread!");
					return true;
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (statement != null) {
						if (!statement.isClosed()) {
							statement.close();
						}
					}
					if (conn != null) {
						if (!conn.isClosed()) {
							conn.close();
						}
					}
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

	public int getFileSizeToDownload(String fileTitle, String userName)
			throws RemoteException {
		try {
			File file = new File(userName + "/" + fileTitle);
			return (int) file.length();
		} catch (Exception e) {
			System.err
					.println("Server download file stream: " + e.getMessage());
			return -1;
		}
	}

	public int regisToDownload(String fileTitle, String userName)
			throws RemoteException {
		try {
			if (listFreeThreadDownload.size() < MAXTHREAD) {
				increaseBusyThread();
				indexDownload++;
				File file = new File(userName + "/" + fileTitle);
				String dir = file.getAbsolutePath();
				Path path = Paths.get(dir);
				listPathDownload.put(indexDownload, path);
				return indexDownload;
			} else if (listFreeThreadDownload.size() != 0) {
				increaseBusyThread();
				int thread = listFreeThreadDownload.get(0);
				listFreeThreadDownload.remove(0);
				File file = new File(userName + "/" + fileTitle);
				String dir = file.getAbsolutePath();
				Path path = Paths.get(dir);
				listPathDownload.put(thread, path);
				return thread;
			}
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public FileDataDTO downloadFileByBytes(int currentByte, int thread)
			throws RemoteException {
		try {
			if (currentByte == -1) {// finish download
				listFreeThreadDownload.add(thread);
				decreaseBusyThread();
				return null;
			}
			Path path = listPathDownload.get(thread);
			FileDataDTO fileData = new FileDataDTO();
			byte[] temp = new byte[8192];
			byte[] arrFile = Files.readAllBytes(path);
			int byteRead = 8192;
			if (currentByte > arrFile.length - 8192) {
				byteRead = arrFile.length - currentByte;
			}
			System.arraycopy(arrFile, currentByte, temp, 0, byteRead);
			fileData.setData(temp);
			fileData.setCurrentByte(currentByte + 8192);
			fileData.setByteRead(byteRead);
			return fileData;
		} catch (Exception e) {
			listFreeThreadDownload.add(thread);
			decreaseBusyThread();
			e.printStackTrace();
			return null;
		}
	}

	public boolean deleteFileAndCallOthers(final String userName,
			final String fileTitle) throws RemoteException {
		try {
			this.deleteFile(userName, fileTitle);
			Runnable runDelete = new Runnable() {

				@Override
				public void run() {
					int count = deleteFileOnOtherServers(userName, fileTitle);
					System.out.println("Deleted file on " + count
							+ " server(s)");
				}
			};
			Thread thr1 = new Thread(runDelete);
			thr1.start();

			return true;
		} catch (Exception e) {
			System.out.println("Server delete file: " + e.getMessage());
		}
		return false;
	}

	public boolean deleteFile(String userName, String fileTitle)
			throws RemoteException {
		try {
			File file = new File(userName + "/" + fileTitle);
			if (file != null) {
				if (!file.delete()) {
					return false;
				}
			}
			// delete file from database
			Connection conn = null;
			PreparedStatement statement = null;
			String sql = "DELETE FROM `file` WHERE `file_title` = ?";
			try {
				conn = connPool.getConnection();
				statement = conn.prepareStatement(sql);
				statement.setString(1, fileTitle);
				if (!statement.execute()) {// if delete success, it will
											// return false, so check
											// false
					System.out.println("Deleted file");
					return true;
				}
			} catch (Exception ex) {
				System.out.println("Can't delete from database");
				ex.printStackTrace();
			} finally {
				try {
					if (statement != null) {
						if (!statement.isClosed()) {
							statement.close();
						}
					}
					if (conn != null) {
						if (!conn.isClosed()) {
							conn.close();
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			System.out.println("Server delete file: " + e.getMessage());
		}
		return false;
	}

	public int deleteFileOnOtherServers(String userName, String fileTitle) {
		ArrayList<String> listServerIP;
		try {
			listServerIP = this.getListServer();

			if (listServerIP == null) {
				return 0;
			}
			listServerIP.remove(ipThisServer);
			int count = 0;
			Registry myRegis;
			ServerInterf serverI;
			for (String ipServer : listServerIP) {
				try {
					myRegis = LocateRegistry.getRegistry(ipServer);
					// search for server
					serverI = (ServerInterf) myRegis.lookup("server");
				} catch (Exception ex) {
					System.out.println("Server " + ipServer
							+ " not found to delete!");
					logFileToExecuteLate(fileTitle, ipServer, 0);
					continue;
				}
				if (serverI != null) {
					if (serverI.deleteFile(userName, fileTitle)) {
						count++;
					}
				}
			}
			return count;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String getNameByTitle(String fileTitle) throws RemoteException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			String sql = "SELECT `filename` FROM `file` WHERE `file_title` = ?";
			conn = connPool.getConnection();
			statement = conn.prepareStatement(sql);
			statement.setString(1, fileTitle);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public boolean checkFileExist(String fileTitle, String userName)
			throws RemoteException {
		String sql = "SELECT `size` FROM `file` WHERE `username` = ? and `file_title` = ?";
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = connPool.getConnection();
			statement = conn.prepareStatement(sql);
			statement.setString(1, userName);
			statement.setString(2, fileTitle);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {// had have file in database

				// check file on disk
				File temp = new File(userName + "/" + fileTitle);
				if (temp != null) {
					if (temp.exists() && temp.length() == rs.getLong(1)) {
						return true;
					}
				}
			}
		} catch (Exception ex) {
			return false;
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public ArrayList<String> getListServer() throws RemoteException {
		ArrayList<String> result = new ArrayList<String>();
		String sql = "SELECT `IP_server` FROM `server`";
		Connection conn = null;
		Statement statement = null;
		try {
			conn = connPool.getConnection();
			statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Can't read list server form database");
		return null;
	}

	public ServerInterf getFreestServer() throws RemoteException {
		ArrayList<String> listIpServer = this.getListServer();
		if (listIpServer == null) {
			return null;
		}
		Registry myRegis;
		ServerInterf serverI;
		int minThreadServer = this.getNumOfBusyThread();
		ServerInterf freeServer = null;
		for (String serverIP : listIpServer) {
			try {
				myRegis = LocateRegistry.getRegistry(serverIP);
				serverI = (ServerInterf) myRegis.lookup("server");
			} catch (Exception ex) {
				serverI = null;
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
		return freeServer;
	}

	public int transferFileToOthers(FileDTO fileDetail) {
		try {
			int numServer = 0;
			ArrayList<String> listIpServer = getListServer();
			if (listIpServer == null) {
				return 0;
			}
			listIpServer.remove(ipThisServer);
			ServerInterf freeServer = getFreestServer();
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
								.println("transfered file to freest server success!");
						numServer++;
					}
				}
				listIpServer.remove(freeServer.getServerIp());
				// call this free server to transfer file to others server
				numServer += freeServer.transferFile(fileDetail, listIpServer);
			} else {
				// there is no server freer than this server, so transfer file
				// to all others
				numServer += transferFile(fileDetail, listIpServer);
			}
			return numServer;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public int transferFile(FileDTO fileDetail, ArrayList<String> listIpServer)
			throws RemoteException {
		int count = 0;
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
					System.out.println("Server " + serverIP
							+ " not found to transfer!");
					// call thread to check and transfer file as soon as find
					// this server
					logFileToExecuteLate(fileDetail.getFileTitle(), serverIP, 1);
					// transferFileToDiedServer(fileDetail, serverIP);
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
							count++;
						}
					}
				}
			}
			return count;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	private void threadAlwayRunTransfer() {
		Runnable run_Transfer = new Runnable() {

			@Override
			public synchronized void run() {
				while (true) {
					// check database have any row
					final int SLEEPTIME = 1;
					String sql = "SELECT COUNT(`log_id`) FROM `log_transfer`";
					Connection connect = null;
					Statement statement = null;
					try {
						connect = connPool.getConnection();
						statement = connect.createStatement();
						ResultSet rs = statement.executeQuery(sql);
						if (rs.next()) {
							int count = rs.getInt(1);
							if (count <= 0) {
								// do nothing
								System.out.println("There is no log.");
							} else {// have file in log_tranfer need to execute
								ArrayList<String> listServer = getListServerNeedToTransfer();
								Registry myRegis;
								ServerInterf serverI = null;
								for (String server : listServer) {
									try {
										myRegis = LocateRegistry
												.getRegistry(server);
										serverI = (ServerInterf) myRegis
												.lookup("server");
									} catch (Exception ex) {
										System.out.println("Server " + server
												+ " still die.");
										serverI = null;
									}
									if (serverI != null) {// if server had alive
										// execute transfer file process
										executeTransferFileInLogTransfer(
												server, serverI);
										// -------------------------------------------------------------------------------------------------------------------------------//
										// execute for delete process
										executeDeleteFileInLogTransfer(server,
												serverI);
									}
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						try {
							if (statement != null) {
								if (!statement.isClosed()) {
									statement.close();
								}
							}
							if (connect != null) {
								if (!connect.isClosed()) {
									connect.close();
								}
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
					// after find all server, sleep for 1 minute
					try {
						TimeUnit.MINUTES.sleep(SLEEPTIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		Thread thr1 = new Thread(run_Transfer);
		thr1.start();
	}

	// Transfer file to another server
	private void executeTransferFileInLogTransfer(String server,
			ServerInterf serverI) {
		String sql = "SELECT `file_title` FROM `log_transfer` WHERE `server_ip` = ? AND `action` = 1";
		Connection conn = null;
		PreparedStatement preState = null;
		ResultSet rsTitle = null;
		try {
			conn = connPool.getConnection();
			preState = conn.prepareStatement(sql);
			preState.setString(1, server);
			rsTitle = preState.executeQuery();
			FileDTO fileDTO;
			ArrayList<String> listFileTitle = new ArrayList<String>();
			while (rsTitle.next()) {
				listFileTitle.add(rsTitle.getString(1));
			}
			for (String fileTitle : listFileTitle) {
				sql = "SELECT `filename`,`username`,`file_state_id`,`urlfile`,`file_role_id`,`dateupload`,`size`,`checksum` FROM `file` WHERE `file_title` = ?";
				PreparedStatement preStateFile = conn.prepareStatement(sql);
				preStateFile.setString(1, fileTitle);
				ResultSet fileInfo = preStateFile.executeQuery();
				if (fileInfo.next()) {
					fileDTO = new FileDTO();
					fileDTO.setFileTitle(fileTitle);
					fileDTO.setFileName(fileInfo.getString(1));
					fileDTO.setUserName(fileInfo.getString(2));
					fileDTO.setFileStateId(fileInfo.getInt(3));
					fileDTO.setUrlFile(fileInfo.getString(4));
					fileDTO.setFileRoleId(fileInfo.getInt(5));
					fileDTO.setDateUpload(fileInfo.getTimestamp(6));
					fileDTO.setSize(fileInfo.getFloat(7));
					fileDTO.setCheckSum(fileInfo.getString(8));
					if (transferFileToJustAliveServer(serverI, fileDTO, server)) {
						// transfer file success then delete log
						sql = "DELETE FROM `log_transfer` WHERE `file_title` = ? and `server_ip` = ? and `action` = 1 ";
						try {
							PreparedStatement preStaDelete = conn
									.prepareStatement(sql);
							preStaDelete.setString(1, fileTitle);
							preStaDelete.setString(2, server);
							boolean rsDelete = preStaDelete.execute();
							if (rsDelete) {
								System.out.println("Delete log success!");
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		} catch (Exception ex) {

		} finally {
			try {
				if (rsTitle != null) {
					if (!rsTitle.isClosed()) {
						rsTitle.close();
					}
				}
				if (preState != null) {
					if (!preState.isClosed()) {
						preState.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	// delete file on log transfer.
	private void executeDeleteFileInLogTransfer(String server,
			ServerInterf serverI) {
		String sql = "SELECT `file_title` FROM `log_transfer` WHERE `server_ip` = ? AND `action` = 0";
		Connection conn = null;
		PreparedStatement preState = null;
		ResultSet rsTitle = null;
		// delete older statement
		// connectDB.CloseStatement();
		try {
			conn = connPool.getConnection();
			preState = conn.prepareStatement(sql);
			preState.setString(1, server);
			rsTitle = preState.executeQuery();
			ArrayList<String> listFileTitle = new ArrayList<String>();
			while (rsTitle.next()) {
				listFileTitle.add(rsTitle.getString(1));
			}
			// connectDB.CloseStatement();
			for (String fileTitle : listFileTitle) {
				sql = "SELECT `username` FROM `file` WHERE `file_title` = ?";
				PreparedStatement preStateFile = conn.prepareStatement(sql);
				preStateFile.setString(1, fileTitle);
				ResultSet userName = preStateFile.executeQuery();
				if (userName.next()) {
					if (serverI.deleteFile(userName.getString(1), fileTitle)) {
						// transfer delete success
						// then delete log
						sql = "DELETE FROM `log_transfer` WHERE `file_title` = ? and `server_ip` = ? and `action` = 0 ";
						try {
							PreparedStatement preStaDelete = conn
									.prepareStatement(sql);
							preStaDelete.setString(1, fileTitle);
							preStaDelete.setString(2, server);
							boolean rsDelete = preStaDelete.execute();
							if (rsDelete) {
								System.out.println("Delete log success!");
							}
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (rsTitle != null) {
					if (!rsTitle.isClosed()) {
						rsTitle.close();
					}
				}
				if (preState != null) {
					if (!preState.isClosed()) {
						preState.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<String> getListServerNeedToTransfer() {
		ArrayList<String> result = new ArrayList<String>();
		String sql = "SELECT DISTINCT `server_ip` FROM `log_transfer` WHERE `source_server` = ?";
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = connPool.getConnection();
			statement = conn.prepareStatement(sql);
			statement.setString(1, ipThisServer);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Can't read list server form log_transfer");
		return null;
	}

	// action = 1 to transfer, 0 to delete
	private void logFileToExecuteLate(String fileTitle, String diedServer,
			int action) {
		String sql = "INSERT INTO `log_transfer`(`source_server`, `file_title`, `server_ip`, `action`) VALUES(?, ?, ?, ?)";
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = connPool.getConnection();
			statement = conn.prepareStatement(sql);
			statement.setString(1, ipThisServer);
			statement.setString(2, fileTitle);
			statement.setString(3, diedServer);
			statement.setInt(4, action);
			boolean rs = statement.execute();
			if (!rs) {
				System.out.println("Logged file to execute late!");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean transferFileToJustAliveServer(ServerInterf serverI,
			final FileDTO fileDetail, final String diedServer) {
		try {
			if (serverI != null) {
				byte[] data = new byte[65536];// 1024*64 , 64 bytes
				int byteReads;
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

					// finish upload and update file's state to
					// Uploaded
					if (serverI.finishTransferOneServer(fileDetail, thread)) {
						System.out
								.println("Transferred success to a server just alive: "
										+ diedServer);
						return true;
					}
				}
				// server had file
				return true;
			}
		} catch (FileNotFoundException e) {
			System.out.println("File Not Found Exception");
		} catch (IOException e) {
			System.out.println("IO Exception");
		}
		return false;
	}

	public boolean finishTransferOneServer(final FileDTO fileDetail, int thread)
			throws RemoteException {
		Connection conn = null;
		PreparedStatement statement = null;
		if (thread != -1) {
			try {
				listThreadUpload.get(thread).close();
				listFreeThreadUpload.add(thread);
				decreaseBusyThread();
				System.out.println("Be transfered file done!");
				// update database, update status of file: uploaded
				String sql = "UPDATE `file` SET `file_state_id` = 2 WHERE `filename` = ?";
				conn = connPool.getConnection();
				statement = conn.prepareStatement(sql);
				statement.setString(1, fileDetail.getFileName());
				if (!statement.execute()) {
					return true;
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (statement != null) {
						if (!statement.isClosed()) {
							statement.close();
						}
					}
					if (conn != null) {
						if (!conn.isClosed()) {
							conn.close();
						}
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public String Login(String userName, String passWord)
			throws RemoteException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = connPool.getConnection();
			statement = conn
					.prepareStatement("SELECT `username`, `password` FROM `user` WHERE `username` = ?");

			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				if (passWord.equals(rs.getString("password"))) {
					return rs.getString("username");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
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
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			if (Files.exists(Paths.get(userName))) {
				File folder = new File(userName + "/");
				File[] listOfFileTitle = folder.listFiles();
				for (File file : listOfFileTitle) {
					listFileOnDisk.add(file.getName());
				}
			}
			// get list file of user from database
			String sql = "SELECT `file_title`, `filename`, `size`, `dateupload` FROM `file` WHERE `username` = ? and `file_state_id` = 2";
			conn = connPool.getConnection();
			statement = conn.prepareStatement(sql);
			statement.setString(1, userName);
			ResultSet rs = statement.executeQuery();
			if (rs == null) {
				return null;
			}
			FileDTO fileInfo;
			while (rs.next()) {
				fileInfo = new FileDTO();
				fileInfo.setFileTitle(rs.getString(1));
				fileInfo.setFileName(rs.getString(2));
				fileInfo.setSize((float) rs.getLong(3) / 1024);// get KB
				fileInfo.setDateUploadString("" + rs.getTime(4) + " "
						+ rs.getDate(4));
				// if file exist on database and also exist on disk, then put it
				// into list file info
				if (listFileOnDisk.indexOf(fileInfo.getFileTitle()) >= 0) {
					listFileInfo.add(fileInfo);
				}
			}
			return listFileInfo;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					if (!statement.isClosed()) {
						statement.close();
					}
				}
				if (conn != null) {
					if (!conn.isClosed()) {
						conn.close();
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String hello() throws RemoteException {
		return "Server " + this.getServerIp() + " say: Hi client!";
	}
}
