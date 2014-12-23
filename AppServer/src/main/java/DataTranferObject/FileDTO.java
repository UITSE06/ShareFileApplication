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
	int userId;
	String fileName;
	String urlFile;
	int fileRoleId;
	Date dateUpload;
	long size;
	String checkSum;
	
	
	public int getFileStateId() {
		return fileStateId;
	}
	public void setFileStateId(int fileStateId) {
		this.fileStateId = fileStateId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
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
	public int getFileRoleId() {
		return fileRoleId;
	}
	public void setFileRoleId(int roleFile) {
		this.fileRoleId = roleFile;
	}
	public Date getDateUpload() {
		return dateUpload;
	}
	public void setDateUpload(Date dateUpload) {
		this.dateUpload = dateUpload;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getCheckSum() {
		return checkSum;
	}
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
}
