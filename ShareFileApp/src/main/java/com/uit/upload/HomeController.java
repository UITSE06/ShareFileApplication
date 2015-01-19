package com.uit.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Calendar;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import DataTranferObject.FileDTO;
import appServerHandling.*;

/**
 * Handles requests for the application home page. This comment is added by Anh
 * Quan who is very handsome boy edit on quanta branch
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);

	private ServerInterf serverI;

	// private MySessionCounter msc = new MySessionCounter();

	public ServerInterf getServerInterf() {
		return serverI;
	}

	private String currentUserName = "";

	@RequestMapping(value = "/index")
	public String home(HttpServletResponse response) {
		return "index";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String Login(HttpServletResponse response) {
		try {
			Registry myRegis = LocateRegistry.getRegistry("127.0.0.1");
			// Registry myRegis = LocateRegistry.getRegistry("104.155.199.62");
			// Registry myRegis = LocateRegistry.getRegistry("192.168.137.161");
			// search for FileManagementServices
			serverI = (ServerInterf) myRegis.lookup("server");
			if (serverI != null) {
				logger.info("Found server FileManagementServices!");
				logger.info(serverI.hello());
				logger.info("Active Session"
						+ MySessionCounter.getActiveSessions());
			} else {
				logger.info("Server FileManagementServices not found!");
			}
		} catch (Exception e) {
			logger.info("Server FileManagementServices not found!");
			e.printStackTrace();
		}
		return "login";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public ModelAndView Welcome(HttpServletRequest request, HttpSession session) {

		ModelAndView modelAndView = new ModelAndView();
		String userSession = (String) session.getAttribute("userName");
		if (userSession == null) {
			modelAndView.setViewName("login");
		} else {
			modelAndView.addObject("userName", userSession);
			modelAndView.setViewName("index");
		}

		return modelAndView;
	}

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public ModelAndView LoginConfirm(HttpServletRequest request) {

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		try {
			String username = request.getParameter("userName");

			String pass = request.getParameter("pass");
			if (username == null || pass == null) {
				modelAndView.setViewName("login");
				modelAndView.addObject("message",
						"Please input User name and Password!");
				return modelAndView;
			}
			currentUserName = serverI.Login(username, pass);
			if (username.equals(currentUserName)) {
				modelAndView.addObject("userName", username);
				modelAndView.setViewName("index");
				return modelAndView;
			} else {
				modelAndView.setViewName("login");
				modelAndView.addObject("message", "Login failed!");
				return modelAndView;
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return modelAndView;
	}

	@RequestMapping(value = "/clearSession", method = RequestMethod.POST)
	public ModelAndView ClearSession(HttpServletRequest request,
			HttpSession session, SessionStatus status) {
		try {
			status.setComplete();
			session.removeAttribute("userName");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("login");
		return modelAndView;
	}

	@RequestMapping(value = "/getFile", method = RequestMethod.POST)
	public final @ResponseBody String getFile() throws IOException,
			JSONException {

		String str = "";
		HashMap<String, String> listFileName = serverI
				.getListOfFile(currentUserName);
		str += "[";
		if (listFileName == null) {
			return "";
		}
		int index = 0;
		int size = listFileName.size();
		for (String key : listFileName.keySet()) {
			index++;
			StringBuffer sb = new StringBuffer();
			sb.append("{"); // Bắt đầu một đối tượng JSON là dấu mở ngoặc nhọn
			sb.append("\"id\":\"" + index + "\""); // {"id":"i + 1",
			sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
			sb.append("\"title\":\"" + key + "\"");
			sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
			sb.append("\"name\":\"" + listFileName.get(key) + "\"");
			// {"id":"i + 1","name":"listFileName.get(i)"
			if (index == size) {
				sb.append("}"); // Kết thúc một đối tượng JSON bự là dấu đóng
								// ngoặc
								// nhọn
				// {"id":"i + 1","name":"listFileName.get(i)"}
			} else {
				sb.append("},"); // Kết thúc một đối tượng JSON nhỏ là dấu đóng
									// ngoặc nhọn và 1 dấu phẩy
				// {"id":"i + 1","name":"listFileName.get(i)"},
			}
			str += sb.toString();
		}

		/*
		 * for (int i = 0; i < listFileName.size(); i++) { StringBuffer sb = new
		 * StringBuffer(); sb.append("{"); // Bắt đầu một đối tượng JSON là dấu
		 * mở ngoặc nhọn sb.append("\"id\":\"" + (i + 1) + "\""); //
		 * {"id":"i + 1", sb.append(","); // sau mỗi cặp key/value là một dấu
		 * phẩy sb.append("\"name\":\"" + listFileName.get(i) + "\""); //
		 * {"id":"i + 1","name":"listFileName.get(i)" if (i ==
		 * (listFileName.size() - 1)) { sb.append("}"); // Kết thúc một đối
		 * tượng JSON bự là dấu đóng ngoặc // nhọn //
		 * {"id":"i + 1","name":"listFileName.get(i)"} } else { sb.append("},");
		 * // Kết thúc một đối tượng JSON nhỏ là dấu đóng // ngoặc nhọn và 1 dấu
		 * phẩy // {"id":"i + 1","name":"listFileName.get(i)"}, } str +=
		 * sb.toString(); }
		 */
		str += "]";
		return str;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ModelAndView downloadFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String fileTitle = request.getParameter("fileTitle");

		// return a link to another server

		byte[] fileData = serverI.downloadFile(fileTitle, currentUserName);

		// get file name by file title
		String fileName = serverI.getNameByTitle(fileTitle);
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.setContentLength(fileData.length);

		FileCopyUtils.copy(fileData, response.getOutputStream());
		response.getOutputStream().flush();
		response.getOutputStream().close();
		return null;
		/*
		 * byte[] buffer = new byte[65536]; while (fileData != -1) {
		 * outStream.write(buffer, 0, bytesRead); }
		 * 
		 * inStream.close(); outStream.close();
		 */
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
				// not yet process it (update late)
				fileDetail.setCheckSum("checkSum001");
				fileDetail.setDateUpload(Calendar.getInstance().getTime());
				fileDetail.setFileId(1);
				fileDetail.setFileName(file.getOriginalFilename());
				int latestFileId = serverI.getLatestFileId();
				if (latestFileId <= 0) {
					System.out.println("get latest file id error!");
					return;
				}
				// get the extend of file, ex: music.mp3
				String fileExt = "."
						+ file.getOriginalFilename().split("\\.")[1];
				String fileTitle = currentUserName + latestFileId + fileExt;
				fileDetail.setFileTitle(fileTitle);
				// roleId = 1 , default role is private
				fileDetail.setFileRoleId(1);
				fileDetail.setSize(file.getSize());
				// stateId = 1 , file is uploading
				fileDetail.setFileStateId(1);
				fileDetail.setUrlFile("/" + currentUserName + "/");
				fileDetail.setUserName(currentUserName);

				int thread = serverI.sendFileInfoToServer(fileDetail);
				if (thread != -1) {

					/*
					 * Runnable run_Transfer = new Runnable() {
					 * 
					 * @Override public void run() { try {
					 * 
					 * } catch (IOException e) { e.printStackTrace(); } } };
					 * Thread thr1 = new Thread(run_Transfer); thr1.start();
					 * thr1.join();
					 */

					/*
					 * String tomcatHome = System.getProperty("catalina.home");
					 * String fileNameToCreate = tomcatHome + "/temp/" +
					 * file.getOriginalFilename();
					 * 
					 * File fileCreate = new File(fileNameToCreate);
					 * FileUtils.writeByteArrayToFile(fileCreate,
					 * file.getBytes());
					 */
					// upload file by bytes
					byte[] data = new byte[65536];// 1024*64 , 64 bytes
					int byteReads;
					InputStream is = file.getInputStream();
					byteReads = is.read(data);
					while (byteReads != -1) {
						serverI.sendDataToServer(data, 0, byteReads, thread);
						byteReads = is.read(data);
					}
					is.close();
					// finish upload and update file's state to Uploaded
					if (serverI.finishUpload(fileDetail, thread)) {
						System.out.println("File upload success!");
						return;
					}
				} else {
					System.out.println("File upload failed!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("File upload failed!");
				// update status failToUpload
			}
		}
	}

	public File multipartToFile(MultipartFile multipart)
			throws IllegalStateException, IOException {
		File convFile = new File(multipart.getOriginalFilename());
		multipart.transferTo(convFile);
		return convFile;
	}
}
