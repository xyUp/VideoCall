/*
 * Copyright (c) 2015 - 2017. 小石头信息技术有限公司 版权所有
 *
 * 技术扣扣：1801902666
 * 公司网站：http://www.shitouerp.com
 */

package com.xy.android.videocall;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 通话推送信息回掉接口，主要是用来实现当对方不在线时，发送一条消息，推送给对方，让对方上线后能继续收到呼叫
 */
public class CallPushProvider implements EMCallManager.EMCallPushProvider {
	@Override
	public void onRemoteOffline(String username) {
		EMMessage message = EMMessage.createTxtSendMessage("医生邀请您进行在线会诊，点击查看。", username);
		if (CallManager.getInstance().getCallType() == CallManager.CallType.VIDEO) {
			message.setAttribute("attr_call_video", true);
		} else {
			message.setAttribute("attr_call_voice", true);
		}
		// 设置强制推送
		message.setAttribute("em_force_notification", "true");
		// 设置自定义推送提示
		JSONObject extObj = new JSONObject();
		try {
			extObj.put("em_push_title", "医生邀请您进行在线会诊，点击查看。");
			extObj.put("extern", "定义推送扩展内容");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		message.setAttribute("em_apns_ext", extObj);
		message.setMessageStatusCallback(new EMCallBack() {
			@Override
			public void onSuccess() {

			}

			@Override
			public void onError(int i, String s) {

			}

			@Override
			public void onProgress(int i, String s) {

			}
		});
		EMClient.getInstance().chatManager().sendMessage(message);
	}
}
