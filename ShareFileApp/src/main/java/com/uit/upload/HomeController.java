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

	public FileManagementServices getFmServiceInterface() {
		return fmServiceInterface;
	}

	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);

	private FileManagementServices fmServiceInterface;
	private String currentUserName = "";

	/*@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(HttpServletResponse response) {
		return "index";
	}*/

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String Login(HttpServletResponse response) {
		try {

			// fire to
			//Registry myRegis = LocateRegistry.getRegistry("127.0.0.1");
			Registry myRegis = LocateRegistry.getRegistry("104.155.199.62");
			//Registry myRegis = LocateRegistry.getRegistry("192.168.137.161");
			//Registry myRegis = LocateRegistry.getRegistry("104.46.63.42");		
			//Registry myRegis = LocateRegistry.getRegistry("54.169.102.72");
			// search for FileManagementServices
			fmServiceInterface = (FileManagementServices) myRegis
					.lookup("FileManagementServices");
			if (fmServiceInterface != null) {
				logger.info("Found server FileManagementServices!");
			} else {
				logger.info("Server FileManagementServices not found!");
			}
			//logger.info("home page");
			
			logger.info(fmServiceInterface.hello());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "login";
	}

	@RequestMapping(value = "/submitLogin", method = RequestMethod.POST)
	// @ResponseBody String message(HttpServletRequest request)
	public String LoginConfirm(HttpServletRequest request) {
		try {
			String username = request.getParameter("userName");
			String pass = request.getParameter("pass");
			if (username == null || pass == null) {
				return "login";
			}			currentUserName = fmServiceInterface.Login(username, pass);
			if (username.equals(currentUserName)) {
				return "index";
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "login";
	}

	/*@RequestMapping(value = "/multiPartFileSingle", method = RequestMethod.POST)
	public void uploadFile(HttpServletResponse response,
			@RequestParam(value = "file") MultipartFile file) {

		String back = "";
		try {
			if (!file.isEmpty()) {

				file.getBytes();
				back = "{successMessage : 'successMessage'}";
			} else {
				back = "{errorMessage : 'errorMessage'}";
			}
		} catch (Exception e) {
			e.printStackTrace();
			back = "{errorMessage : 'errorMessage'}";
		}

		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		try {// Changing to ISO, because standard AJAX response is in ISO and
				// our string is in UTF-8
			back = new String(back.getBytes("UTF-8"), "ISO8859_1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		// Write Json string in Json format
		AbstractHttpMessageConverter<String> stringHttpMessageConverter = new StringHttpMessageConverter();
		MediaType jsonMimeType = MediaType.APPLICATION_JSON;
		if (stringHttpMessageConverter.canWrite(String.class, jsonMimeType)) {
			try {
				stringHttpMessageConverter.write(back, jsonMimeType,
						new ServletServerHttpResponse(response));
			} catch (IOException m_Ioe) {
				m_Ioe.printStackTrace();
			} catch (HttpMessageNotWritableException p_Nwe) {
				p_Nwe.printStackTrace();
			}
		}
	}*/

	@RequestMapping(value = "/getFile", method = RequestMethod.POST)
	public final @ResponseBody String getFile() throws IOException,
			JSONException {

		String str = "";

		ArrayList<String> listFileName = fmServiceInterface.getListOfFile(currentUserName);
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
		byte[] filedata = fmServiceInterface.downloadFile(fileName);

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

		boolean isEmptyFile = file.isEmpty();

		// insert database
		FileDTO fileDetail = new FileDTO();
		fileDetail.setCheckSum("checkSum001");
		fileDetail.setDateUpload(Calendar.getInstance().getTime());
		fileDetail.setFileId(1);
		fileDetail.setFileName(file.getOriginalFilename());
		fileDetail.setFileRoleId(1);
		fileDetail.setSize(file.getSize());
		fileDetail.setFileStateId(1);
		fileDetail.setUrlFile("/path");
		fileDetail.setUserName(currentUserName);
		int rs = fmServiceInterface.InsertFileInfo(currentUserName, fileDetail);
		if (rs == 1) {
			logger.info("Insert Database success!" + rs);
		} else {
			logger.info("Insert Database fail!" + rs);
		}
		// process only if it's not empty
		if (!isEmptyFile) {
			try {
				int thread = fmServiceInterface.sendFileNameToServer(file.getOriginalFilename());

				byte[] data = new byte[8192];
				int byteReads;
				InputStream is = file.getInputStream();
				byteReads = is.read(data);
				while (byteReads != -1) {
					fmServiceInterface.sendDataToServer(data, 0, byteReads, thread);
					byteReads = is.read(data);
				}
				is.close();
				fmServiceInterface.finishUpload(file.getOriginalFilename(), thread);
				System.out.println("File upload success!");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("File upload failed!");
				// update status failToUpload
			}
			// update status uploaded
		}
	}
}
