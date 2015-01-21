package com.uit.upload;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import DataTranferObject.FileDTO;
import appServerHandling.*;

/**
 * Handles requests for the application home page. This comment is added by Anh
 * Quan who is very handsome boy edit on quanta branch
 */
@Controller
@SessionAttributes("userName")
public class HomeController {

	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);

	private ServerInterf serverI;

	public ServerInterf getServerInterf() {
		return serverI;
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
				logger.info(serverI.hello());
				logger.info("Active Session "
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
		modelAndView.setViewName("index");
		return modelAndView;
	}

	@RequestMapping(value = "/home", method = RequestMethod.POST)
	public ModelAndView LoginConfirm(HttpServletRequest request,
			HttpSession session) {

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
			String currentUser = serverI.Login(username, pass);
			if (username.equals(currentUser)) {
				if(!username.equals((String)session.getAttribute("userName"))){
					session.setAttribute("userName", username);
					MySessionCounter.addActiveSessions();
				}				
				logger.info("Active Session "
						+ MySessionCounter.getActiveSessions());
				modelAndView.setViewName("index");
				return modelAndView;

			} else {
				modelAndView.setViewName("login");
				modelAndView.addObject("message",
						"Wrong user name or password!");
				return modelAndView;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return modelAndView;
	}

	@RequestMapping(value = "/clearSession", method = RequestMethod.POST)
	public ModelAndView ClearSession(HttpServletRequest request,
			HttpSession session, SessionStatus status) {
		ModelAndView modelAndView = new ModelAndView();
		try {
			if ((String) session.getAttribute("userName") == null) {
				modelAndView.setViewName("login");
				return modelAndView;
			} else {
				status.setComplete();
				session.removeAttribute("userName");
				MySessionCounter.removeActiveSessions();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		modelAndView.setViewName("login");
		return modelAndView;
	}

	@RequestMapping(value = "/getFile", method = RequestMethod.POST)
	public final @ResponseBody String getFile(HttpSession session)
			throws IOException, JSONException {

		String str = "";
		String userSession = (String) session.getAttribute("userName");
		if (userSession == null) {
			return "";
		}
		ArrayList<FileDTO> listFileInfo = serverI
				.getListOfFile(userSession);
		if (listFileInfo == null) {
			return "";
		}
		str += "[";
		int index = 0;
		int size = listFileInfo.size();
		for (FileDTO file : listFileInfo) {
			index++;
			StringBuffer sb = new StringBuffer();
			sb.append("{"); // Bắt đầu một đối tượng JSON là dấu mở ngoặc nhọn
			sb.append("\"name\":\"" + file.getFileName() + "\""); // {"id":"i + 1",
			sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
			String sizeString = String.format("%.2f", file.getSize());
			sb.append("\"size\":\"" + sizeString + " MB" + "\"");
			sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
			sb.append("\"date\":\"" + file.getDateUploadString() + "\"");
			sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
			sb.append("\"title\":\"" + file.getFileTitle() + "\"");
			// {"id":"i + 1","name":"listFileName.get(i)"
			if (index == size) {
				sb.append("}"); // Kết thúc một đối tượng JSON là dấu đóng
								// ngoặc nhọn
				// {"id":"i + 1","name":"listFileName.get(i)"}
			} else {
				sb.append("},"); // Kết thúc một đối tượng JSON thêm dấu đóng
									// ngoặc nhọn và 1 dấu phẩy
				// {"id":"i + 1","name":"listFileName.get(i)"},
			}
			str += sb.toString();
		}
		str += "]";
		return str;
	}

	@RequestMapping(value = "/download", method = RequestMethod.GET)
	public ModelAndView downloadFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws Exception {

		ModelAndView modelAndView = new ModelAndView();
		String fileTitle = request.getParameter("fileTitle");

		// return a link to another server
		String userSession = (String) session.getAttribute("userName");
		if (userSession == null) {
			modelAndView.setViewName("login");
			modelAndView.addObject("message", "Time out, Please log in again!");
			return modelAndView;
		}
		//start busy thread download
		serverI.increaseBusyThread();
		byte[] fileData = serverI.downloadFile(fileTitle, userSession);

		// get file name by file title
		String fileName = serverI.getNameByTitle(fileTitle);
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.setContentLength(fileData.length);
		FileCopyUtils.copy(fileData, response.getOutputStream());
		//stop busy thread
		serverI.decreaseBusyThread();
		response.getOutputStream().flush();
		response.getOutputStream().close();
		return null;
	}

	@RequestMapping(value = "/UploadFile", method = RequestMethod.POST)
	public ModelAndView upload(HttpServletResponse response,
			MultipartHttpServletRequest request,
			HttpSession session) throws ServletException, IOException {
		logger.info("begin upload");
		// process only when file is not empty
		
		MultipartFile file = request.getFile("myfile");
		if (!file.isEmpty()) {
			
			try {
				ModelAndView modelAndView = new ModelAndView();
				String userSession = (String) session.getAttribute("userName");
				if (userSession == null) {
					modelAndView.setViewName("login");
					modelAndView.addObject("message",
							"Time out, Please log in again!");
					return modelAndView;
				}
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
					return null;
				}
				// get the extend of file, ex: music.mp3
				String splitString[] = file.getOriginalFilename().split("\\.");
				String fileExt = "." + splitString[splitString.length - 1];
				String fileTitle = userSession + latestFileId + fileExt;
				fileDetail.setFileTitle(fileTitle);
				// roleId = 1 , default role is private
				fileDetail.setFileRoleId(1);
				fileDetail.setSize(file.getSize());
				// stateId = 1 , file is uploading
				fileDetail.setFileStateId(1);
				fileDetail.setUrlFile("/" + userSession + "/");
				fileDetail.setUserName(userSession);

				int thread = serverI.sendFileInfoToServer(fileDetail);
				if (thread != -1) {
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
					}
				} else {
					//send message server busy
					System.out.println("Server is busy!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("File upload failed!");
				// update status failToUpload
			}
		}
		return null;
	}
	
	@RequestMapping(value = "/deleteFile", method = RequestMethod.GET)
	public ModelAndView deleteFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws Exception {

		ModelAndView modelAndView = new ModelAndView();
		String fileTitle = request.getParameter("fileTitle");

		// return a link to another server
		String userSession = (String) session.getAttribute("userName");
		if (userSession == null) {
			modelAndView.setViewName("login");
			modelAndView.addObject("message", "Time out, Please log in again!");
			return modelAndView;
		}	
		if(serverI.deleteFile(userSession, fileTitle)){
			
		}
		return null;
	}
}
