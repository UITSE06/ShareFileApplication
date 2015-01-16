package appServerHandling;

/*
 * @Hung Ngoc
 * Create interface fileManagementServices
 * Function: UploadFile, DownloadFile
*/

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import DataTranferObject.*;
public interface FileManagementServices extends Remote {
	//upload file
	public int sendFileInfoToServer(FileDTO fileDetail) throws RemoteException;
	public void sendDataToServer(byte[] data, int offset, int length, int thread) throws RemoteException;
	public boolean finishUpload(String fileName, int thread) throws RemoteException;
	
	//download file
	public byte[] downloadFile(String fileName, String userName) throws RemoteException;
	
	//get list of file uploaded
	public ArrayList<String> getListOfFile(String userName) throws RemoteException;	
	
	//login
	public String Login(String userName, String passWord) throws RemoteException;
	
	public String hello() throws RemoteException;
	
	public InputStream getInputStream(File f) throws IOException;
	public OutputStream getOutputStream(File f) throws IOException;
}
