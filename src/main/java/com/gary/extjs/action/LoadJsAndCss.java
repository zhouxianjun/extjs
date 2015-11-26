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
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gary.compress.YUI;
import com.gary.compress.gzip.Gzip;
import com.gary.extjs.ExtjsDesktopInit;
import com.gary.extjs.dto.Config;
import com.gary.web.controller.BaseController;

@Controller
public class LoadJsAndCss extends BaseController {
	
	@RequestMapping("loadjc")
	private void load(@RequestParam(defaultValue = "file") String type, @RequestParam String files, @RequestParam(defaultValue = "true") boolean compress,
			@RequestParam(defaultValue = "false") boolean cover, String character, HttpServletRequest request, HttpServletResponse response) throws Exception{
		HttpSession session = request.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		character = StringUtils.isBlank(character) ? config.getGzipCharacter() : character;
		request.setCharacterEncoding(character);
		response.setCharacterEncoding(character);
		
		StringBuffer filesStr = new StringBuffer();
		String path = request.getSession().getServletContext().getRealPath("")+File.separator;
		String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/";
		String[] cfiles = files.split(",");
		for (String string : cfiles) {
			if(StringUtils.isNotBlank(string) && (string.endsWith(".js") || string.endsWith(".css"))){
				string = string.replace("\\", File.separator).replace("/", File.separator);
				int lastIndex = string.lastIndexOf(File.separator);
				if(lastIndex == -1)
					lastIndex = string.length();
				//文件夹名
				String filePath = string.substring(0, lastIndex);
				String fileName = string.substring(lastIndex + 1);
				if("source".equalsIgnoreCase(type)){
					if(compress){
						if(fileName.endsWith(".js"))
							filesStr.append(YUI.getCompressJs(new File(path + filePath, fileName), character));
						else
							filesStr.append(YUI.getCompressCSS(new File(path + filePath, fileName), character));
					}else{
						filesStr.append(inputStream2String(new FileInputStream(new File(path + filePath, fileName)), character));
					}
				}else{
					StringBuffer name = new StringBuffer(fileName);
					if(fileName.endsWith(".js")){
						name.insert(name.lastIndexOf(".") + 1, "gz"); //xxx.gzjs
					}else{
						name.insert(name.lastIndexOf(".") + 1, "gzip."); //xxx.gzip.css
					}
					File file = new File(path + filePath, name.toString());
					if(cover || !file.exists()){
						String yuiname = path + filePath + fileName;
						if(compress){
							if(fileName.endsWith(".js"))
								yuiname = YUI.writerCompressJs(new File(path + filePath, fileName), character);
							else
								yuiname = YUI.writerCompressCSS(new File(path + filePath, fileName), character);
						}
						String fname = Gzip.compress(new File(yuiname), false);
						File f = new File(path + filePath, fname);
						if(!file.exists() || file.delete()){
							f.renameTo(file);
						}
					}
					String wpath = basePath + filePath.replace("\\", "/") + "/" + file.getName();
					filesStr.append(wpath).append(",");
				}
			}
		}
		
		if("source".equalsIgnoreCase(type)){
			response.setHeader("Content-config.getGzipCharacter()", "gzip");
			response.setHeader("Content-Encoding", "gzip");
			try {
				OutputStream os = response.getOutputStream();
				GZIPOutputStream gzip = new GZIPOutputStream(os);
				gzip.write(filesStr.toString().getBytes(character));
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
	
	private String inputStream2String(InputStream is, String charsetName) {
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
