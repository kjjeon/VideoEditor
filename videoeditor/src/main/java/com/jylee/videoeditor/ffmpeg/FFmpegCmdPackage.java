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

	public String[] getToAddTextCmd(String output, String input, String text,int x, int y,
									int fontsize,String fontColor,
									int startTime, int endTime,
									int width, int height,
									String tbr) {
		StringBuffer textBody = new StringBuffer ();

		textBody.append("drawtext=");
		if(endTime != 0)
			textBody.append("enable=").append("'between(t,"+String.valueOf(startTime)+","+String.valueOf(endTime)+")'").append(":");
		textBody.append("text=").append(text).append(":");
		textBody.append("fontsize=").append(String.valueOf(fontsize)).append(":");
//		textBody.append("fontfile=").append("/system/fonts/DroidSans.ttf").append(":");
		textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
		textBody.append("fontcolor=").append(fontColor).append(":");
		textBody.append("x=").append(String.valueOf(x)).append(":");
		textBody.append("y=").append(String.valueOf(y));

		String preset = "medium"; // ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo
		String resolution = String.valueOf(width) + "x" + String.valueOf(height);// 16:9 --> 1920x1080,1280x720,640x360,480x270

		Log.d(TAG,"input = " + input);
		Log.d(TAG,"output = " + output);
		Log.d(TAG,"textbody = " + textBody.toString());
		Log.d(TAG,"preset = " + preset);
		Log.d(TAG,"resolution = " + resolution);


//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-vcodec","copy","-acodec","copy","-framerate","30","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-video_track_timescale","2997","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
		// ffmpeg info : if tbn 2997 -> video_track_timescale 2997. sample video is 2997 tbn.
		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),
				"-video_track_timescale",tbr, "-strict", "-2",
				"-preset", preset, "-s", resolution, output};

		return cmd;
	}

	public String[] getToAddCenterAlignTextCmd(String output, String input, String text,
									int fontsize,String fontColor,
									int startTime, int endTime,
									String tbr) {
		StringBuffer textBody = new StringBuffer ();

		textBody.append("drawtext=");
		if(endTime != 0)
			textBody.append("enable=").append("'between(t,"+String.valueOf(startTime)+","+String.valueOf(endTime)+")'").append(":");
		textBody.append("text=").append(text).append(":");
		textBody.append("fontsize=").append(String.valueOf(fontsize)).append(":");
//		textBody.append("fontfile=").append("/system/fonts/DroidSans.ttf").append(":");
		textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
		textBody.append("fontcolor=").append(fontColor).append(":");
		textBody.append("x=(w-tw)/2").append(":");
		textBody.append("y=(h-th-line_h)/2");

		String preset = "medium"; // ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo


		Log.d(TAG,"input = " + input);
		Log.d(TAG,"output = " + output);
		Log.d(TAG,"textbody = " + textBody.toString());
		Log.d(TAG,"preset = " + preset);



//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-vcodec","copy","-acodec","copy","-framerate","30","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-video_track_timescale","2997","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
		// ffmpeg info : if tbn 2997 -> video_track_timescale 2997. sample video is 2997 tbn.
		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),
				"-video_track_timescale",tbr, "-strict", "-2",
				"-preset", preset, output};

		return cmd;
	}

	public String[] getToAddCenterAlignTextCmd(String output, String input,
											   int fontsize1,String text1,
											   int fontsize2,String text2,
											   int fontsize3,String text3,
											   String fontColor,
											   int startTime, int endTime,
											   String tbr) {


		StringBuffer textBody = new StringBuffer ();

		textBody.append("drawtext=");
		if(endTime != 0)
			textBody.append("enable=").append("'between(t,"+String.valueOf(startTime)+","+String.valueOf(endTime)+")'").append(":");
		textBody.append("text=").append(text1).append(":");
		textBody.append("fontsize=").append(String.valueOf(fontsize1)).append(":");
		textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
		textBody.append("fontcolor=").append(fontColor).append(":");
		textBody.append("x=(w-tw)/2").append(":");
		textBody.append("y=(h-th-").append(String.valueOf(fontsize2)).append("-line_h)/2");
//		textBody.append("y=(h-th)/2");

		if(text2 != "") {
			textBody.append(", drawtext=");
			if (endTime != 0)
				textBody.append("enable=").append("'between(t," + String.valueOf(startTime) + "," + String.valueOf(endTime) + ")'").append(":");
			textBody.append("text=").append(text2).append(":");
			textBody.append("fontsize=").append(String.valueOf(fontsize2)).append(":");
			textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
			textBody.append("fontcolor=").append(fontColor).append(":");
			textBody.append("x=(w-tw)/2").append(":");
			textBody.append("y=(h-th)/2");
		}

		if(text3 != "") {
			textBody.append(", drawtext=");
			if (endTime != 0)
				textBody.append("enable=").append("'between(t," + String.valueOf(startTime) + "," + String.valueOf(endTime) + ")'").append(":");
			textBody.append("text=").append(text3).append(":");
			textBody.append("fontsize=").append(String.valueOf(fontsize3)).append(":");
			textBody.append("fontfile=").append(FontManager.getInstance().getFontFilePath(FontManager.NAUM_GOTHIC)).append(":");
			textBody.append("fontcolor=").append(fontColor).append(":");
			textBody.append("x=(w-tw)/2").append(":");
			textBody.append("y=(h+th+line_h)/2");
		}


		String preset = "medium"; // ultrafast,superfast,veryfast,faster,fast,medium,slow,slower,veryslow,placebo

		Log.d(TAG,"input = " + input);
		Log.d(TAG,"output = " + output);
		Log.d(TAG,"textbody = " + textBody.toString());
		Log.d(TAG,"preset = " + preset);



//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-vcodec","copy","-acodec","copy","-framerate","30","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
//		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),"-video_track_timescale","2997","-profile:v","baseline", "-strict", "-2", "-preset", preset, "-s", resolution, output};
		// ffmpeg info : if tbn 2997 -> video_track_timescale 2997. sample video is 2997 tbn.
		String[] cmd = new String[]{"-y", "-i", input, "-vf", textBody.toString(),
				"-video_track_timescale",tbr, "-strict", "-2",
				"-preset", preset, output};

		return cmd;
	}

	public String[] getToMergeAudio(String output, String input, String audio) {
		//shortest 옵션은 input source 하나라도 끝나면 인코딩을 중지한다.
		String[] cmd = new String[]{"-i", input, "-i", audio, "-map", "0:0", "-map", "1:0", "-shortest",  output};
		return cmd;
	}
}


//	There can be achieved without using map also.
//
//	ffmpeg -i video.mp4 -i audio.mp3 output.mp4
//
//	In case you want the output.mp4 to stop as soon as one of the input stops (audio/video) then use
//
//-shortest
//
//	For example: ffmpeg -i video.mp4 -i audio.mp3 -shortest output.mp4
//
//	This will make sure that the output stops as and when any one of the inputs is completed.
//
//	Since you have asked that you want to do it with map. this is how you do it:
//
//	ffmpeg -i video.mp4 -i audio.mp3 -map 0:0 -map 1:0 -shortest output.mp4
//
//	Now, since you want to retain the audio of the video file, consider you want to merge audio.mp3 and  video.mp4. These are the steps:
//
//	Extract audio from the video.mp4
//	ffmpeg -i video.mp4 1.mp3
//
//	Merge both audio.mp3 and 1.mp3
//	ffmpeg -i audio.mp3 -i 1.mp3  -filter_complex amerge -c:a libmp3lame -q:a 4 audiofinal.mp3
//
//	Remove the audio from video.mp4 (this step is not required. but just to do it properly)
//	ffmpeg -i video.mp4 -an videofinal.mp4
//
//	Now merge audiofinal.mp3 and videofinal.mp4
//	ffmpeg  -i videofinal.mp4 -i audiofinal.mp3 -shortest final.mp4
//
//	note: in the latest version of ffmpeg it will only prompt you to use '-strict -2' in case it does then use this:
//
//	ffmpeg  -i videofinal.mp4 -i audiofinal.mp3 -shortest -strict -2 final.mp4

