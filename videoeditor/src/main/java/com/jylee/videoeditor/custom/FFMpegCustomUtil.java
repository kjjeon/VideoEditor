package com.jylee.videoeditor.custom;

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
//		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text,
//				72, 52, 50,"white", 0, 0, 1280, 720,tbn);
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextAlignTextCmd(outputFile, inputFile, text
				, 70,"white", 0, 0, 1280, 720,tbn);

		FFmpegExcutor.getInstance().run(cmd,listener);
	}
	public void drawText(FFmpegExcutorListener listener, String outputFile, String inputFile, String text1,String text2, String text3, String tbn) {
		int newLine = 5; // 5글자
		if(tbn == "") tbn ="2997";

		if(text2.contains("\n")) {
			Log.d(TAG,"new Line  = " +  text2.indexOf("\n"));
			int lastIndex = text2.length() > text2.indexOf("\n") + newLine ? text2.indexOf("\n") + 1 + newLine : text2.length();
			text3 = text2.substring(text2.indexOf("\n") + 1, lastIndex);
			text2 = text2.substring(0,text2.indexOf("\n"));
			Log.d(TAG,"text2  = " + text2);
			Log.d(TAG,"text3  = " + text3);
		}

		else if(text2.length() > newLine) {
			Log.d(TAG,"length  = " + text2.length());
			int lastIndex = text2.length() > newLine * 2 ? newLine * 2 : text2.length();
			text3 = text2.substring(newLine,lastIndex);
			text2 = text2.substring(0,newLine);
			Log.d(TAG,"text2  = " + text2);
			Log.d(TAG,"text3  = " + text3);
		}


		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddCenterAlignTextCmd(outputFile, inputFile,
				40, text1,50,text2,50,text3,"black", 0, 0,tbn);
		FFmpegExcutor.getInstance().run(cmd,listener);
	}


	public void concatVideo(FFmpegExcutorListener listener, ConcatTextVideoProperty property, String outputFile, ArrayList<String> videoList) {
		StringBuffer fileList = new StringBuffer ();

		for (String video : videoList){
			File file = new File(video);
			if(file.exists()) {
				String path = video;
				String base = property.getMakeFolder();
//				String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();
				String relative = makeRelativePath(base,file.getParentFile().getAbsolutePath());
				if(relative == null) {
					relative = file.getName();
				}else {
					relative =  makeRelativePath(base,file.getParentFile().getAbsolutePath())  + File.separator + file.getName();
				}
				fileList.append("file \'");
				fileList.append(relative);
				fileList.append("\'\n");
			}
		}

		Log.d("TAG",fileList.toString());
		writeFile(property.getMakeFolder() + File.separator +  "fflist.txt",fileList.toString());
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getConcatVideoCmd(outputFile,property.getMakeFolder()+ File.separator + "fflist.txt");
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

	public  void copy(String src, String dst){
		try {
			File file = new File(dst);
			if (file.exists()) return;

			InputStream is = null;
			OutputStream os = null;

			is = new FileInputStream(src);
			os = new FileOutputStream(dst);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}
			is.close();
			os.flush();
			os.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String makeRelativePath(String fromPath, String toPath) {

		if( fromPath == null || fromPath.isEmpty() )    return null;
		if( toPath == null || toPath.isEmpty() )        return null;

		StringBuilder relativePath = null;

		fromPath = fromPath.replaceAll("\\\\", File.separator);
		toPath = toPath.replaceAll("\\\\", File.separator);

		if (fromPath.equals(toPath) == true) {

		} else {
			String[] absoluteDirectories = fromPath.split(File.separator);
			String[] relativeDirectories = toPath.split(File.separator);

			//Get the shortest of the two paths
			int length = absoluteDirectories.length < relativeDirectories.length ?
					absoluteDirectories.length : relativeDirectories.length;

			//Use to determine where in the loop we exited
			int lastCommonRoot = -1;
			int index;

			//Find common root
			for (index = 0; index < length; index++) {
				if (absoluteDirectories[index].equals(relativeDirectories[index])) {
					lastCommonRoot = index;
				} else {
					break;
					//If we didn't find a common prefix then throw
				}
			}
			if (lastCommonRoot != -1) {
				//Build up the relative path
				relativePath = new StringBuilder();
				//Add on the ..
				for (index = lastCommonRoot + 1; index < absoluteDirectories.length; index++) {
					if (absoluteDirectories[index].length() > 0) {
						relativePath.append("../");
					}
				}
				for (index = lastCommonRoot + 1; index < relativeDirectories.length - 1; index++) {
					relativePath.append(relativeDirectories[index] + File.separator);
				}
				relativePath.append(relativeDirectories[relativeDirectories.length - 1]);
			}
		}

		return relativePath == null ? null : relativePath.toString();
	}
}
