package com.jylee.videoeditor;

import android.content.Context;
import android.util.Log;

import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-02-03.
 * facade pattern
 */

public class VideoEditorService implements Mp4ParserListener,FFmpegExcutorListener {
	private static final String TAG = "VideoEditorService";

	public enum StateType {
		WATTING, MAKING_DAY_VIDEO, MAKING_FULL_VIDEO
	}

	private Context mContext = null;
	private Mp4Parser mMp4Parser = null;
	private VideoEditorServiceListener mListener = null;
	private VideoEditorProperty mProperty = null;


	public  VideoEditorService(Context context, VideoEditorServiceListener listener) {
		mContext = context;
		mListener = listener;
		mMp4Parser = new Mp4Parser(this);
		FFmpegExcutor.getInstance(context);
		mProperty = new VideoEditorProperty();
	}

	public void makeDayVideo(String outputFile, String inputFile, String text)
	{
		mProperty.setStatus(StateType.MAKING_DAY_VIDEO);
		drawText(outputFile, inputFile, text);
	}

	public void makeFullVideo(String outputFile,  ArrayList<String> videoList, String audio, String text)
	{
		mProperty.setStatus(StateType.MAKING_FULL_VIDEO);
		mProperty.setOutput(outputFile);
		mProperty.setText(text);
		convert(mProperty.getMp4TempFile(), videoList, audio);
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
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text, 72, 52, 50,"white", 0, 5, 640, 360);
//		FFmpegExcutor.getInstance(mContext).run(FFmpegCmdPackage.getInstance().getToAddTextCmd(outputFile, inputFile, text ),this);
		if(mProperty.getStatus() ==  StateType.MAKING_DAY_VIDEO){
			mListener.onStartToConvert();
		}
		FFmpegExcutor.getInstance().run(cmd,this);
	}

	//mp4parser
	@Override
	public void onStart() {
		Log.d(TAG,"start mp4parser");
		mListener.onStartToConvert();
	}

	@Override
	public void onFininsh(int jobType,String outFile) {
		Log.d(TAG,"finish converting");
		if(mProperty.getStatus()  == StateType.MAKING_DAY_VIDEO)
			mListener.onFininshToConvert();
		else if(mProperty.getStatus()  == StateType.MAKING_FULL_VIDEO)
			drawText(mProperty.getOutput(),mProperty.getMp4TempFile(),mProperty.getText());
	}

	//ffmpeg
	@Override
	public void onProgress(String message) {
		Log.d("TAG","onProgress = " + message);
	}

	@Override
	public void onFailure(String message) {
		Log.d("TAG","onFailure = " + message);
	}

	@Override
	public void onSuccess(String message) {
		Log.d(TAG,"onSuccess = " + message);
	}
	@Override
	public void onFinish() {
		Log.d(TAG,"onFinish");
		mProperty.setStatus(StateType.WATTING);
		mListener.onFininshToConvert();
	}

	@Override
	public void onError(String exception) {
		Log.d(TAG,"onError = " + exception);
		mProperty.setStatus(StateType.WATTING);
	}

	public class VideoEditorProperty {
		private StateType status = StateType.WATTING;
		private String text;
		private String output;
		private String tempMp4File =  mContext.getFilesDir().getAbsolutePath() + "/" + "temp.mp4";

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
