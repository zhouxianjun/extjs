package com.gary.extjs.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.sf.json.JSONObject;

import org.apache.commons.io.output.FileWriterWithEncoding;

import com.gary.extjs.ExtjsDesktopInit;
import com.gary.socketio.dto.NodejsSocketMsg;

public class Util {
	public static List<String> getFilesString(String root, String suffix){
		List<String> list = new ArrayList<String>();
		File rootFile = new File(root);
		if(rootFile.exists()){
			String[] sz = rootFile.list();
			for (String string : sz) {
				if(string.endsWith(suffix)){
					list.add(string);
				}
			}
		}
		return list;
	}
	
	public static List<Map<String, String>> toLocalFileIdentifer(List<String> filesStr, String com){
		List<Map<String, String>> rs = new ArrayList<Map<String, String>>();
		for (String string : filesStr) {
			String name = string.replace(com, "");
			name = name.replace(".js", "");
			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("id", name);
			rs.add(hashMap);
		}
		return rs;
	}
	
	public static String getClassPath(){
		URL url = Util.class.getClassLoader().getResource("");
		String path = url.getPath().replace("%20", " ");
		return fix(path);
	}
	public static String fix(String path){
		if(path.startsWith("/"))
			path = path.substring(1);
		return path.replace("\\", "/");
	}
	public static void createIconImage(InputStream in, String destFile, String name, double maxw, double maxh, double minw, double minh) throws Exception { 
		String f = name.substring(0, name.lastIndexOf("."));
		String suffix = name.substring(name.lastIndexOf("."));
		String src = UUID.randomUUID().toString() + suffix;
		File temp = new File(src);
		inputstreamtofile(in, temp);
		LinuxImageUtils.cutImage((int)maxw,(int)maxh, temp.getAbsolutePath(), destFile + f + "-" + ((int)maxw) + suffix);
		LinuxImageUtils.cutImage((int)minw,(int)minh, temp.getAbsolutePath(), destFile + f + "-" + ((int)minw) + suffix);
		temp.delete();
    }
	
	/**
	 * 向客户端发送消息
	 * @param name 发送给谁
	 * @param doWhat 做什么
	 * @param type 类型
	 * @param data 数据
	 */
	public static void push(String name, String doWhat, String type, Object data){
		if(ExtjsDesktopInit.handler != null){
			NodejsSocketMsg msg = new NodejsSocketMsg();
			List<String> list = Collections.emptyList();
			if(name != null)
				list = ExtjsDesktopInit.handler.getConnections(name);
			msg.setDoWhat(doWhat);
			msg.setTags(list);
			msg.setType(type);
			msg.setData(data);
			ExtjsDesktopInit.client.send(JSONObject.fromObject(msg).toString());
		}
	}
	public static boolean write(String path, String name, String encoding, String str,
			boolean writer) {
		try {
			String filePath = path + name;
			File fileDirectory = new File(path);// 文件夹的路径
			if (!fileDirectory.exists()) {
				fileDirectory.mkdirs();
			}// 文件夹不存在就创建
			File file = new File(filePath);// 文件的路径
			if (!file.exists()) {
				file.createNewFile();
			}// 文件不存在就创建
			FileWriterWithEncoding fw = new FileWriterWithEncoding(file.getPath(), encoding, writer);// 创建FileWriter对象，用来写入字符流
			BufferedWriter bw = new BufferedWriter(fw); // 将缓冲对文件的输出
			bw.write(str); // 写入文件
			bw.newLine();
			bw.flush(); // 刷新该流的缓冲
			bw.close();
			fw.close();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	public static void inputstreamtofile(InputStream ins, File file) {
		try {
			OutputStream os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			os.close();
			ins.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
