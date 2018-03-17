package com.jylee.videoeditor.custom;

import android.util.Log;

import com.jylee.videoeditor.VideoEditorServiceListener;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-03-17.
 */

public class StateDayVideo implements State {

	private static final String TAG = "StateDayVideo";

	private ConcatTextVideoContext mContext;
	private FFmpegExcutorListener mFFmpegExcutorListener;
	private VideoEditorServiceListener mVideoEditorServiceListener;
	private ConcatTextVideoProperty mProperty;
	private FFMpegCustomUtil mFFmpegUtil = new FFMpegCustomUtil();
	private final int START_ADD_TEXT_PER = 10;
	private final int FINISHED_ADD_TEXT_PER = 70;
	private final int START_MERGE_VIDEO_PER = 75;
	private final int START_REMOVE_TEMP_PER = 95;

	@Override
	public boolean start(ConcatTextVideoContext context, ConcatTextVideoProperty property, VideoEditorServiceListener listener) {
		this.mContext = context;
		this.mFFmpegExcutorListener = context;
		this.mVideoEditorServiceListener = listener;
		this.mProperty = property;
		mVideoEditorServiceListener.onStartToConvert();
		if(property.getVideoList().isEmpty()) {
			mVideoEditorServiceListener.onErrorToConvert("no such video list");
			return false;
		}
		mProperty.setStatus(ConcatTextVideoProperty.StateType.GET_INFO);
		mFFmpegUtil.getInfo(mFFmpegExcutorListener,property.getVideoList().get(0));
		mVideoEditorServiceListener.onProgressToConvert(START_ADD_TEXT_PER);
		return true;
	}

	@Override
	public boolean cancel() {
		mFFmpegUtil.cancel();
		return true;
	}

	@Override
	public void onStartFFmepg() {
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.GET_INFO) {
			mVideoEditorServiceListener.onProgressToConvert(0);
		}else if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.MERGE_VIDEO) {
			mVideoEditorServiceListener.onProgressToConvert(START_MERGE_VIDEO_PER);
		}
	}

	@Override
	public void onProgressFFmepg(String message) {
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.ADD_TEXT) {
			int progressTime = mFFmpegUtil.parseProgressTime(message);
			Log.d(TAG, "progress= " + progressTime + File.separator + mProperty.getDuration());
			if(progressTime != -1)
				mVideoEditorServiceListener.onProgressToConvert(FINISHED_ADD_TEXT_PER * progressTime/mProperty.getDuration());
		}
	}

	@Override
	public void onFailureFFmepg(String message) {
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.GET_INFO){
			mProperty.setDuration(mFFmpegUtil.parseDuration(message));
			mProperty.setVideoTbr(mFFmpegUtil.parseTbr(message));
		}

		Log.d(TAG,"onFailure = " + message);
	}

	@Override
	public void onSuccessFFmepg(String message) {
		Log.d(TAG,"ffmpeg onSuccess");
	}

	@Override
	public void onFinishFFmepg() {
		if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.GET_INFO){
			mProperty.setStatus(ConcatTextVideoProperty.StateType.ADD_TEXT);
			mFFmpegUtil.drawText(mFFmpegExcutorListener,mProperty.getMp4Step1File(), mProperty.getVideoList().get(0),
					mProperty.getText1(),
					mProperty.getVideoTbr());

		} else if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.ADD_TEXT){
			mProperty.setStatus(ConcatTextVideoProperty.StateType.MERGE_VIDEO);
			ArrayList<String> list = mProperty.getVideoList();
			list.remove(0);
			list.add(0,mProperty.getMp4Step1File());
			list.add(mProperty.getEnding());
			//If you want to test from an external storage, you can use it.
//			mFFmpegUtil.copy(mProperty.getEnding(),mProperty.getMakeFolder() + "/ending.mp4");
//			list.add(mProperty.getMakeFolder() + "/ending.mp4");

			mFFmpegUtil.concatVideo(mFFmpegExcutorListener,mProperty,mProperty.getOutput(), list);
		} else {
			File file = new File(mProperty.getOutput());
			if(!file.exists()) {
				mVideoEditorServiceListener.onErrorToConvert("fail create output file");
			}
			finishToConvert();
			Log.d(TAG,"finish ffmpeg converting");
		}
	}

	@Override
	public void onErrorFFmepg(String exception) {
		mVideoEditorServiceListener.onErrorToConvert(exception);
		Log.d(TAG,"onError = " + exception);
	}

	@Override
	public void onStartMp4Parser() {
		mVideoEditorServiceListener.onProgressToConvert(START_MERGE_VIDEO_PER);
	}

	@Override
	public void onFinishMp4Parser(int jobType, String outFile) {
		finishToConvert();
	}

	private void finishToConvert() {
		mVideoEditorServiceListener.onProgressToConvert(START_REMOVE_TEMP_PER);
		File file = new File(mProperty.getMp4Step1File());
		if(file.exists()) file.delete();
		file = new File(mProperty.getMp4Step2File());
		if(file.exists()) file.delete();
		file = new File(mProperty.getMakeFolder() + File.separator +  "fflist.txt");
		if(file.exists()) file.delete();
		mProperty.setStatus(ConcatTextVideoProperty.StateType.WATTING);
		mVideoEditorServiceListener.onProgressToConvert(100);
		mVideoEditorServiceListener.onFininshToConvert();
		mContext.setState(null); //don't remove
	}
}