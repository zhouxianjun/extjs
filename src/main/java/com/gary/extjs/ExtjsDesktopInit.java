package com.gary.extjs;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.rubyeye.xmemcached.MemcachedClient;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.gary.extjs.dto.Config;
import com.gary.extjs.entity.ExtRole;
import com.gary.extjs.handler.ClientHandler;
import com.gary.extjs.service.IExtRoleService;
import com.gary.extjs.util.LinuxImageUtils;
import com.gary.socketio.nodejs.MinaClient;
import com.gary.socketio.nodejs.handler.AbstractClientHandler;
import com.gary.util.HttpClient;
import com.gary.web.config.ApplicationContextHolder;
public class ExtjsDesktopInit implements ServletContextListener {
	
	@Autowired
	private MemcachedClient memcachedClient;
	
	public static final String SYSTEM_ROLE = "GARY-EXTJS-DESKTOP-SYSTEM-ROLE";
	public static final String EXTJS_CONFIG = "GARY-EXTJS-DESKTOP-CONFIG";
	
	public static MinaClient client;
	public static AbstractClientHandler handler;
	private int nodejsPid = 0;
	private Thread monitoring;
	private MonitoringThread mt;
	private Logger log = Logger.getLogger(ExtjsDesktopInit.class);
	public void contextDestroyed(ServletContextEvent arg0) {
		if(client != null){
			client.close();
		}
		if(mt != null){
			mt.setStop(true);
		}
		log.info("Gary Extjs v4.0.7 Desktop stop.");
	}

	public void contextInitialized(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		String name = arg0.getServletContext().getInitParameter("role_name");
		String descr = arg0.getServletContext().getInitParameter("role_descr");
		if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(descr)){
			IExtRoleService roleService = ApplicationContextHolder.getBean("extRoleService", IExtRoleService.class);
			Config config = ApplicationContextHolder.getBean("config", Config.class);
			if(config == null){
				config = new Config();
			}
			ExtRole role = roleService.get(name);
			if(role == null){
				role = new ExtRole(name, descr);
				roleService.save(role);
			}
			if(config.isNodejs() && !StringUtils.isEmpty(config.getNodejsIp()) && config.getNodejsPort() > 0 && config.getNodejsSocketIoPort() > 0){
				if(config.isRunNode()){
					HttpClient http = new HttpClient();
					JSONObject json = http.getMethod("http://" + config.getNodejsIp() + ":" + config.getNodejsSocketIoPort() + "/pid").getJSONObject("UTF-8");
					nodejsPid = json.optInt("pid", 0);
					log.info("Nodejs Server start success. PID:" + nodejsPid);
				}
				handler = new ClientHandler();
				client = new MinaClient(handler);
				if(client.connect(config.getNodejsIp(), config.getNodejsPort())){
					log.info("Nodejs Socket IO Client start success.");
					mt = new MonitoringThread(config.getMonitoringPush());
					monitoring = new Thread(mt);
					monitoring.start();
				}else{
					log.error("Nodejs Socket IO Client start error.");
				}
			}
			arg0.getServletContext().setAttribute(SYSTEM_ROLE, role);
			arg0.getServletContext().setAttribute(EXTJS_CONFIG, config);
			LinuxImageUtils.imageMagickPath = config.getImageMagickPath();
			log.info("Gary Extjs v4.0.7 Desktop start success.");
		}else{
			System.exit(0);
		}
	}
}
