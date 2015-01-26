package appServerHandling;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import DataTranferObject.FileDTO;
import DataTranferObject.FileDataDTO;

public interface ServerInterf extends Remote {

	//util
	public ServerInterf getFreestServer() throws RemoteException;
	public String getServerIp() throws RemoteException;
	public ArrayList<String> getListServer() throws RemoteException;
	
	//thread manager
	public int getNumOfBusyThread() throws RemoteException;
	
	public void increaseBusyThread() throws RemoteException;
	
	public void decreaseBusyThread() throws RemoteException;
	
	// upload
	public int sendFileInfoToServer(FileDTO fileDetail) throws RemoteException;

	public void sendDataToServer(byte[] data, int offset, int length, int thread)
			throws RemoteException;

	public boolean finishUpload(FileDTO fileDetail, int thread)
			throws RemoteException;
	public int getLatestFileId() throws RemoteException;
	
	//transfer file to others server
	public boolean finishTransferOneServer(final FileDTO fileDetail, int thread)
			throws RemoteException;
	public int transferFile(FileDTO fileDetail,
			ArrayList<String> listIpServer) throws RemoteException;
	// download file
	public byte[] downloadFile(String fileTitle, String userName)
			throws RemoteException;
	
	public int getFileSizeToDownload(String fileTitle, String userName)
			throws RemoteException;
	
	public String getNameByTitle(String fileTitle) throws RemoteException;
	
	public FileDataDTO downloadFileByBytes(int currentByte, int thread) throws RemoteException;
	
	public int regisToDownload(String fileTitle, String userName)
			throws RemoteException;
	
	//delete file
	public boolean deleteFile(String userName, String fileTitle)
			throws RemoteException;
	
	public boolean deleteFileAndCallOthers(String userName, String fileTitle)
			throws RemoteException;
	
	// get list of file uploaded
	public ArrayList<FileDTO> getListOfFile(String userName)
			throws RemoteException;

	// login
	public String Login(String userName, String passWord)
			throws RemoteException;

	public String hello() throws RemoteException;

	// check file in another server
	public boolean checkFileExist(String fileName, String userName)
			throws RemoteException;
}
