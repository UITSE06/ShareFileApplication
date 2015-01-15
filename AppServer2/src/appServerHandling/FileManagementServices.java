package appServerHandling;

/*
 * @Hung Ngoc
 * Create interface fileManagementServices
 * Function: UploadFile, DownloadFile
*/

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import DataTranferObject.*;
public interface FileManagementServices extends Remote {
	//upload file
	public int sendFileNameToServer(String fileName) throws RemoteException;
	public void sendDataToServer(byte[] data, int offset, int length, int thread) throws RemoteException;
	public boolean finishUpload(String fileName, int thread) throws RemoteException;
	//download file
	public byte[] downloadFile(String fileName) throws RemoteException;
	//get list of file uploaded
	public ArrayList<String> getListOfFile(String userName) throws RemoteException;	
	//insert file info to database
	public int InsertFileInfo(String userName, FileDTO fileDetail) throws RemoteException;
	//login
	public String Login(String userName, String passWord) throws RemoteException;
	
	public String hello() throws RemoteException;
}
