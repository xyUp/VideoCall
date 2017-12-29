/*
 * Copyright (c) 2015 - 2017. 小石头信息技术有限公司 版权所有
 *
 * 技术扣扣：1801902666
 * 公司网站：http://www.shitouerp.com
 */

package com.xy.android.videocall;

import com.hyphenate.chat.EMCallStateChangeListener;

import org.greenrobot.eventbus.EventBus;

/**
 * 通话状态监听类，用来监听通话过程中状态的变化
 */

public class CallStateListener implements EMCallStateChangeListener {

	@Override
	public void onCallStateChanged(CallState callState, CallError callError) {
		CallEvent event = new CallEvent();
		event.setState(true);
		event.setCallError(callError);
		event.setCallState(callState);
		EventBus.getDefault().post(event);
		switch (callState) {
			case CONNECTING: // 正在呼叫对方，TODO 没见回调过
				L.i("正在呼叫对方" + callError);
				CallManager.getInstance().setCallState(CallManager.CallState.CONNECTING);
				break;
			case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
				L.i("正在连接" + callError);
				CallManager.getInstance().setCallState(CallManager.CallState.CONNECTED);
				break;
			case ACCEPTED: // 通话已接通
				L.i("通话已接通");
				CallManager.getInstance().stopCallSound();
				CallManager.getInstance().startCallTime();
				CallManager.getInstance().setEndType(CallManager.EndType.NORMAL);
				CallManager.getInstance().setCallState(CallManager.CallState.ACCEPTED);
				break;
			case DISCONNECTED: // 通话已中断
				L.i("通话已结束 " + callError);
				// 通话结束，重置通话状态
				if (callError == CallError.ERROR_UNAVAILABLE) {
					L.i("对方不在线 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.OFFLINE);
				} else if (callError == CallError.ERROR_BUSY) {
					L.i("对方正忙 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.BUSY);
				} else if (callError == CallError.REJECTED) {
					L.i("对方已拒绝 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.REJECTED);
				} else if (callError == CallError.ERROR_NORESPONSE) {
					L.i("对方未响应，可能手机不在身边 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.NORESPONSE);
				} else if (callError == CallError.ERROR_TRANSPORT) {
					L.i("连接建立失败 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.TRANSPORT);
				} else if (callError == CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED) {
					L.i("双方通讯协议不同 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.DIFFERENT);
				} else if (callError == CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
					L.i("双方通讯协议不同 " + callError);
					CallManager.getInstance().setEndType(CallManager.EndType.DIFFERENT);
				} else if (callError == CallError.ERROR_NO_DATA) {
					L.i("没有通话数据 " + callError);
				} else {
					L.i("通话已结束 " + callError);
					if (CallManager.getInstance().getEndType() == CallManager.EndType.CANCEL) {
						CallManager.getInstance().setEndType(CallManager.EndType.CANCELLED);
					}
				}
				// 通话结束，保存消息
				CallManager.getInstance().saveCallMessage();
				CallManager.getInstance().reset();
				break;
			case NETWORK_DISCONNECTED:
				L.i("对方网络不可用");
				break;
			case NETWORK_NORMAL:
				L.i("网络正常");
				break;
			case NETWORK_UNSTABLE:
				if (callError == CallError.ERROR_NO_DATA) {
					L.i("没有通话数据 " + callError);
				} else {
					L.i("网络不稳定 " + callError);
				}
				break;
			case VIDEO_PAUSE:
				L.i("视频传输已暂停");
				break;
			case VIDEO_RESUME:
				L.i("视频传输已恢复");
				break;
			case VOICE_PAUSE:
				L.i("语音传输已暂停");
				break;
			case VOICE_RESUME:
				L.i("语音传输已恢复");
				break;
			default:
				break;

		}
	}
}
