package com.uit.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import DataTranferObject.FileDTO;
import DataTranferObject.FileDataDTO;
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
	private static HashMap<String, ServerInterf> listServerI = new HashMap<String, ServerInterf>();
	private static ArrayList<String> listServer = null;

	public static String checkServerIp(String username) {
		String ip = null;
		try {
			ip = listServerI.get(username).getServerIp();
			return ip;
		} catch (RemoteException e) {
			if (ip == null) {
				try {
					ServerInterf serverAlive = getAliveServer();
					ip = serverAlive.getServerIp();
					listServerI.put(username, serverAlive);
				} catch (RemoteException e1) {
					return "All servers were died";
				}
			}
			return ip;
		}
	}

	private static ServerInterf getAliveServer() {
		ServerInterf server;
		if (listServer == null) {
			listServer = new ArrayList<String>();
			listServer.add("104.155.199.62");
			listServer.add("107.167.180.164");
			listServer.add("104.155.210.44");
			listServer.add("104.155.204.35");
			listServer.add("127.0.0.1");
		}
		for (String serverIP : listServer) {
			Registry myRegis;
			try {
				myRegis = LocateRegistry.getRegistry(serverIP);
				server = (ServerInterf) myRegis.lookup("server");
			} catch (RemoteException e) {
				server = null;
			} catch (NotBoundException e) {
				server = null;
			}
			if (server != null) {
				return server;
			}
		}
		return null;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String Connect(HttpServletResponse response) {
		try {
			//Registry myRegis = LocateRegistry.getRegistry("127.0.0.1");
			// search for FileManagementServices
			serverI = getAliveServer();//(ServerInterf) myRegis.lookup("server");
			if (serverI != null) {
				listServer = serverI.getListServer();
				ServerInterf serverFreest = null;
				serverFreest = serverI.getFreestServer();
				if (serverFreest != null) {
					if (!serverI.getServerIp().equals(
							serverFreest.getServerIp())) {
						serverI = serverFreest;
					}
				}
				logger.info(serverI.hello());
				logger.info("Active Session "
						+ MySessionCounter.getActiveSessions());
			} else {
				logger.info("Server FileManagementServices not found!");
				return "error";
			}
		} catch (Exception e) {
			logger.info("Server FileManagementServices not found!");
			e.printStackTrace();
			return "error";
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
			String userSession = (String)session.getAttribute("userName");
			if(userSession != null){
				modelAndView.setViewName("index");
				//if(listServerI.get(userSession) == null)
				return modelAndView;
			}
			String currentUser = serverI.Login(username, pass);
			if(currentUser == null){
				modelAndView.setViewName("login");
				modelAndView.addObject("message",
						"Wrong user name or password!");
				return modelAndView;
			}
			if (username.equals(currentUser)) {
				if (!username.equals((String) session.getAttribute("userName"))) {
					session.setAttribute("userName", username);
					MySessionCounter.addActiveSessions();
				}
				logger.info("Active Session "
						+ MySessionCounter.getActiveSessions());
				modelAndView.setViewName("index");
				listServerI.put(username, serverI);
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
			String userName = (String) session.getAttribute("userName");
			if (userName == null) {
				modelAndView.setViewName("login");
				return modelAndView;
			} else {
				status.setComplete();
				session.removeAttribute("userName");
				MySessionCounter.removeActiveSessions();
				listServerI.remove(userName);
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
		String error = "[{\"name\":\"\",\"size\":\" KichThuoc \",\"date\":\"NgayThang\",\"title\":\"TieuDeFile\"}]";
		String userSession = (String) session.getAttribute("userName");
		if (userSession == null) {
			return error;
		}
		ServerInterf serverInte = listServerI.get(userSession);
		if (serverInte == null) {
			serverInte = HomeController.getAliveServer();
			if (serverInte == null) {
				return null;// all server was died
			}
		}
		ArrayList<FileDTO> listFileInfo = serverInte.getListOfFile(userSession);
		if (listFileInfo == null) {
			return error;
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
			if (file.getSize() >= 1024.0f) {
				String sizeString = String.format("%.2f",
						file.getSize() / 1024.0f);
				sb.append("\"size\":\"" + sizeString + " MB" + "\"");
			} else {
				String sizeString = String.format("%.2f", file.getSize());
				sb.append("\"size\":\"" + sizeString + " KB" + "\"");
			}
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
		ServerInterf serverInte = listServerI.get(userSession);
		if (serverInte == null) {
			serverInte = HomeController.getAliveServer();
			if (serverInte == null) {
				return null;// all server was died
			}
		}
		int fileLength = serverInte
				.getFileSizeToDownload(fileTitle, userSession);
		// get file name by file title
		String fileName = serverInte.getNameByTitle(fileTitle);
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.setContentLength(fileLength);

		OutputStream outStream = response.getOutputStream();
		final int MAXREAD = 8192;
		int byteIndex = 1;
		int currentByte = 0;
		int thread = serverInte.regisToDownload(fileTitle, userSession);
		if (thread == -1) {
			return null;
		}
		FileDataDTO fileData = serverInte.downloadFileByBytes(currentByte,
				thread);
		if (fileData == null) {
			return null;
		}
		while (fileData.getByteRead() != -1) {
			outStream.write(fileData.getData(), 0, fileData.getByteRead());
			currentByte = MAXREAD * byteIndex;
			byteIndex++;
			try {
				if(fileLength < MAXREAD){
					// send -1 to notify that finish download
					fileData = serverInte.downloadFileByBytes(-1, thread);
					break;
				}
				fileData = serverInte.downloadFileByBytes(currentByte, thread);
			} catch (Exception ex) {
				serverInte = getAliveServer();
				if (serverInte != null) {
					thread = serverInte.regisToDownload(fileTitle, userSession);
					fileData = serverInte.downloadFileByBytes(currentByte,
							thread);
				} else {
					break;
				}
			}
			if (fileData != null) {
				if (fileData.getByteRead() < MAXREAD) {// the last data
					outStream.write(fileData.getData(), 0,
							fileData.getByteRead());
					// send -1 to notify that finish download
					fileData = serverInte.downloadFileByBytes(-1, thread);
					break;
				}
			} else {
				break;
			}
		}
		outStream.close();
		return null;
	}

	@RequestMapping(value = "/downloadSharedFile", method = RequestMethod.GET)
	public ModelAndView downloadSharedFile(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		ModelAndView modelAndView = new ModelAndView();
		String fileTitle = request.getParameter("fileTitle");
		String userNameMD5 = request.getParameter("userNameMD5");

		// MyEncrypt en = new MyEncrypt();
		String userName = userNameMD5;
		if (userName == null) {
			modelAndView.setViewName("login");
			return modelAndView;
		}
		ServerInterf serverInte = HomeController.getAliveServer();
		if (serverInte == null) {
			return null;// all server was died
		}
		int fileLength = serverInte.getFileSizeToDownload(fileTitle, userName);
		// get file name by file title
		String fileName = serverInte.getNameByTitle(fileTitle);
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ fileName + "\"");
		response.setContentLength(fileLength);

		OutputStream outStream = response.getOutputStream();
		final int MAXREAD = 8192;
		int byteIndex = 1;
		int currentByte = 0;
		int thread = serverInte.regisToDownload(fileTitle, userName);
		if (thread == -1) {
			return null;
		}
		FileDataDTO fileData = serverInte.downloadFileByBytes(currentByte,
				thread);
		if (fileData == null) {
			return null;
		}
		while (fileData.getByteRead() != -1) {
			outStream.write(fileData.getData(), 0, fileData.getByteRead());
			currentByte = MAXREAD * byteIndex;
			byteIndex++;
			try {
				fileData = serverInte.downloadFileByBytes(currentByte, thread);
			} catch (Exception ex) {
				serverInte = getAliveServer();
				if (serverInte != null) {
					thread = serverInte.regisToDownload(fileTitle, userName);
					fileData = serverInte.downloadFileByBytes(currentByte,
							thread);
				} else {
					break;
				}
			}
			if (fileData != null) {
				if (fileData.getByteRead() < MAXREAD) {// the last data
					outStream.write(fileData.getData(), 0,
							fileData.getByteRead());
					// send -1 to notify that finish download
					fileData = serverInte.downloadFileByBytes(-1, thread);
					break;
				}
			} else {
				break;
			}
		}
		outStream.close();
		return null;
	}

	@RequestMapping(value = "/getLink", method = RequestMethod.GET)
	public final @ResponseBody String getShareLink(HttpSession session,
			HttpServletRequest request) throws IOException, JSONException {
		String error = "{\"file\":\"\",\"username\":\"AiDo\"}";
		String userSession = (String) session.getAttribute("userName");
		String fileTitle = request.getParameter("fileTitle");
		if (userSession == null || fileTitle == null) {
			return error;
		}

		String userNameMD5;
		// MyEncrypt en = new MyEncrypt();
		userNameMD5 = userSession;
		/*
		 * if (userNameMD5 == null) { return error; }
		 */
		String result = "{\"file\":\"" + fileTitle + "\",\"username\":\""
				+ userNameMD5 + "\"}";
		return result;
	}

	@RequestMapping(value = "/UploadFile", method = RequestMethod.POST)
	public ModelAndView upload(HttpServletResponse response,
			@RequestParam(value = "myfile") MultipartFile file,
			HttpSession session) throws ServletException, IOException {
		logger.info("begin upload");
		// process only when file is not empty
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
				ServerInterf serverInte = listServerI.get(userSession);
				int thread = serverInte.sendFileInfoToServer(fileDetail);
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
						serverInte.sendDataToServer(data, 0, byteReads, thread);
						byteReads = is.read(data);
					}
					is.close();
					// finish upload and update file's state to Uploaded
					if (serverInte.finishUpload(fileDetail, thread)) {
						System.out.println("File upload success!");
					}
				} else {
					// send message server busy
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

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public @ResponseBody String deleteFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) throws Exception {

		String message = "{\"error\": \"1\",\"message\": \"Error!!!\"}";
		String fileTitle = request.getParameter("fileTitle");

		// return a link to another server
		String userSession = (String) session.getAttribute("userName");
		if (userSession == null) {
			message = "{\"error\": \"1\",\"message\": \"Error!!!\"}";
			return message;
		}
		ServerInterf serverInte = listServerI.get(userSession);
		if (serverInte.deleteFileAndCallOthers(userSession, fileTitle)) {
			logger.info("Deleted file, get list again");
			message = "{\"error\": \"2\",\"message\": \"\"}";
		}
		return message;
	}
}
