package com.jylee.videoeditor;

import android.content.Context;
import android.util.Log;

import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jooyoung on 2018-02-03.
 * facade pattern
 */

public class VideoEditorService implements FFmpegExcutorListener {
	private static final String TAG = "VideoEditorService";

	public enum StateType {
		WATTING, GET_INFO, ADD_TEXT, MERGE_VIDEO,
	}

	private Context mContext = null;
	private Mp4Parser mMp4Parser = null;
	private VideoEditorServiceListener mListener = null;
	private VideoEditorProperty mProperty = null;


	public  VideoEditorService(Context context, VideoEditorServiceListener listener) {
		mContext = context;
		mListener = listener;
		VideoEditorMp4ParserListener videoEditorMp4ParserListener= new VideoEditorMp4ParserListener();
		mMp4Parser = new Mp4Parser(videoEditorMp4ParserListener);
		FFmpegExcutor.getInstance(context);
		mProperty = new VideoEditorProperty();
	}

	public void makeVideo(String outputFile,String introFile,  ArrayList<String> videoList, String audio, String text)
	{
		if(mProperty.getStatus() == StateType.WATTING) {
			mProperty.setStatus(StateType.GET_INFO);
			mProperty.setIntro(introFile);
			mProperty.setVideoList(videoList);
			mProperty.setOutput(outputFile);
			mProperty.setAudio(audio);
			mProperty.setText(text);
			getInfo(outputFile);
		}
	}

	private void getInfo(String outputFile)
	{
		FFmpegExcutor.getInstance().run(FFmpegExcutor.getInstance().getCmdPackage().getInfo(outputFile),this);
	}

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

	private void drawText(String outputFile, String inputFile, String text) {
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text, 72, 52, 50,"white", 0, 5, 1280, 720);
//		FFmpegExcutor.getInstance(mContext).run(FFmpegCmdPackage.getInstance().getToAddTextCmd(outputFile, inputFile, text ),this);
		FFmpegExcutor.getInstance().run(cmd,this);
	}

	private void concetVideo(String outputFile, ArrayList<String> videoList, String mp3) {
		StringBuffer fileList = new StringBuffer ();

		for (String video : videoList){
			File file = new File(video);
			if(file.exists()) {
				video.replace(mProperty.getTempFolder(),"");
				fileList.append("file \'");
				fileList.append(video);
				fileList.append("\'\n");
			}
		}

		Log.d("TAG",fileList.toString());
		writeFile(mProperty.getTempFolder(),"fflist.txt",fileList.toString());
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getConcatVideoCmd(outputFile,mProperty.getTempFolder()+"fflist.txt");
		FFmpegExcutor.getInstance().run(cmd,this);
	}

	private void writeFile(String dir,String baseName ,String text) {
		try {
			File file = new File(dir,baseName);
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(text.getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//ffmpeg
	@Override
	public void onStart() {
		Log.d(TAG,"start mp4parser");
		mListener.onProgressToConvert(0);
	}

	@Override
	public void onProgress(String message) {
		Log.d("TAG","onProgress = " + message);

	}

	@Override
	public void onFailure(String message) {
		if(mProperty.getStatus()  == StateType.GET_INFO){
			String[] lines = message.split(System.getProperty("line.separator"));
			for(String  str : lines) {
				Pattern word = Pattern.compile("Duration:");
				Matcher match = word.matcher(str);
				if(match.find()) {
					Log.d("TAG"," str = " + str);
					mProperty.setDuration(10);
				}else {
					mProperty.setDuration(-1);
				}
			}
		}
		Log.d("TAG","onFailure = " + message);
	}

	@Override
	public void onSuccess(String message) {
		Log.d(TAG,"onSuccess = " + message);
	}
	@Override
	public void onFinish() {
		Log.d(TAG,"onFinish");
		if(mProperty.getStatus()  == StateType.GET_INFO){
			mProperty.setStatus(StateType.ADD_TEXT);
			drawText(mProperty.getMp4TempFile(), mProperty.getIntro(), mProperty.getText());
		} else if(mProperty.getStatus()  == StateType.ADD_TEXT){
			mProperty.setStatus(StateType.MERGE_VIDEO);
			ArrayList <String> list = mProperty.getVideoList();
//			list.add(0,mProperty.getMp4TempFile());
			Log.d(TAG,"list = " + list.toString());
			concetVideo(mProperty.getOutput(),list,mProperty.getAudio());
//			convert(mProperty.getOutput(),list,mProperty.getAudio());
		}
	}

	@Override
	public void onError(String exception) {
		Log.d(TAG,"onError = " + exception);
	}

	public class VideoEditorMp4ParserListener implements Mp4ParserListener {

		@Override
		public void onStart() {

		}

		@Override
		public void onFininsh(int jobType, String outFile) {

		}
	}

	public class VideoEditorProperty {
		private StateType status = StateType.WATTING;
		private String text;
		private String output;
		private String intro;
		private String audio;
		private ArrayList<String> videoList;
		private int duration;

		private String tempFolder =  mContext.getFilesDir().getAbsolutePath() + "/storage/emulated/0/Download/mp4parser/";
		private String tempMp4File =  mContext.getFilesDir().getAbsolutePath() + "/" + "temp.mp4";

		public String getTempFolder() {
			return tempFolder;
		}

		public String getAudio() {
			return audio;
		}

		public void setAudio(String audio) {
			this.audio = audio;
		}

		public ArrayList<String> getVideoList() {
			return videoList;
		}

		public void setVideoList(ArrayList<String> videoList) {
			this.videoList = videoList;
		}

		public String getIntro() {
			return intro;
		}

		public void setIntro(String intro) {
			this.intro = intro;
		}

		public int getDuration() {
			return duration;
		}

		public void setDuration(int duration) {
			this.duration = duration;
		}

		public String getMp4TempFile() {
			return tempMp4File;
		}

		public void setMp4TempFile(String tempFile) {
			this.tempMp4File = tempFile;
		}

		public StateType getStatus() {
			return status;
		}

		public void setStatus(StateType status) {
			this.status = status;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			this.output = output;
		}
	}
}
