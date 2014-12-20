package appServerHandling;

/*
 * @Hung Ngoc
 * Create interface fileManagementServices
 * Funtion: UploadFile, DownloadFile
*/
import java.rmi.Remote;
import java.rmi.RemoteException;
//import DataTranferObject.*;
public interface FileManagementServices extends Remote {
	public String UploadFile(String userName, String urlFile) throws RemoteException;
	public String DownloadFile(String IP, String urlFile)  throws RemoteException;
	//public int InsertFileInfo(String userName, FileDetailDTO fileDetail);
	public String Login(String userName, String passWord) throws RemoteException;
}
