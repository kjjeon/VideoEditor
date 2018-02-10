package com.jylee.videoeditor;

import android.util.Log;

import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-02-03.
 * facade pattern
 */

public class VideoEditorService implements Mp4ParserListener {
	private Mp4Parser mMp4Parser = null;;
	private VideoEditorServiceListener mListener = null;


	public  VideoEditorService(VideoEditorServiceListener listener) {
		mListener = listener;
		mMp4Parser = new Mp4Parser(this);
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

	public void convert(String outputFile, String video, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	@Override
	public void onStart() {
		Log.d("TAG","start converting");
		mListener.onStartToConvert();

	}

	@Override
	public void onFininsh(int jobType,String outFile) {
		Log.d("TAG","finish converting");
		mListener.onStartToConvert();

	}


}
