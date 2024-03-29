package com.jylee.videoeditor.custom;

import android.util.Log;

import com.jylee.videoeditor.VideoEditorServiceListener;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.util.StaticVideoManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-03-17.
 */

public class StateFinalVideo implements State {
	private static final String TAG = "StateFinalVideo";

	private ConcatTextVideoContext mContext;
	private FFmpegExcutorListener mFFmpegExcutorListener;
	private VideoEditorServiceListener mVideoEditorServiceListener;
	private ConcatTextVideoProperty mProperty;
	private FFMpegCustomUtil mFFmpegUtil = new FFMpegCustomUtil();

	private final int FINISHED_ADD_TEXT_PER = 70;
	private final int START_MERGE_VIDEO_PER = 75;
	private final int START_MERGE_AUDIO_PER = 81;
	private final int START_REMOVE_TEMP_PER = 95;

	@Override
	public boolean start(ConcatTextVideoContext context, ConcatTextVideoProperty property, VideoEditorServiceListener listener) {
		this.mContext = context;
		this.mFFmpegExcutorListener = context;
		this.mVideoEditorServiceListener = listener;
		this.mProperty = property;
		mVideoEditorServiceListener.onStartToConvert();
		if(property.getEmblem() == ""){
			mVideoEditorServiceListener.onErrorToConvert("no such emblem file");
			return false;
		}
		if(property.getVideoList().isEmpty()){
			mVideoEditorServiceListener.onErrorToConvert("no such video list");
			return false;
		}
		mProperty.setStatus(ConcatTextVideoProperty.StateType.GET_INFO);
		mFFmpegUtil.getInfo(mFFmpegExcutorListener,property.getEmblem());
		return false;
	}

	@Override
	public boolean cancel() {
		mFFmpegUtil.cancel();
		return false;
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
			if(progressTime != -1 && mProperty.getDuration() != 0)
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
			StaticVideoManager.getInstance().copyFileFromAsset(mProperty.getEmblem(),
					mProperty.getMakeFolder() + File.separator + mProperty.getEmblem());
				mFFmpegUtil.drawText(mFFmpegExcutorListener,mProperty.getMp4Step1File(),
						mProperty.getMakeFolder() + File.separator + mProperty.getEmblem(),
						mProperty.getText1(),
						mProperty.getText2(),
						mProperty.getText3(),
						mProperty.getVideoTbr());
		} else if(mProperty.getStatus()  == ConcatTextVideoProperty.StateType.ADD_TEXT){
			mProperty.setStatus(ConcatTextVideoProperty.StateType.MERGE_VIDEO);
			ArrayList<String> list = mProperty.getVideoList();
			list.add(0,mProperty.getMp4Step1File());
			if(mProperty.getIntro() != "") {
//				list.add(0, mProperty.getIntro());
				//If you want to test from an external storage, you can use it.
//				mFFmpegUtil.copy(mProperty.getIntro(),mProperty.getMakeFolder() + "/intro.mp4");
//				list.add(0, mProperty.getMakeFolder() + "/intro.mp4");
				StaticVideoManager.getInstance().copyFileFromAsset(mProperty.getIntro(),
						mProperty.getMakeFolder() + File.separator + mProperty.getIntro());
				list.add(0, mProperty.getMakeFolder() + File.separator + mProperty.getIntro());
			}
			if(mProperty.getEnding() != "") {
//				list.add(mProperty.getEnding());
				//If you want to test from an external storage, you can use it.
//				mFFmpegUtil.copy(mProperty.getEnding(), mProperty.getMakeFolder() + "/ending.mp4");
//				list.add(mProperty.getMakeFolder() + "/ending.mp4");
				StaticVideoManager.getInstance().copyFileFromAsset(mProperty.getEnding(),
						mProperty.getMakeFolder() + File.separator + mProperty.getEnding());
				list.add(mProperty.getMakeFolder() + File.separator + mProperty.getEnding());
			}
//			Log.d(TAG,"list = " + list.toString());
			mFFmpegUtil.concatVideo(mFFmpegExcutorListener,mProperty,mProperty.getMp4Step2File(), list);

		}else if(mProperty.getStatus() == ConcatTextVideoProperty.StateType.MERGE_VIDEO){
				mProperty.setStatus(ConcatTextVideoProperty.StateType.MERGE_AUDIO);
				StaticVideoManager.getInstance().copyFileFromAsset(mProperty.getAudio(),
					mProperty.getMakeFolder() + File.separator + mProperty.getAudio());
				File step2 = new File(mProperty.getMp4Step2File());
				File audio = new File(mProperty.getMakeFolder() + File.separator + mProperty.getAudio());
				if(step2.isFile() == false ){
					mVideoEditorServiceListener.onErrorToConvert("no exist step2 file");
					finishToConvert();
					return;
				}
				if(audio.isFile()  == false) {
					mVideoEditorServiceListener.onErrorToConvert("no exist audio file");
					finishToConvert();
					return;
				}
				mContext.getMp4ParserCustomUtil().convert(mProperty.getOutput(),mProperty.getMp4Step2File(),
						mProperty.getMakeFolder() + File.separator + mProperty.getAudio());
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
		mVideoEditorServiceListener.onProgressToConvert(START_MERGE_AUDIO_PER);
	}

	@Override
	public void onFinishMp4Parser(int jobType, String outFile) {
		finishToConvert();
	}

	private void finishToConvert() {
		mVideoEditorServiceListener.onProgressToConvert(START_REMOVE_TEMP_PER);
		File file = new File(mProperty.getMp4Step1File());
		if(file.isFile() && file.exists()) file.delete();
		file = new File(mProperty.getMp4Step2File());
		if(file.isFile() && file.exists()) file.delete();
		file = new File(mProperty.getMakeFolder() + File.separator +  "fflist.txt");
		if(file.isFile() && file.exists()) file.delete();
		file = new File(mProperty.getMakeFolder() + File.separator + mProperty.getEmblem());
		if(file.isFile() && file.exists() && mProperty.getEmblem() != "") file.delete();
		file = new File(mProperty.getMakeFolder() + File.separator + mProperty.getEnding());
		if(file.isFile() && file.exists() && mProperty.getEnding() != "") file.delete();
		file = new File(mProperty.getMakeFolder() + File.separator + mProperty.getIntro());
		if(file.isFile() && file.exists() && mProperty.getIntro() != "") file.delete();
		file = new File(mProperty.getMakeFolder() + File.separator + mProperty.getAudio());
		if(file.isFile() && file.exists() && mProperty.getAudio() != "") file.delete();

		mProperty.setStatus(ConcatTextVideoProperty.StateType.WATTING);
		mVideoEditorServiceListener.onProgressToConvert(100);
		mVideoEditorServiceListener.onFininshToConvert();
		mContext.setState(null); //don't remove
	}

}
