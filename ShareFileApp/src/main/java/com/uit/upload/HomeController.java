package com.uit.upload;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import DataTranferObject.FileDTO;
import appServerHandling.FileManagementServices;

/**
 * Handles requests for the application home page. This comment is added by Anh
 * Quan who is very handsome boy edit on quanta branch
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);

	private FileManagementServices fmServiceInterface;

	public FileManagementServices getFmServiceInterface() {
		return fmServiceInterface;
	}

	private String currentUserName = "";

	/*
	 * final public static int BUF_SIZE = 1024 * 64;
	 * 
	 * public static void copy(InputStream in, OutputStream out) throws
	 * IOException { System.out.println("using byte[] read/write"); byte[] b =
	 * new byte[BUF_SIZE]; int len; while ((len = in.read(b)) >= 0) {
	 * out.write(b, 0, len); } in.close(); out.close(); }
	 * 
	 * public static void upload(FileManagementServices server, MultipartFile
	 * src, File dest) throws IOException { copy (src.getInputStream(),
	 * server.getOutputStream(dest)); }
	 * 
	 * public static void download(FileManagementServices server, File src, File
	 * dest) throws IOException { copy (server.getInputStream(src), new
	 * FileOutputStream(dest)); }
	 */

	@RequestMapping(value = "/index")
	public String home(HttpServletResponse response) {
		return "index";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String Login(HttpServletResponse response) {
		try {

			// fire to
			// Registry myRegis = LocateRegistry.getRegistry("127.0.0.1");
			 Registry myRegis = LocateRegistry.getRegistry("104.155.199.62");
			// Registry myRegis = LocateRegistry.getRegistry("192.168.137.161");
			// Registry myRegis = LocateRegistry.getRegistry("104.46.63.42");
			// Registry myRegis = LocateRegistry.getRegistry("54.169.102.72");
			// search for FileManagementServices
			fmServiceInterface = (FileManagementServices) myRegis
					.lookup("FileManagementServices");
			if (fmServiceInterface != null) {
				logger.info("Found server FileManagementServices!");
			} else {
				logger.info("Server FileManagementServices not found!");
			}
			// logger.info("home page");

			logger.info(fmServiceInterface.hello());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "login";
	}

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public String LoginConfirm(HttpServletRequest request) {
		try {
			String username = request.getParameter("userName");
			String pass = request.getParameter("pass");
			if (username == null || pass == null) {
				return "login";
			}
			currentUserName = fmServiceInterface.Login(username, pass);
			if (username.equals(currentUserName)) {
				return "index";
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "login";
	}

	@RequestMapping(value = "/getFile", method = RequestMethod.POST)
	public final @ResponseBody String getFile() throws IOException,
			JSONException {

		String str = "";
		ArrayList<String> listFileName = fmServiceInterface
				.getListOfFile(currentUserName);
		str += "[";
		if (listFileName == null) {
			return "";
		}
		for (int i = 0; i < listFileName.size(); i++) {
			StringBuffer sb = new StringBuffer();
			sb.append("{"); // Bắt đầu một đối tượng JSON là dấu mở ngoặc nhọn
			sb.append("\"id\":\"" + (i + 1) + "\"");
			sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
			sb.append("\"name\":\"" + listFileName.get(i) + "\"");

			if (i == (listFileName.size() - 1)) {
				sb.append("}"); // Kết thúc một đối tượng JSON là dấu đóng ngoặc
								// nhọn
			} else {
				sb.append("},"); // Kết thúc một đối tượng JSON là dấu đóng
									// ngoặc nhọn
			}
			str += sb.toString();
		}
		str += "]";
		return str;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ModelAndView downloadFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String fileName = request.getParameter("fileName");
		byte[] filedata = fmServiceInterface.downloadFile(fileName, currentUserName);
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.setContentLength(filedata.length);

		FileCopyUtils.copy(filedata, response.getOutputStream());
		response.getOutputStream().flush();
		response.getOutputStream().close();
		return null;
	}

	@RequestMapping(value = "/UploadFile", method = RequestMethod.POST)
	public void upload(HttpServletResponse response,
			@RequestParam(value = "myfile") MultipartFile file)
			throws ServletException, IOException {

		// process only when file is not empty
		if (!file.isEmpty()) {
			try {
				// create object to pass
				FileDTO fileDetail = new FileDTO();
				fileDetail.setCheckSum("checkSum001"); //not yet process it (update late)
				fileDetail.setDateUpload(Calendar.getInstance().getTime());
				fileDetail.setFileId(1);
				fileDetail.setFileName(file.getOriginalFilename());
				fileDetail.setFileRoleId(1); //roleId = 1 , default role is private
				fileDetail.setSize(file.getSize());
				fileDetail.setFileStateId(1); //stateId = 1 , file is uploading
				fileDetail.setUrlFile("/" + currentUserName + "/");
				fileDetail.setUserName(currentUserName);

				int thread = fmServiceInterface
						.sendFileInfoToServer(fileDetail);
				if (thread != -1) {
					// upload file by bytes
					byte[] data = new byte[8192];// 1024*8 , 8 bytes
					int byteReads;
					InputStream is = file.getInputStream();
					byteReads = is.read(data);
					while (byteReads != -1) {
						fmServiceInterface.sendDataToServer(data, 0, byteReads,
								thread);
						byteReads = is.read(data);
					}
					is.close();
					//finish upload and update file's state to Uploaded
					fmServiceInterface.finishUpload(file.getOriginalFilename(),
							thread);
					System.out.println("File upload success!");
				} else {
					System.out.println("Send file infomation failed!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("File upload failed!");
				// update status failToUpload
			}
		}
	}
}
