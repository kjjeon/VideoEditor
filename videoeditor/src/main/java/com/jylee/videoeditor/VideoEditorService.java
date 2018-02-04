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
		mMp4Parser.startToAppend(outputFile, videoList);
	}

	public void convert(String outputFile, String video, String bgm ) {
		mMp4Parser.startToAddBgm(outputFile, video, bgm);
	}

	@Override
	public void onStartToAppend() {
		Log.d("TAG","start append");
		mListener.onStartToConvert();

	}

	@Override
	public void onFininshToAppend() {
		Log.d("TAG","finish append");
		mListener.onFininshToConvert();
	}

	@Override
	public void onStartToAddBgm() {
		Log.d("TAG","start addBgm");
	}

	@Override
	public void onFininshToAddBgm() {
		Log.d("TAG","finish addBgm");

	}
}
