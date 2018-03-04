package com.jylee.videoeditor;

import android.content.Context;

import com.jylee.videoeditor.custom.ConcatTextVideo;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-03-04.
 */

public class VideoEditorService {

	private Context mContext = null;
	private ConcatTextVideo mConcatTextVideo = null;
	private VideoEditorServiceListener mListener = null;


	public VideoEditorService(Context context, VideoEditorServiceListener listener) {
		mContext = context;
		mListener = listener;
		FFmpegExcutor.getInstance(context);
		mConcatTextVideo = new ConcatTextVideo("/storage/emulated/0/Download/mp4parser/",mListener);
	}


	public boolean makeVideo(String outputFile, String introFile, ArrayList<String> videoList, String audio, String text)
	{
		return mConcatTextVideo.makeVideo(outputFile, introFile, videoList, audio, text);
	}

	public boolean isRunning()
	{
		return mConcatTextVideo.isRunning();
	}

	public boolean stop()
	{
		return mConcatTextVideo.stop();
	}
}
