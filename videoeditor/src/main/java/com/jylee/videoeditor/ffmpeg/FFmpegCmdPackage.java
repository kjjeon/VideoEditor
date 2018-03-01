package com.jylee.videoeditor.ffmpeg;

import android.util.Log;

import com.jylee.videoeditor.util.FontManager;

/**
 * Created by jooyoung on 2018-02-24.
 */

public class FFmpegCmdPackage {
	private static final String TAG = "FFmpegCmdPackage";

	public String[] getVersionCmd() {
		String[] cmd =  {"-version"};
		return cmd;
	}

	public String[] getInfo(String file) {
		String[] cmd = new String[]{"-i",file};
		return cmd;
	}

	public String[] getConcatVideoCmd(String output,String fileListText) {
		String[] cmd = new String[]{"-y","-f","concat"
				,"-i", fileListText
				,"-c","copy", output};

		return cmd;

//		"/storage/emulated/0/Download/mp4parser/ffmpeg.txt
		//		String[] cmd = new String[]{
//				"-i", "/storage/emulated/0/Download/mp4parser/0.mp4",
//				"-i", "/storage/emulated/0/Download/mp4parser/1.mp4",
//				"-i", "/storage/emulated/0/Download/mp4parser/2.mp4",
//				"-filter_complex","[0:v] [0:a] [1:v] [1:a] [2:v] [2:a] concat=n=3:v=1:a=1 [v] [a]",
//				"-map","[v]","-map","[a]",
//				output};
//		ffmpeg -i first.mp3 -i second.mp3 -filter_complex [0:a][1:a]concat=n=2:v=0:a=1 out.mp3
//			String[] cmd = new String[]{"-i","concat:/storage/emulated/0/Download/mp4parser/0.mp4|/storage/emulated/0/Download/mp4parser/1.mp4|/storage/emulated/0/Download/mp4parser/12.mp4","-c","copy", output};

	}

	public String[] getToAddTextCmd(String output, String input, String text) {
		StringBuffer textBody = new StringBuffer ();

		textBody.append("drawtext=");
		textBody.append("text=").append(text).append(":");
		textBody.append("fontsize=").append("50").append(":");
//		textBody.append("fontfile=").append("/system/fonts/DroidSans.ttf").append(":");
		textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
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

	public String[] getToAddTextCmd(String output, String input, String text,int x, int y,int fontsize,String fontColor, int startTime, int endTime, int width, int height) {
		StringBuffer textBody = new StringBuffer ();

		textBody.append("drawtext=");
		textBody.append("text=").append(text).append(":");
		textBody.append("fontsize=").append(String.valueOf(fontsize)).append(":");
//		textBody.append("fontfile=").append("/system/fonts/DroidSans.ttf").append(":");
		textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
		textBody.append("fontcolor=").append(fontColor).append(":");
		textBody.append("x=").append(String.valueOf(x)).append(":");
		textBody.append("y=").append(String.valueOf(y)).append(":");
		textBody.append("enable=").append("'between(t,"+String.valueOf(startTime)+","+String.valueOf(endTime)+")'");

		String preset = "medium"; // ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo
		String resolution = String.valueOf(width) + "x" + String.valueOf(height);// 16:9 --> 1920x1080,1280x720,640x360,480x270

		Log.d(TAG,"input = " + input);
		Log.d(TAG,"output = " + output);
		Log.d(TAG,"textbody = " + textBody.toString());
		Log.d(TAG,"preset = " + preset);
		Log.d(TAG,"resolution = " + resolution);


//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-vcodec","copy","-acodec","copy","-framerate","30","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-framerate","30","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};

		return cmd;
	}


}
