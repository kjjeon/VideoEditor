package com.jylee.videoeditor.ffmpeg;

import android.util.Log;

import com.jylee.videoeditor.util.FontManager;

/**
 * Created by jooyoung on 2018-02-24.
 */

public class FFmpegCmdPackage {
	private static final String TAG = "FFmpegCmdPackage";
	private static FFmpegCmdPackage mInstance = null;

	public static FFmpegCmdPackage getInstance() {
		if(mInstance == null) {
			mInstance = new FFmpegCmdPackage();
		}
		return mInstance;
	}

	public String[] getVersionCmd() {
		String[] cmd =  {"-version"};
		return cmd;
	}

	public String[] getToAddTextCmd(String output, String input, String text) {
		StringBuffer textBody = new StringBuffer ();

		textBody.append("drawtext=");
		textBody.append("text=").append(text).append(":");
		textBody.append("fontsize=").append("50").append(":");
//		textBody.append("fontfile=").append("file://android_asset/NanumGothic.ttf").append(":");
//		textBody.append("fontfile=").append("/system/fonts/DroidSansFallback.ttf").append(":");
		textBody.append("fontfile=").append(FontManager.getInstance().getNanumGothicPath()).append(":");
		textBody.append("fontcolor=").append("white").append(":");
		textBody.append("x=").append("70").append(":");
		textBody.append("y=").append("52").append(":");
		textBody.append("enable=").append("'between(t,0,5)'");

		String preset = "veryfast"; // ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo
		String resolution = "640x360";// 16:9 --> 1920x1080,1280x720,640x360,480x270

		Log.d(TAG,"input = " + input);
		Log.d(TAG,"output = " + output);
		Log.d(TAG,"textbody = " + textBody.toString());
		Log.d(TAG,"preset = " + preset);
		Log.d(TAG,"resolution = " + resolution);
		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(), "-strict", "-2", "-preset", preset, "-s", resolution, output};
		return cmd;
	}

}
