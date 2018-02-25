package com.jylee.videoeditor;

import android.content.Context;
import android.util.Log;

import com.jylee.videoeditor.ffmpeg.FFmpegCmdPackage;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;
import com.jylee.videoeditor.util.FontManager;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-02-03.
 * facade pattern
 */

public class VideoEditorService implements Mp4ParserListener,FFmpegExcutorListener {
	private static final String TAG = "VideoEditorService";
	private Context mContext = null;
	private Mp4Parser mMp4Parser = null;
	private VideoEditorServiceListener mListener = null;


	public  VideoEditorService(Context context, VideoEditorServiceListener listener) {
		mContext = context;
		mListener = listener;
		mMp4Parser = new Mp4Parser(this);
		FontManager.getInstance(context);
		FontManager.getInstance().initNanumGothic(context);
	}

	public void convert(String outputFile, ArrayList<String> videoList) {
		mMp4Parser.run(outputFile, videoList,null);
	}

	public void convert(String outputFile, ArrayList<String> videoList, ArrayList<String> audioList) {
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	public void convert(String outputFile,  ArrayList<String> videoList, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	public void drawText(String outputFile, String inputFile, String text) {

		FFmpegExcutor.getInstance(mContext).run(FFmpegCmdPackage.getInstance().getToAddTextCmd(outputFile, inputFile, text ),this);
//		FFmpegExcutor.getInstance(mContext).run(FFmpegCmdPackage.getInstance().getVersionCmd(),this);
	}

	public void convert(String outputFile, String video, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	//mp4parser
	@Override
	public void onStart() {
		Log.d(TAG,"start converting");
		mListener.onStartToConvert();
	}

	@Override
	public void onFininsh(int jobType,String outFile) {
		Log.d(TAG,"finish converting");
		mListener.onStartToConvert();
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

	}

	@Override
	public void onError(String exception) {

	}


}
