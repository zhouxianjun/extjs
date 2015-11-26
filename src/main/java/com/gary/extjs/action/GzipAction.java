package com.gary.extjs.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gary.compress.YUI;
import com.gary.compress.gzip.Gzip;
import com.gary.extjs.ExtjsDesktopInit;
import com.gary.extjs.dto.Config;
import com.gary.util.NotValidateLogin;
import com.gary.web.controller.BaseController;

@Controller
public class GzipAction extends BaseController {
	
	@RequestMapping("gzip")
	@NotValidateLogin
	public void gzip(HttpServletRequest request, HttpServletResponse response) throws Exception{
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		request.setCharacterEncoding(config.getGzipCharacter());
		response.setCharacterEncoding(config.getGzipCharacter());
		String type = request.getParameter("type");
		String path = request.getSession().getServletContext().getRealPath("")+File.separator;
		String debug = request.getParameter("debug");
		String[] files = request.getParameter("files").split(",");
		String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
		StringBuffer filesStr = new StringBuffer();
		for (String string : files) {
			if(string != null && !string.trim().isEmpty()){
				string = string.replace("\\", File.separator).replace("/", File.separator);
				int lastIndex = string.lastIndexOf(File.separator);
				if(lastIndex == -1)
					lastIndex = string.length();
				String rpath = string.substring(0,lastIndex);
				StringBuffer sb = new StringBuffer(string);
				sb.insert(sb.lastIndexOf("."), config.getDebugSuffix());
				string = sb.toString();
				if(type != null && type.equals("source")){
					if(string.endsWith(".js")){ 
						if(debug != null && !debug.trim().isEmpty() && !debug.equals("false")){
							filesStr.append(YUI.getCompressJs(new File(path + string), config.getGzipCharacter()));
						}else{
							filesStr.append(inputStream2String(new FileInputStream(path + string), config.getGzipCharacter()));
						}
					}else{
						if(debug != null && !debug.trim().isEmpty() && !debug.equals("false")){
							filesStr.append(YUI.getCompressCSS(new File(path + string), config.getGzipCharacter()));
						}else{
							filesStr.append(inputStream2String(new FileInputStream(path + string), config.getGzipCharacter()));
						}
					}
				}else{
					try {
						StringBuffer name = new StringBuffer(string);
						if(string.endsWith(".js")){
							name.insert(name.lastIndexOf(".") + 1, "gz");
						}else{
							name.insert(name.lastIndexOf(".") + 1, "gzip.");
						}
						File file = new File(name.insert(0, path).toString());
						if(!file.exists()){
							String yuiname = path + string;
							if(debug != null && !debug.trim().isEmpty() && !debug.equals("false")){
								yuiname = YUI.writerCompressJs(new File(path + string), config.getGzipCharacter());
							}
							String fname = Gzip.compress(new File(yuiname), false);
							File f = new File(path + rpath,fname);
							f.renameTo(file);
						}
						String wpath = basePath + rpath.replace("\\", "/") + "/" + file.getName();
						filesStr.append(wpath).append(",");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if(type != null && type.equals("source")){
			response.setHeader("Content-config.getGzipCharacter()", "gzip");
			try {
				OutputStream os = response.getOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(os);
				gzip.write(filesStr.toString().getBytes(config.getGzipCharacter()));
				gzip.finish();
				gzip.flush();
				gzip.close();
				os.flush();
				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			if(filesStr.length() > 0)
				filesStr.deleteCharAt(filesStr.length() - 1);
			PrintWriter out = response.getWriter();
			out.write(filesStr.toString());
			out.flush();
			out.close();
		}
	}

	public String inputStream2String(InputStream is, String charsetName) {
		if (is == null)
			return null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i = -1;
			while ((i = is.read()) != -1) {
				baos.write(i);
			}
			return baos.toString(charsetName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
