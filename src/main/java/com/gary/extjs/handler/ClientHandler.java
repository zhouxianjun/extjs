package com.gary.extjs.handler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.mina.core.session.IoSession;

import com.gary.socketio.nodejs.handler.AbstractClientHandler;

public class ClientHandler extends AbstractClientHandler {
	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(IoSession session, Object message)
			throws Exception {
		try {
			JSONObject json = JSONObject.fromObject(message);
			if(json.get("allConnection") != null){
				JSONArray array = json.getJSONArray("allConnection");
				connectionSync(array);
				logger.debug(JSONArray.fromObject(clients).toString());
				logger.debug(JSONArray.fromObject(currentConn));
			}
		} catch (Exception e) {
			logger.error("数据解析异常!" + message.toString(), e);
		}
	}
}
