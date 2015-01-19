package appServerHandling;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import DataTranferObject.FileDTO;

public interface ServerInterf extends Remote {

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
	// download file
	public byte[] downloadFile(String fileTitle, String userName)
			throws RemoteException;
	public String getNameByTitle(String fileTitle) throws RemoteException;
	// get list of file uploaded
	public HashMap<String, String> getListOfFile(String userName)
			throws RemoteException;

	// login
	public String Login(String userName, String passWord)
			throws RemoteException;

	public String hello() throws RemoteException;

	// check file in another server
	public boolean checkFileExist(String fileName, String userName)
			throws RemoteException;
}
