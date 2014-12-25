package appServerHandling;

/*
 * @Hung Ngoc
 * Create interface fileManagementServices
 * Funtion: UploadFile, DownloadFile
*/
import java.rmi.Remote;
import java.rmi.RemoteException;

import DataTranferObject.*;
public interface FileManagementServices extends Remote {
	//public String UploadFile(String userName, String urlFile) throws RemoteException;
	public void sendFileNameToServer(String fileName) throws RemoteException;
	public void sendDataToServer(byte[] data, int offset, int length) throws RemoteException;
	public void finishUpload() throws RemoteException;
	public String DownloadFile(String IP, String urlFile)  throws RemoteException;
	public int InsertFileInfo(String userName, FileDTO fileDetail);
	public String Login(String userName, String passWord) throws RemoteException;
}
