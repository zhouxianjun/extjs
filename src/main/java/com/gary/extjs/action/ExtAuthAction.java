package com.gary.extjs.action;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;

import org.hyperic.sigar.SigarException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.gary.dao.result.Page;
import com.gary.extjs.ExtjsDesktopInit;
import com.gary.extjs.dto.BaseUser;
import com.gary.extjs.dto.Config;
import com.gary.extjs.dto.FunctionAuth;
import com.gary.extjs.entity.ExtFunction;
import com.gary.extjs.entity.ExtModular;
import com.gary.extjs.entity.ExtRole;
import com.gary.extjs.service.IExtFunctionService;
import com.gary.extjs.service.IExtModularService;
import com.gary.extjs.service.IExtRoleService;
import com.gary.extjs.util.Util;
import com.gary.sys.SysSigar;
import com.gary.util.FileUtils;
import com.gary.util.NotValidateLogin;
import com.gary.util.ValidateUtils;
import com.gary.web.controller.BaseController;
import com.gary.web.exception.error.ErrorCode;
import com.gary.web.result.ExecuteResult;
import com.gary.web.result.Result;
import com.gary.web.util.MemcachedUtil;

@Controller
@RequestMapping("ext/auth")
public class ExtAuthAction extends BaseController {
	@Autowired
	private IExtRoleService extRoleService;
	@Autowired
	private IExtModularService extModularService;
	@Autowired
	private IExtFunctionService extFunctionService;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	@RequestMapping("modulars")
	@ResponseBody
	public Result getModulars(HttpServletRequest request){
		HttpSession session = request.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		int role = MemcachedUtil.getSession(config.getROLE_SESSION_VAL_NAME(), session);
		Result result = new Result();
		result.setSuccess(true);
		List<ExtModular> modulars = extRoleService.getModulars(role);
		if(modulars == null)
			modulars = Collections.emptyList();
		else{
			modulars = extModularService.checkLocalFile(modulars, getPath(request, config.getModularPath()), config.getCom());
		}
		result.getData().put("modulars", modulars == null ? Collections.emptyList() : modulars);
		return result;
	}
	
	@RequestMapping("allModulars")
	@ResponseBody
	public Result allModulars(HttpServletRequest request, @RequestParam int pageSize, @RequestParam int page){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		result.setSuccess(true);
		Page<ExtModular> pages = extModularService.list(pageSize, page);
		List<ExtModular> modulars = pages.getItems();
		if(modulars == null)
			modulars = Collections.emptyList();
		else{
			modulars = extModularService.checkLocal(modulars, getPath(request, config.getModularPath()), config.getCom(), getPath(request, config.getModularIconPath()), config);
		}
		result.getData().put("modulars", pages);
		return result;
	}
	
	@RequestMapping("functions")
	@ResponseBody
	public List<?> getFunctions(HttpServletRequest request, @RequestParam String modularIdentifer, Integer parentId, @RequestParam boolean isTree){
		HttpSession session = request.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		int srole = MemcachedUtil.getSession(config.getROLE_SESSION_VAL_NAME(), session);
		List<ExtFunction> functions = Collections.emptyList();
		if(parentId == null){
			functions = extModularService.getFuncitons(modularIdentifer);
		}else{
			functions = getChildren(parentId);
		}
		
		ExtRole role = extRoleService.get(srole);
		List<ExtFunction> roleFunctions = role.getFunctions();
		functions = extFunctionService.checkAuth(functions, roleFunctions);
		functions = extFunctionService.checkLocalFile(functions, getPath(request, config.getFunctionPath()) + modularIdentifer + File.separator, config.getDebugSuffix());
		if(isTree){
			return extFunctionService.transform(functions);
		}
		return functions;
	}
	
	@RequestMapping("children")
	@ResponseBody
	public List<ExtFunction> getChildren(@RequestParam Integer parentId){
		try {
			return extFunctionService.get(parentId).getChildren();
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}
	
	@RequestMapping("roles")
	@ResponseBody
	public Result getRoles(HttpServletRequest request, @RequestParam int pageSize, @RequestParam int page){
		Result result = new Result();
		result.setSuccess(true);
		Page<ExtRole> pageRole = extRoleService.list(pageSize, page);
		result.getData().put("roles", pageRole);
		return result;
	}
	
	@RequestMapping("addRole")
	@ResponseBody
	public Result addRole(HttpServletRequest request, @RequestParam String name, String descr){
		Result result = new Result();
		ExtRole role = extRoleService.get(name);
		if(role == null){
			role = new ExtRole();
			role.setDescr(descr);
			role.setName(name);
			extRoleService.save(role);
			result.setSuccess(true);
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(11, "[" + name + "]该角色已存在"));
		}
		return result;
	}
	
	@RequestMapping("updateRole")
	@ResponseBody
	public Result updateRole(HttpServletRequest request, @RequestParam Integer id, @RequestParam String name, String descr, Boolean enable){
		Result result = new Result();
		ExtRole system = (ExtRole)request.getSession().getServletContext().getAttribute("GARY-EXTJS-DESKTOP-SYSTEM-ROLE");
		ExtRole role = extRoleService.get(id);
		if(role != null){
			if(descr != null)
				role.setDescr(descr);
			if(system != null && system.getId() != id)
				role.setName(name);
			if(enable != null)
				role.setEnable(enable);
			extRoleService.update(role);
			result.setSuccess(true);
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
		}
		return result;
	}
	
	@RequestMapping("deleteRole")
	@ResponseBody
	public Result delRole(HttpServletRequest request, @RequestParam Integer id){
		Result result = new Result();
		ExtRole system = (ExtRole)request.getSession().getServletContext().getAttribute("GARY-EXTJS-DESKTOP-SYSTEM-ROLE");
		if(system != null && id == system.getId()){
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(ErrorCode.FAIL, "系统角色禁止删除"));
		}else{
			ExtRole role = extRoleService.get(id);
			if(role != null){
				role.setFunctions(null);
				role.setModulars(null);
				extRoleService.delete(role);
				result.setSuccess(true);
			}else{
				result.setSuccess(false);
				result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
			}
		}
		return result;
	}
	
	@RequestMapping("addModular")
	@ResponseBody
	public Result addModular(HttpServletRequest request, @RequestParam String name, @RequestParam String modularIdentifer, @RequestParam MultipartFile icon, boolean create) throws Exception{
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		if(icon == null || icon.isEmpty() || icon.getSize() < 1){
			throw new MissingServletRequestParameterException("icon", "File");
		}
		ExtModular modular = extModularService.get(modularIdentifer);
		if(modular == null){
			modular = new ExtModular();
			modular.setModularIdentifer(modularIdentifer);
			modular.setName(name);
			String iconName = generateModularIcon(request, modular, icon);
			modular.setIcon(iconName);
			extModularService.save(modular);
			if(!extModularService.checkLocal(modular, getPath(request, config.getModularPath()), config.getCom()) && create){
				result.setSuccess(generateModular(request, modular));
			}else{
				result.setSuccess(true);
			}
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(11, "[" + modularIdentifer + "]该模块已存在"));
		}
		return result;
	}
	
	@RequestMapping("updateModular")
	@ResponseBody
	public Result updateModular(HttpServletRequest request, @RequestParam Integer id, @RequestParam String name, MultipartFile updateIcon) throws Exception{
		Result result = new Result();
		if(updateIcon == null || updateIcon.isEmpty() || updateIcon.getSize() < 1){
			throw new MissingServletRequestParameterException("updateIcon", "File");
		}
		ExtModular modular = extModularService.get(id);
		if(modular != null){
			modular.setName(name);
			if(updateIcon != null){
				delModularIcon(request, modular);
				String iconName = generateModularIcon(request, modular, updateIcon);
				modular.setIcon(iconName);
			}
			extModularService.update(modular);
			result.setSuccess(true);
			HttpSession session = request.getSession();
			Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
			BaseUser user = MemcachedUtil.getSession(config.getUSER_SESSION_VAL_NAME(), session);
			Util.push(user.get_name(), "modular", null, null);
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
		}
		return result;
	}
	
	@RequestMapping("generateModular")
	@ResponseBody
	public Result generateModular(HttpServletRequest request, @RequestParam Integer id){
		Result result = new Result();
		ExtModular modular = extModularService.get(id);
		if(modular != null){
			result.setSuccess(generateModular(request, modular));
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
		}
		return result;
	}
	
	@RequestMapping("addFunction")
	@ResponseBody
	public Result addFunction(HttpServletRequest request, @RequestParam Integer roleId, @RequestParam String text, String modularIdentifer, Integer parentId, String extjsId, boolean create, @RequestParam String modularId) throws MissingServletRequestParameterException{
		Result result = new Result();
		ExtFunction function = extFunctionService.get(extjsId);
		if(function != null){
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(11, "[" + extjsId + "]该功能已存在"));
		}else{
			function = new ExtFunction();
			function.setId(extjsId);
			function.setText(text);
			if(!StringUtils.isEmpty(modularIdentifer)){
				ExtModular modular = extModularService.get(modularIdentifer);
				if(modular != null){
					function.getModulars().add(modular);
					extFunctionService.save(function);
					modular.getFunctions().add(function);
					extModularService.update(modular);
				}else{
					result.setSuccess(false);
					result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
					return result;
				}
			}else if(parentId != null && parentId != 0){
				ExtFunction parentFunction = extFunctionService.get(parentId);
				extFunctionService.save(function);
				parentFunction.setId(parentFunction.getFid() + "");
				parentFunction.getChildren().add(function);
				extFunctionService.update(parentFunction);
			}else{
				throw new MissingServletRequestParameterException(modularIdentifer == null ? "parentId" : "modularIdentifer", modularIdentifer == null ? "Integer" : "String");
			}
			if(!StringUtils.isEmpty(extjsId) && !ValidateUtils.zhengshuValidate(extjsId) && create){
				generateFunction(request, function, modularId);
			}
			result.setSuccess(true);
		}
		return result;
	}
	
	@RequestMapping("deleteFunction")
	@ResponseBody
	public Result deleteFunction(HttpServletRequest request, @RequestParam Integer fid){
		Result result = new Result();
		ExtFunction function = extFunctionService.get(fid);
		if(function != null){
			List<ExtRole> roles = extRoleService.list();
			for (ExtRole role : roles) {
				extRoleService.deleteFuction(function, role);
				List<ExtModular> modulars = function.getModulars();
				for (ExtModular modular : modulars) {
					extModularService.deleteFuction(function, modular);
					deleteFunction(request, function, modular.getModularIdentifer());
				}
			}
			extFunctionService.delete(function);
			result.setSuccess(true);
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
		}
		return result;
	}
	
	@RequestMapping("deleteModular")
	@ResponseBody
	public Result deleteModular(HttpServletRequest request, @RequestParam String modularId){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		ExtModular modular = extModularService.get(modularId);
		if(modular != null){
			List<ExtRole> roles = extRoleService.list();
			for (ExtRole role : roles) {
				List<ExtFunction> functions = modular.getFunctions();
				for (ExtFunction function : functions) {
					extRoleService.deleteFuction(function, role);
				}
				extRoleService.deleteModular(modular, role);
			}
			extModularService.delete(modular);
			String path = getPath(request, config.getModularPath());
			if(extModularService.checkLocal(modular, path, config.getCom())){
				deleteModular(request, modular);
			}
			delModularIcon(request, modular);
			result.setSuccess(true);
		}else{
			result.setSuccess(false);
			result.setExecuteResult(new ExecuteResult(ErrorCode.NOT_FOUND));
		}
		return result;
	}
	
	@RequestMapping("all")
	@ResponseBody
	public List<FunctionAuth> getAll(Integer roleId){
		List<ExtModular> modulars = Collections.emptyList();
		if(roleId == null || roleId == 0)
			modulars = extModularService.list();
		else
			modulars = extRoleService.getModulars(roleId);
		List<FunctionAuth> funcitons = new ArrayList<FunctionAuth>();
		for (ExtModular modular : modulars) {
			FunctionAuth function = new FunctionAuth();
			function.setChildren(transformAuth(modular.getFunctions()));
			function.setId(modular.getModularIdentifer());
			function.setText(modular.getName());
			funcitons.add(function);
		}
		return funcitons;
	}
	
	@RequestMapping("allAuth")
	@ResponseBody
	public List<ExtFunction> getAllAuth(Integer roleId){
		if(roleId == null)
			return Collections.emptyList();
		List<ExtModular> modularAll = extModularService.list();
		List<ExtModular> modularMy = extRoleService.getModulars(roleId);
		ExtRole role = extRoleService.get(roleId);
		List<ExtFunction> roleFunctions = role.getFunctions();
		List<ExtFunction> funcitons = new ArrayList<ExtFunction>();
		for (ExtModular modular : modularAll) {
			ExtFunction function = new ExtFunction();
			for (ExtModular modular2 : modularMy) {
				if(modular.getId() == modular2.getId()){
					function.setChecked(true);
				}
			}
			if(roleFunctions == null){
				function.setChildren(modular.getFunctions());
			}else{
				function.setChildren(checkedAuth(modular.getFunctions(), roleFunctions));
			}
			function.setParent(true);
			function.setId("modular#" + modular.getModularIdentifer());
			function.setText(modular.getName());
			funcitons.add(function);
		}
		return funcitons;
	}
	
	@RequestMapping("set")
	@ResponseBody
	public Result setAuth(HttpServletRequest request, @RequestParam String modular,@RequestParam String functions, @RequestParam Integer roleId){
		Result result = new Result();
		result.setSuccess(true);
		JSONArray array = JSONArray.fromObject(modular);
		JSONArray functionArray = JSONArray.fromObject(functions);
		ExtRole role = extRoleService.get(roleId);
		List<ExtModular> ms = role.getModulars();
		ms.clear();
		List<String> noModular = new ArrayList<String>();
		for (Object key : array) {
			ExtModular m = extModularService.get(key.toString());
			if(m != null)
				ms.add(m);
			else
				noModular.add(key.toString());
		}
		
		List<ExtFunction> functionList = role.getFunctions();
		functionList.clear();
		List<Integer> no = new ArrayList<Integer>();
		for (Object key : functionArray) {
			Integer decode = Integer.decode(key.toString());
			ExtFunction function = extFunctionService.get(decode);
			if(function == null){
				no.add(decode);
			}else{
				functionList.add(function);
			}
		}
		result.getData().put("noFunction", no);
		result.getData().put("noModular", noModular);
		extRoleService.update(role);
		HttpSession session = request.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		BaseUser user = MemcachedUtil.getSession(config.getUSER_SESSION_VAL_NAME(), session);
		Util.push(user.get_name(), "auth", null, null);
		return result;
	}
	
	@RequestMapping("modularFiles")
	@ResponseBody
	public Result getModularFileTree(HttpServletRequest request){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		String path = getPath(request, config.getModularPath());
		logger.debug("modular路径:" + path);
		List<Map<String, String>> list = Util.toLocalFileIdentifer(Util.getFilesString(path, ".js"), config.getCom() + ".");
		result.getData().put("modularFiles", list);
		result.setSuccess(true);
		return result;
	}
	
	@RequestMapping("functionFiles")
	@ResponseBody
	public Result getFunctionFileTree(HttpServletRequest request, @RequestParam String modularId){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		String path = getPath(request, config.getFunctionPath())  + modularId + File.separator;
		List<Map<String, String>> list = Util.toLocalFileIdentifer(Util.getFilesString(path, ".js"), config.getDebugSuffix());
		result.getData().put("functionFiles", list);
		result.setSuccess(true);
		return result;
	}
	
	@RequestMapping("getConfig")
	@ResponseBody
	public Result getConfig(HttpServletRequest request){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		result.getData().put("config", config);
		result.setSuccess(true);
		return result;
	}
	
	@RequestMapping("addConnection")
	@ResponseBody
	public Result addConnection(HttpServletRequest request, @RequestParam String name, @RequestParam String id){
		HttpSession session = request.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		BaseUser user = MemcachedUtil.getSession(config.getUSER_SESSION_VAL_NAME(), session);
		Result result = new Result();
		if(config.isNodejs() && ExtjsDesktopInit.handler != null && user != null && user.get_name().equals(name)){
			ExtjsDesktopInit.handler.addConnection(name, id);
			result.setSuccess(true);
		}else{
			if(!config.isNodejs()){
				result.setExecuteResult(new ExecuteResult(12, "未开启socket io"));
			}else{
				result.setExecuteResult(new ExecuteResult(999));
			}
		}
		return result;
	}
	
	@RequestMapping("isLogin")
	@ResponseBody
	@NotValidateLogin
	public Result isLogin(HttpServletRequest request){
		HttpSession session = request.getSession();
		Config config = (Config)session.getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		Result result = new Result();
		BaseUser user = MemcachedUtil.getSession(config.getUSER_SESSION_VAL_NAME(), session);
		result.setSuccess(user != null);
		result.getData().put("user", user);
		return result;
	}
	
	@RequestMapping("sysDynamic")
	@ResponseBody
	public Result sysDynamic(HttpServletRequest request) throws SigarException{
		Result result = new Result();
		result.setSuccess(true);
		result.getData().put("cpusInfo", SysSigar.getCpusInfo());
		result.getData().put("cpusPerc", SysSigar.getCpusPerc());
		result.getData().put("mem", SysSigar.getMem());
		result.getData().put("swap", SysSigar.getSwap());
		return result;
	}
	
	private List<ExtFunction> checkedAuth(List<ExtFunction> list, List<ExtFunction> list2){
		if(list != null && list2 != null){
			for (ExtFunction function : list) {
				for (ExtFunction function2 : list2) {
					if(function.getFid() == function2.getFid()){
						function.setChecked(true);
						function.setChildren(checkedAuth(function.getChildren(), function2.getChildren()));
					}
				}
			}
		}
		return list;
	}
	
	private List<FunctionAuth> transformAuth(List<ExtFunction> list){
		List<FunctionAuth> funcitons = new ArrayList<FunctionAuth>();
		for (ExtFunction modular : list) {
			FunctionAuth function = new FunctionAuth();
			function.setChildren(transformAuth(modular.getChildren()));
			function.setId(modular.getId());
			function.setText(modular.getText());
			funcitons.add(function);
		}
		return funcitons;
	}
	
	private synchronized boolean generateModular(HttpServletRequest request, ExtModular modular){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		if(modular != null){
			String tpl = FileUtils.readFile(new File(Util.getClassPath(), config.getModularTplPath()), config.getGzipCharacter());
			tpl = tpl.replace("{modularIdentifer}", modular.getModularIdentifer());
			tpl = tpl.replace("{modularName}", modular.getName());
			return Util.write(getPath(request, config.getModularPath()), config.getCom() + "." + modular.getModularIdentifer() + ".js", config.getGzipCharacter(), tpl, false);
		}
		return false;
	}
	
	private synchronized void deleteModular(HttpServletRequest request, ExtModular modular){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		if(modular != null){
			File file = new File(getPath(request, config.getModularPath()), config.getCom() + "." + modular.getModularIdentifer() + ".js");
			if(file.exists()){
				file.delete();
			}
		}
	}
	
	private synchronized void deleteFunction(HttpServletRequest request, ExtFunction function, String modularId){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		if(function != null){
			String path = getPath(request, config.getFunctionPath())  + modularId + File.separator;
			File file = new File(path, function.getId() + config.getDebugSuffix() + ".js");
			if(file.exists()){
				file.delete();
			}
		}
	}
	
	private synchronized boolean generateFunction(HttpServletRequest request, ExtFunction function, String modularId){
		Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
		if(function != null){
			String tpl = FileUtils.readFile(new File(Util.getClassPath(), config.getFunctionTplPath()), config.getGzipCharacter());
			tpl = tpl.replace("{functionIdentifer}", function.getId());
			tpl = tpl.replace("{functionName}", function.getText());
			return Util.write(getPath(request, config.getFunctionPath())  + modularId + File.separator, function.getId() + config.getDebugSuffix() + ".js", config.getGzipCharacter(), tpl, false);
		}
		return false;
	}
	
	private synchronized String generateModularIcon(HttpServletRequest request, ExtModular modular,MultipartFile icon) throws IOException, Exception{
		if(modular != null){
			Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
			String originalFilename = icon.getOriginalFilename();
			String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
			String iconName = modular.getModularIdentifer() + "-" + sdf.format(new Date());
			File modularIconTplFile = new File("/" + Util.getClassPath(), config.getModularIconTplPath());
			logger.debug("modular-icon-tpl-path:" + modularIconTplFile);
			String tpl = FileUtils.readFile(modularIconTplFile, config.getGzipCharacter());
			logger.debug("modular-icon:" + tpl);
			tpl = tpl.replace("{modularIdentifer}", modular.getModularIdentifer());
			tpl = tpl.replace("{com}", config.getCom());
			tpl = tpl.replace("{path}", config.getModularIconPath());
			tpl = tpl.replace("{suffix}", suffix);
			tpl = tpl.replace("{icon}", iconName);
			tpl = tpl.replace("{max}", ((int)config.getIconMaxW()) + "");
			tpl = tpl.replace("{min}", ((int)config.getIconMinW()) + "");
			logger.debug("modular-icon:" + tpl);
			logger.debug("modular-icon-path:" + getPath(request, "css"));
			Util.write(getPath(request, "css"), "modular-icon.css", config.getGzipCharacter(), tpl.trim(), true);
			iconName = iconName + suffix;
			Util.createIconImage(icon.getInputStream(), getPath(request, config.getModularIconPath()), iconName, config.getIconMaxW(), config.getIconMaxH(), config.getIconMinW(), config.getIconMinH());
			return iconName;
		}
		return null;
	}
	
	private synchronized void delModularIcon(HttpServletRequest request, ExtModular modular){
		if(modular != null){
			Config config = (Config)request.getSession().getServletContext().getAttribute(ExtjsDesktopInit.EXTJS_CONFIG);
			String tpl = FileUtils.readFile(new File(getPath(request, "css"), "modular-icon.css"), config.getGzipCharacter());
			int indexOf = tpl.indexOf("/**"+modular.getModularIdentifer()+"-start*/");
			String string = "/**"+modular.getModularIdentifer()+"-end*/";
			int indexOf2 = tpl.indexOf(string);
			if(indexOf > -1 && indexOf2 > -1){
				String del = tpl.substring(indexOf, indexOf2 + string.length());
				tpl = tpl.replace(del, "").trim();
				Util.write(getPath(request, "css"), "modular-icon.css", config.getGzipCharacter(), tpl, false);
			}
			if(!StringUtils.isEmpty(modular.getIcon())){
				String name = modular.getIcon().substring(0, modular.getIcon().lastIndexOf("."));
				String suffix = modular.getIcon().substring(modular.getIcon().lastIndexOf("."));
				File file = new File(getPath(request, config.getModularIconPath()), name + "-" + ((int)config.getIconMaxW()) + suffix);
				if(file.exists()){
					file.delete();
				}
				file = new File(getPath(request, config.getModularIconPath()), name + "-" + ((int)config.getIconMinW()) + suffix);
				if(file.exists()){
					file.delete();
				}
			}
		}
	}

	public void setExtRoleService(IExtRoleService extRoleService) {
		this.extRoleService = extRoleService;
	}

	public void setExtModularService(IExtModularService extModularService) {
		this.extModularService = extModularService;
	}

	public void setExtFunctionService(IExtFunctionService extFunctionService) {
		this.extFunctionService = extFunctionService;
	}
}
