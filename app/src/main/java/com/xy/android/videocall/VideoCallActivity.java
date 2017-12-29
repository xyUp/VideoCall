/*
 * Copyright (c) 2015 - 2017. 小石头信息技术有限公司 版权所有
 *
 * 技术扣扣：1801902666
 * 公司网站：http://www.shitouerp.com
 */

package com.xy.android.videocall;

import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.superrtc.sdk.VideoView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * 视频通话界面处理
 */
public class VideoCallActivity extends CallActivity implements View.OnClickListener {

	// 视频通话帮助类
	private EMCallManager.EMVideoCallHelper videoCallHelper;
	// SurfaceView 控件状态，-1 表示通话未接通，0 表示本小远大，1 表示远小本大
	private int surfaceState = -1;

	// 视频界面
	private EMCallSurfaceView localSurface = null;
	private EMCallSurfaceView oppositeSurface = null;

	private FrameLayout flayCallCtrl;
	private LinearLayout llayCallHeader;
	private LinearLayout llayCallFooter;

	private LinearLayout llaySwitch;    // 切换摄像头
	private LinearLayout llayAnswer;    // 接听通话
	private LinearLayout llayReject;    // 拒绝通话
	private LinearLayout llayEnd;       // 结束
	private LinearLayout llayTrans;     // 转诊

	private ImageView imgvMini;

	// private TextView txtvHeaderName;
	private TextView txtvHeaderTip;
	private TextView txtvFooterTip;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_video_call);
		// 初始化布局
		initView();
	}

	/**
	 * 重载父类方法,实现一些当前通话的操作，
	 */
	@Override
	protected void initView() {
		super.initView();

		flayCallCtrl = (FrameLayout) findViewById(R.id.flay_call_ctrl);
		llaySwitch = (LinearLayout) findViewById(R.id.llay_call_footer_switch);
		llayAnswer = (LinearLayout) findViewById(R.id.llay_call_footer_answer);
		llayReject = (LinearLayout) findViewById(R.id.llay_call_footer_reject);
		llayEnd = (LinearLayout) findViewById(R.id.llay_call_footer_end);
		llayTrans = (LinearLayout) findViewById(R.id.llay_call_footer_trans);

		imgvMini = (ImageView) findViewById(R.id.imgv_call_mini);

		llaySwitch.setOnClickListener(this);
		llayAnswer.setOnClickListener(this);
		llayReject.setOnClickListener(this);
		llayEnd.setOnClickListener(this);
		llayTrans.setOnClickListener(this);

		flayCallCtrl.setOnClickListener(this);
		imgvMini.setOnClickListener(this);

		llayCallHeader = (LinearLayout) findViewById(R.id.llay_call_header);
		llayCallFooter = (LinearLayout) findViewById(R.id.llay_call_footer);

		// txtvHeaderName = (TextView) findViewById(R.id.txtv_call_header_name);
		txtvHeaderTip = (TextView) findViewById(R.id.txtv_call_header_tip);
		txtvFooterTip = (TextView) findViewById(R.id.txtv_call_footer_tip);

		// 初始化视频通话帮助类
		videoCallHelper = EMClient.getInstance().callManager().getVideoCallHelper();

		// 远程通话画面
		oppositeSurface = (EMCallSurfaceView) findViewById(R.id.em_oppo);
		oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);  // 设置本地画面的显示方式，是填充，还是居中

		// 本地画面控件
		localSurface = (EMCallSurfaceView) findViewById(R.id.em_local);
		localSurface.setZOrderOnTop(false);
		localSurface.setZOrderMediaOverlay(true);
		localSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);

		// 设置通话画面显示控件
		EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);

		// 判断当前通话是刚开始，还是从后台恢复已经存在的通话
		if (CallManager.getInstance().getCallState() == CallManager.CallState.ACCEPTED) {
			llaySwitch.setVisibility(View.VISIBLE);
			llayAnswer.setVisibility(View.GONE);
			llayReject.setVisibility(View.GONE);
			llayEnd.setVisibility(View.VISIBLE);
			// llayTrans.setVisibility(View.VISIBLE);
			// 更新通话时间
			refreshCallTime();
			// 通话已接通，修改画面显示
			exchangeCallSurface();

		} else {
			// 设置对方姓名
			// txtvHeaderName.setText(CallManager.getInstance().getChatId());

			// 通话刚开始
			if (CallManager.getInstance().isInComingCall()) {
				txtvHeaderTip.setText(getString(R.string.call_video_in_tip));
				llaySwitch.setVisibility(View.GONE);
				llayAnswer.setVisibility(View.VISIBLE);
				llayReject.setVisibility(View.VISIBLE);
				llayEnd.setVisibility(View.GONE);
				// llayTrans.setVisibility(View.GONE);
			} else {
				txtvHeaderTip.setText(getString(R.string.call_video_out_tip));
				llaySwitch.setVisibility(View.GONE);
				llayAnswer.setVisibility(View.GONE);
				llayReject.setVisibility(View.GONE);
				llayEnd.setVisibility(View.VISIBLE);
				// llayTrans.setVisibility(View.GONE);
			}
		}

		try {
			// 设置默认摄像头为前置
			EMClient.getInstance().callManager().setCameraFacing(Camera.CameraInfo.CAMERA_FACING_FRONT);
		} catch (HyphenateException e) {
			e.printStackTrace();
		}
		CallManager.getInstance().setCallCameraDataProcessor();
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.imgv_call_mini) {
			// 退出全屏弹出小窗口
			exitFullScreen();

		} else if (id == R.id.llay_call_footer_switch) {

			// 切换摄像头
			changeCamera();
		} else if (id == R.id.llay_call_footer_reject) {

			// 拒绝接听通话
			rejectCall();
		} else if (id == R.id.llay_call_footer_answer) {

			// 接听通话
			answerCall();
		} else if (id == R.id.llay_call_footer_end) {

			// 结束通话
			endCall();
		}
	}

	/**
	 * 更新UI界面
	 */
	private void exchangeCallSurface() {
		// 更新底部显示文字
		txtvFooterTip.setText(getString(R.string.call_accepted));
		// 影藏头部
		llayCallHeader.setVisibility(View.GONE);
		// 显示最小化
		imgvMini.setVisibility(View.VISIBLE);
		// 更新通话界面控件状态
		surfaceState = 0;
		localSurface.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				changeCallSurface();
			}
		});
		oppositeSurface.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				hideControlLayout();
			}
		});
	}

	/**
	 * 控制界面的显示与隐藏
	 */
	private void hideControlLayout() {
		if (llayCallFooter.isShown()) {
			llayCallFooter.setVisibility(View.GONE);
		} else {
			llayCallFooter.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 退出全屏通话界面
	 */
	private void exitFullScreen() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(VideoCallActivity.this)) {
			Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
			startActivityForResult(intent, 100);
		} else {
			CallManager.getInstance().addFloatWindow();
			// 结束当前界面
			finish();
		}

	}

	/**
	 * 切换摄像头
	 */
	private void changeCamera() {
		// 根据切换摄像头开关是否被激活确定当前是前置还是后置摄像头
		try {
			if (EMClient.getInstance().callManager().getCameraFacing() == 1) {
				EMClient.getInstance().callManager().switchCamera();
				EMClient.getInstance().callManager().setCameraFacing(0);
			} else {
				EMClient.getInstance().callManager().switchCamera();
				EMClient.getInstance().callManager().setCameraFacing(1);
			}
		} catch (HyphenateException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 接听通话
	 */
	@Override
	protected void answerCall() {
		super.answerCall();

		llaySwitch.setVisibility(View.VISIBLE);
		llayAnswer.setVisibility(View.GONE);
		llayReject.setVisibility(View.GONE);
		llayEnd.setVisibility(View.VISIBLE);
		// llayTrans.setVisibility(View.VISIBLE);
	}

	/**
	 * 切换通话界面，这里就是交换本地和远端画面控件设置，以达到通话大小画面的切换
	 */
	private void changeCallSurface() {
		if (surfaceState == 0) {
			surfaceState = 1;
			EMClient.getInstance().callManager().setSurfaceView(oppositeSurface, localSurface);
		} else {
			surfaceState = 0;
			EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onEventBus(CallEvent event) {
		if (event.isState()) {
			refreshCallView(event);
		}
		if (event.isTime()) {
			// 不论什么情况都检查下当前时间
			refreshCallTime();
		}
	}

	/**
	 * 刷新通话界面
	 */
	private void refreshCallView(CallEvent event) {
		EMCallStateChangeListener.CallError callError = event.getCallError();
		EMCallStateChangeListener.CallState callState = event.getCallState();
		switch (callState) {
			case CONNECTING: // 正在呼叫对方，TODO 没见回调过
				L.i("正在呼叫对方" + callError);
				break;
			case CONNECTED: // 正在等待对方接受呼叫申请（对方申请与你进行通话）
				L.i("正在连接" + callError);
				break;
			case ACCEPTED: // 通话已接通
				L.i("通话已接通");
				// 通话接通，更新界面 UI 显示
				exchangeCallSurface();
				break;
			case DISCONNECTED: // 通话已中断
				L.i("通话已结束" + callError);
				finish();
				break;
			// TODO 3.3.0版本 SDK 下边几个暂时都没有回调
			case NETWORK_UNSTABLE:
				if (callError == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
					L.i("没有通话数据" + callError);
				} else {
					L.i("网络不稳定" + callError);
				}
				break;
			case NETWORK_NORMAL:
				L.i("网络正常");
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

	/**
	 * 刷新通话时间显示
	 */
	private void refreshCallTime() {
		int t = CallManager.getInstance().getCallTime();
		int h = t / 60 / 60;
		int m = t / 60 % 60;
		int s = t % 60 % 60;
		String time = "";
		if (h > 9) {
			time = "" + h;
		} else {
			time = "0" + h;
		}
		if (m > 9) {
			time += ":" + m;
		} else {
			time += ":0" + m;
		}
		if (s > 9) {
			time += ":" + s;
		} else {
			time += ":0" + s;
		}
		if (!txtvFooterTip.isShown()) {
			txtvFooterTip.setVisibility(View.VISIBLE);
		}
		txtvFooterTip.setText(time);
	}

	/**
	 * 屏幕方向改变回调方法
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onUserLeaveHint() {
		//super.onUserLeaveHint();
		exitFullScreen();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 100) {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(VideoCallActivity.this)) {
				ToastUtils.showShortSafe(R.string.err_cant_overlay);
			} else {
				CallManager.getInstance().addFloatWindow();
				// 结束当前界面
				finish();
			}
		}
	}

	/**
	 * 通话界面拦截 Back 按键，不能返回
	 */
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
		exitFullScreen();
	}
}
