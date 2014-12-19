package com.uit.upload;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Handles requests for the application home page.
 * This comment is added by Anh Quan who is very handsome boy
 * edit on quanta branch
 */
@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate);

		return "index";
	}
	
	@RequestMapping(value = "/multiPartFileSingle", method = RequestMethod.POST)
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
	}
	
	@RequestMapping(value = "/getFile", method = RequestMethod.POST)
	public final @ResponseBody String getFile() throws IOException, JSONException {
		
		String str = "";
		
		String catalinaHome = System.getProperty("catalina.home");
		String path = catalinaHome + "\\SaveFile\\";
		
		File folder = new File(path);
		File[] listOfFile = folder.listFiles();
		
		str += "[";
		
		for (int i = 0; i < listOfFile.length; i++) {
			StringBuffer sb = new StringBuffer();
			sb.append("{"); // Bắt đầu một đối tượng JSON là dấu mở ngoặc nhọn
			 
	        sb.append("\"id\":\"" + i + "\""); 
	        sb.append(","); // sau mỗi cặp key/value là một dấu phẩy
	        sb.append("\"name\":\"" + listOfFile[i].getName() + "\"");
	 
	        if (i == listOfFile.length - 1) {
	        	sb.append("}"); // Kết thúc một đối tượng JSON là dấu đóng ngoặc nhọn
			} else {
				sb.append("},"); // Kết thúc một đối tượng JSON là dấu đóng ngoặc nhọn
			}
	        
	        
	        str += sb.toString();
		}
		str += "]";
		System.out.println(str);
		return str;
		
		
	}
	
	@RequestMapping(value = "/UploadFile", method = RequestMethod.POST)
	public void upload(HttpServletResponse response,
			@RequestParam(value = "myfile") MultipartFile file) throws ServletException, IOException {

		boolean isMultipart = file.isEmpty();

		String pp = System.getProperty("catalina.home");
		///
		// port, ip, 
		//final String UPLOAD_DIRECTORY = "F:/TestBackup/";
		
		String fileNameToCreate = pp + "\\SaveFile\\" + file.getOriginalFilename();
		//insert database
		
		// process only if its multipart content
		if (!isMultipart) {
			try {				
				File fileCreate = new File(fileNameToCreate);
				FileUtils.writeByteArrayToFile(fileCreate, file.getBytes());				
			} catch (Exception e) {
				System.out.println("File upload failed");
				//update status failToUpload
			}
			//update status uploaded
		}
	}
}
