package com.jylee.videoeditor.custom;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.jylee.videoeditor.VideoEditorServiceListener;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;

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
 * Created by jooyoung on 2018-02-03.
 * facade pattern
 */

public class ConcatTextVideo implements FFmpegExcutorListener {
	private static final String TAG = "ConcatTextVideo";
	private final int CONCAT_PER = 90; // 동영상 붙이기 까지 기준 표기 퍼센트
	private Mp4Parser mMp4Parser = null;
	private VideoEditorServiceListener mListener = null;
	private ConcatTextVideoProperty mProperty = null;


	public ConcatTextVideo(String baseDirectory, VideoEditorServiceListener listener) {
		mListener = listener;
		mProperty = new ConcatTextVideoProperty(baseDirectory);

		//mp4parser
		VideoEditorMp4ParserListener videoEditorMp4ParserListener= new VideoEditorMp4ParserListener();
		mMp4Parser = new Mp4Parser(videoEditorMp4ParserListener);

	}

	public boolean makeVideo(String outputFile, String introFile,  ArrayList<String> videoList, String audio, String text)
	{
		if(mProperty.getStatus() == ConcatTextVideoProperty.StateType.WATTING) {
			mListener.onStartToConvert();
			mProperty.setStatus(ConcatTextVideoProperty.StateType.GET_INFO);
			mProperty.setIntro(introFile);
			mProperty.setVideoList(videoList);
			mProperty.setOutput(outputFile);
			mProperty.setAudio(audio);
			mProperty.setText(text);
			getInfo(introFile);
			return true;
		}else {
			return false;
		}
	}

	public boolean isRunning() {
		if(mProperty.getStatus() == ConcatTextVideoProperty.StateType.WATTING) {
			return false;
		}
		return true;
	}

	public boolean stop() {
		if(mProperty.getStatus() != ConcatTextVideoProperty.StateType.WATTING || FFmpegExcutor.getInstance().isRunning() == true){
			if(FFmpegExcutor.getInstance().isRunning() == true ) {
				FFmpegExcutor.getInstance().kill();
			}
			mProperty.setStatus(ConcatTextVideoProperty.StateType.WATTING);
			return true;
		}else{
			return false;
		}
	}

	//ffmpeg
	@Override
	public void onStart() {
		Log.d(TAG,"start ffmpeg converting");
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.GET_INFO) {
			mListener.onProgressToConvert(0);
		}else if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.MERGE_VIDEO) {
			mListener.onProgressToConvert(CONCAT_PER);
		}
	}

	@Override
	public void onProgress(String message) {
//		Log.d("TAG","onProgress = " +message);
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.ADD_TEXT) {
			Log.d("TAG", "progress= " + parseProgressTime(message) + "/" + mProperty.getDuration());
			mListener.onProgressToConvert((CONCAT_PER-10) * parseProgressTime(message)/mProperty.getDuration());
		}
	}

	@Override
	public void onFailure(String message) {
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.GET_INFO){
			mProperty.setDuration(parseDuration(message));
		}
//		Log.d("TAG","onFailure = " + message);
	}

	@Override
	public void onSuccess(String message) {
		Log.d(TAG,"ffmpeg onSuccess");

//		Log.d(TAG,"onSuccess = " + message);
	}

	@Override
	public void onFinish() {
		Log.d(TAG,"onFinish");
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.GET_INFO){
			mProperty.setStatus(ConcatTextVideoProperty.StateType.ADD_TEXT);
			drawText(mProperty.getMp4TempFile(), mProperty.getIntro(), mProperty.getText());
		} else if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.ADD_TEXT){
			mProperty.setStatus(ConcatTextVideoProperty.StateType.MERGE_VIDEO);
			ArrayList <String> list = mProperty.getVideoList();
			list.add(0,mProperty.getMp4TempFile());
//			Log.d(TAG,"list = " + list.toString());
			concatVideo(mProperty.getOutput(),list,mProperty.getAudio());
//			convert(mProperty.getOutput(),list,mProperty.getAudio()); //mp4parser not used
		}else {
			mProperty.setStatus(ConcatTextVideoProperty.StateType.WATTING);
			mListener.onProgressToConvert(100);
			mListener.onFininshToConvert();
			Log.d(TAG,"finish ffmpeg converting");
		}
	}

	@Override
	public void onError(String exception) {
		mListener.onErrorToConvert(exception);
		Log.d(TAG,"onError = " + exception);
	}


	private int parseDuration(String message) {
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

	private int parseProgressTime(String message) {
		Pattern word = Pattern.compile("time=");
		Matcher match = word.matcher(message);

		if(match.find()) {
//			Log.d(TAG,"time msg= " + message);
			String progressTime = message.split("time=")[1];
//			int hour = Integer.parseInt(progressTime.substring(0, 2));
			int minute = Integer.parseInt(progressTime.substring(3,5));
			int sec = Integer.parseInt(progressTime.substring(6,8));
			int msec = Integer.parseInt(progressTime.substring(9,11));
//			Log.d(TAG,"m= " + minute +"s=" + sec +  "msec= " + msec);
			return minute *6000 +  sec * 100 + msec;
		}
		return 0;
	}
	private void getInfo(String fileName)
	{
		FFmpegExcutor.getInstance().run(FFmpegExcutor.getInstance().getCmdPackage().getInfo(fileName),this);
	}


	private void drawText(String outputFile, String inputFile, String text) {
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text, 72, 52, 50,"white", 0, 5, 1280, 720);
//		FFmpegExcutor.getInstance().run(FFmpegCmdPackage.getInstance().getToAddTextCmd(outputFile, inputFile, text ),this);
		FFmpegExcutor.getInstance().run(cmd,this);
	}

	private void concatVideo(String outputFile, ArrayList<String> videoList, String mp3) {
		StringBuffer fileList = new StringBuffer ();

		File output = new File(outputFile);
		String parentDirName = output.getAbsoluteFile().getParent();
		Log.d(TAG,"parentDirName = " + parentDirName);

		for (String video : videoList){
			File file = new File(video);
			if(file.exists()) {
				String path = video;
				String base = parentDirName;
				String relative = new File(base).toURI().relativize(new File(path).toURI()).getPath();

//				video = video.replace(mProperty.getMakeFolder(),"");

				fileList.append("file \'");
				fileList.append(relative);
				fileList.append("\'\n");
			}
		}

		Log.d("TAG",fileList.toString());
		writeFile(mProperty.getMakeFolder() +  "fflist.txt",fileList.toString());
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getConcatVideoCmd(outputFile,mProperty.getMakeFolder()+ "/" + "fflist.txt");
		FFmpegExcutor.getInstance().run(cmd,this);

	}

	private void writeFile(String filePath ,String text) {
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
	private  void copy(File src, File dst) throws IOException {
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




/*
* mp4parser api
* have not used this library.
 */

	private void convert(String outputFile, ArrayList<String> videoList) {
		mMp4Parser.run(outputFile, videoList,null);
	}

	private void convert(String outputFile, ArrayList<String> videoList, ArrayList<String> audioList) {
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	private void convert(String outputFile,  ArrayList<String> videoList, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	private void convert(String outputFile, String video, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	private void convert(String outputFile, String video, String audio, String text) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mMp4Parser.run(outputFile, videoList, audioList);
	}


	public class VideoEditorMp4ParserListener implements Mp4ParserListener {

		@Override
		public void onStart() {

		}

		@Override
		public void onFininsh(int jobType, String outFile) {

		}
	}

}
