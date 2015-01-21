package DataTranferObject;

import java.io.Serializable;
import java.util.Date;

public class FileDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int fileId;
	int fileStateId;
	String username;
	String fileName;
	String fileTitle;
	String urlFile;
	String fileRoleName;
	int FileRoleId;
	Date dateUpload;
	String dateUploadString;
	public String getDateUploadString() {
		return dateUploadString;
	}
	public void setDateUploadString(String dateUploadString) {
		this.dateUploadString = dateUploadString;
	}
	float size;
	String checkSum;
	
	public int getFileRoleId() {
		return FileRoleId;
	}
	public void setFileRoleId(int fileRoleId) {
		FileRoleId = fileRoleId;
	}
	
	public String getFileTitle() {
		return fileTitle;
	}
	public void setFileTitle(String fileTitle) {
		this.fileTitle = fileTitle;
	}
	
	public int getFileStateId() {
		return fileStateId;
	}
	public void setFileStateId(int fileStateId) {
		this.fileStateId = fileStateId;
	}
	public String getUserName() {
		return username;
	}
	public void setUserName(String userId) {
		this.username = userId;
	}
	public int getFileId() {
		return fileId;
	}
	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUrlFile() {
		return urlFile;
	}
	public void setUrlFile(String urlFile) {
		this.urlFile = urlFile;
	}
	public String getFileRoleName() {
		return fileRoleName;
	}
	public void setFileRoleName(String roleFile) {
		this.fileRoleName = roleFile;
	}
	public Date getDateUpload() {
		return dateUpload;
	}
	public void setDateUpload(Date dateUpload) {
		this.dateUpload = dateUpload;
	}
	public float getSize() {
		return size;
	}
	public void setSize(float size) {
		this.size = size;
	}
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
}
