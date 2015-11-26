package com.gary.extjs.filter;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.gary.compress.YUI;
import com.gary.compress.gzip.Gzip;
import com.gary.extjs.ExtjsDesktopInit;
import com.gary.extjs.dto.Config;

/**
 * Servlet Filter implementation class GzipFilter
 */
public class JCFilter implements Filter {
	private boolean cover = false;

    /**
     * Default constructor. 
     */
    public JCFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse res = (HttpServletResponse)response;
		HttpSession session = req.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		String uri = req.getRequestURI();
		if((uri.endsWith(".js") || uri.endsWith(".css")) && !uri.endsWith(".gzip.css")){
			String coverStr = req.getParameter("cover");
			if(coverStr != null){
				cover = Boolean.parseBoolean(coverStr);
			}
			String path = req.getSession().getServletContext().getRealPath("")+File.separator;
			String contextPath = req.getContextPath();
			String filePath = uri.replace(contextPath, "");
			filePath = filePath.replace("\\", File.separator).replace("/", File.separator);
			int lastIndex = filePath.lastIndexOf(File.separator);
			if(lastIndex == -1)
				lastIndex = filePath.length();
			//文件夹名
			String fileName = filePath.substring(lastIndex + 1);
			filePath = filePath.substring(0, lastIndex);
			path += filePath;
			
			if(new File(path, fileName).exists()){
				StringBuffer name = new StringBuffer(fileName);
				if(fileName.endsWith(".js")){
					name.insert(name.lastIndexOf(".") + 1, "gz"); //xxx.gzjs
				}else{
					name.insert(name.lastIndexOf(".") + 1, "gzip."); //xxx.gzip.css
				}
				File file = new File(path, name.toString());
				
				if(cover || !file.exists()){
					try {
						String yuiname = "";
						if(fileName.endsWith(".js"))
							yuiname = YUI.writerCompressJs(new File(path, fileName), config.getGzipCharacter());
						else
							yuiname = YUI.writerCompressCSS(new File(path, fileName), config.getGzipCharacter());
						String fname = Gzip.compress(new File(yuiname), false);
						File f = new File(path, fname);
						if(!file.exists() || file.delete()){
							f.renameTo(file);
						}
					} catch (Exception e) {e.printStackTrace();}
				}
				String basePath = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+req.getContextPath();
				res.sendRedirect(basePath + filePath + "/" + name.toString());
				return;
			}
		}
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		cover = Boolean.parseBoolean(fConfig.getInitParameter("cover"));
	}

}
