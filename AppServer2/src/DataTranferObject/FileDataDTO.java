/**
 * 
 */
package DataTranferObject;

import java.io.Serializable;

/**
 * @author John
 *
 */
public class FileDataDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] data;
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public int getByteRead() {
		return byteRead;
	}
	public void setByteRead(int byteRead) {
		this.byteRead = byteRead;
	}
	public int getCurrentByte() {
		return currentByte;
	}
	public void setCurrentByte(int currentByte) {
		this.currentByte = currentByte;
	}
	private int byteRead;
	private int currentByte;
}
