package com.jylee.videoeditor.custom;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jooyoung on 2018-03-17.
 */

public class FFMpegCustomUtil {
	private static final String TAG = "FFMpegCustomUtil";
	public boolean cancel()
	{
		return FFmpegExcutor.getInstance().kill();
	}
	public int parseDuration(String message) {
		String[] lines = message.split(System.getProperty("line.separator"));
		for(String  str : lines) {
//			Log.d(TAG,"str= " + str);
			Pattern word = Pattern.compile("Duration:");
			Matcher match = word.matcher(str);
			if(match.find()) {
//				Log.d(TAG,"match= " + str);
				String[] items = str.split(",");
				String duration = items[0].split(": ")[1];
//				Log.d(TAG,"duration= " + duration);
//				int hour = Integer.parseInt(duration.substring(0, 2));
				int minute = Integer.parseInt(duration.substring(3,5));
				int sec = Integer.parseInt(duration.substring(6,8));
				int msec = Integer.parseInt(duration.substring(9,11));
//				Log.d(TAG,"m= " + minute +"s=" + sec +  "msec= " + msec);
				return minute * 6000 + sec *100 + msec;
			}
		}
		return 0;
	}

	public String parseTbr(String message) {
		String[] lines = message.split(System.getProperty("line.separator"));
		for(String  str : lines) {
//			Log.d(TAG,"str= " + str);
			Pattern word = Pattern.compile("Video:");
			Matcher match = word.matcher(str);
			if(match.find()) {
				Pattern word2 = Pattern.compile("tbr");
				Matcher match2 = word2.matcher(str);
				if(match2.find()){
					str = str.substring(match2.start(),str.length());
					String[] items = str.split(" ");
					Log.d(TAG,"str=" + str);
					Log.d(TAG,"items[1] =" +items[1]);
					return items[1];
				}
			}
		}
		return "";
	}

	public int parseProgressTime(String message) {
		Pattern word = Pattern.compile("time=");
		Matcher match = word.matcher(message);

		if(match.find()) {
//			Log.d(TAG,"time msg= " + message);
			String progressTime = message.split("time=")[1];
			int minute = Integer.parseInt(progressTime.substring(3,5));
			int sec = Integer.parseInt(progressTime.substring(6,8));
			int msec = Integer.parseInt(progressTime.substring(9,11));
//			Log.d(TAG,"m= " + minute +"s=" + sec +  "msec= " + msec);
			return minute *6000 +  sec * 100 + msec;
		}
		return -1;
	}
	public void getInfo(FFmpegExcutorListener listener, String fileName)
	{
		FFmpegExcutor.getInstance().run(FFmpegExcutor.getInstance().getCmdPackage().getInfo(fileName),listener);
	}


	public void drawText(FFmpegExcutorListener listener, String outputFile, String inputFile, String text, String tbn) {
		if(tbn == "") tbn ="2997";
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text,
				72, 52, 50,"white", 0, 0, 1280, 720,tbn);
//		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddCenterAlignTextCmd(outputFile, inputFile, text,
//				72,"black", 0, 0,tbn);
		FFmpegExcutor.getInstance().run(cmd,listener);
	}
	public void drawText(FFmpegExcutorListener listener, String outputFile, String inputFile, String text1,String text2, String text3, String tbn) {
		if(tbn == "") tbn ="2997";
//		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text,
//				72, 52, 50,"black", 0, 0, 1280, 720,tbr);
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddCenterAlignTextCmd(outputFile, inputFile,
				30, text1,50,text2,50,text3,"black", 0, 0,tbn);

		FFmpegExcutor.getInstance().run(cmd,listener);
	}


	public void concatVideo(FFmpegExcutorListener listener, ConcatTextVideoProperty property, String outputFile, ArrayList<String> videoList) {
		StringBuffer fileList = new StringBuffer ();

		for (String video : videoList){
			File file = new File(video);
			if(file.exists()) {
				String path = video;
				String base = property.getMakeFolder();
				String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();

				fileList.append("file \'");
				fileList.append(relative);
				fileList.append("\'\n");
			}
		}

		Log.d("TAG",fileList.toString());
		writeFile(property.getMakeFolder() + "/" +  "fflist.txt",fileList.toString());
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getConcatVideoCmd(outputFile,property.getMakeFolder()+ "/" + "fflist.txt");
		FFmpegExcutor.getInstance().run(cmd,listener);

	}

	public void mergeAudio(FFmpegExcutorListener listener, String outputFile, String inputFile, String audioFile) {
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToMergeAudio(outputFile,inputFile,audioFile);
		FFmpegExcutor.getInstance().run(cmd,listener);
	}

	public void writeFile(String filePath ,String text) {
		try {
			File file = new File(filePath);
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(text.getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public  void copy(File src, File dst) throws IOException {
		try (InputStream in = new FileInputStream(src)) {
			try (OutputStream out = new FileOutputStream(dst)) {
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
		}
	}
}
