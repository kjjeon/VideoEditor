package com.jylee.videoeditor.custom;

import com.jylee.videoeditor.VideoEditorServiceListener;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;

/**
 * Created by jooyoung on 2018-03-17.
 */

public class ConcatTextVideoContext implements FFmpegExcutorListener {

	private static final String TAG = "ConcatTextVideoContext";

	private State state = null;
	private VideoEditorServiceListener mVideoEditorServiceListener = null;
	private Mp4Parser mMp4Parser = null;
	private Mp4ParserCustomUtil mMp4ParserCustomUtil = null;

	public  ConcatTextVideoContext() {
		//mp4parser
		VideoEditorMp4ParserListener videoEditorMp4ParserListener =
				new ConcatTextVideoContext.VideoEditorMp4ParserListener();
		this.mMp4Parser =  new Mp4Parser(videoEditorMp4ParserListener);
		this.mMp4ParserCustomUtil = new Mp4ParserCustomUtil(this.mMp4Parser);
		setState(null);
	}

	public boolean makeDayVideo(String rootDirectory, String outputFileName, String endingFileName, String text, VideoEditorServiceListener listener) {
		ConcatTextVideoProperty property = new ConcatTextVideoProperty();
		property.setMakeFolder(rootDirectory);
		property.setOutput(rootDirectory + "/../" + outputFileName);
		property.setEnding(endingFileName);
//		property.setEnding(StaticVideoManager.getInstance().getDirectory() + File.separator + endingFileName);
		property.setText1(text);
		property.setTempFolder(rootDirectory);
		property.searchVideo(rootDirectory);

		if(getState() == null){
			setState(new StateDayVideo());
			return state.start(this,property,listener);
		}
		return false;
	}
	public boolean makeFinalVideo(String rootDirectory, String outputFileName, String emblemFileName, String introFileName, String endingFileName,
								  String audioFileName, String id, String title, VideoEditorServiceListener listener) {
		ConcatTextVideoProperty property = new ConcatTextVideoProperty();
		property.setMakeFolder(rootDirectory);
		property.setOutput(rootDirectory + "/../" + outputFileName);
		property.setText1(id);
		property.setText2(title);

//		if(introFileName == "")
			property.setIntro(introFileName);
//		else
//			property.setIntro(StaticVideoManager.getInstance().getDirectory() + File.separator + introFileName);

//		if(emblemFileName == "")
			property.setEmblem(emblemFileName);
//		else
//			property.setEmblem(StaticVideoManager.getInstance().getDirectory() + File.separator + emblemFileName);

//		property.setEnding(StaticVideoManager.getInstance().getDirectory() + File.separator + endingFileName);
		property.setEnding(endingFileName);
		property.setAudio(audioFileName);
		property.setTempFolder(rootDirectory);
		property.searchVideo(rootDirectory);
		mVideoEditorServiceListener = listener;
//			property.setCustomRuleDefault(rootDirectory, outputName, "", "", text,"","");

		if(getState() == null){
			setState(new StateFinalVideo());
			return state.start(this,property,listener);
		}
		return false;
	}

	public boolean isRunning(){
		return getState() == null ? false : true;
	}

	public boolean cancel(){
		return state.cancel();
	}



	Mp4ParserCustomUtil getMp4ParserCustomUtil() {
		 return mMp4ParserCustomUtil;
	}

	void setState(State state) {
		this.state = state;
	}

	State getState() {
		return this.state;
	}

	//ffmpeg
	@Override
	public void onStart() {
		if(state != null) state.onStartFFmepg();
	}

	@Override
	public void onProgress(String message) {
		if(state != null)
			state.onProgressFFmepg(message);
	}

	@Override
	public void onFailure(String message) {
		if(state != null) state.onFailureFFmepg(message);
	}

	@Override
	public void onSuccess(String message) {
		if(state != null) state.onSuccessFFmepg(message);
	}

	@Override
	public void onFinish() {
		if(state != null) state.onFinishFFmepg();
	}

	@Override
	public void onError(String exception) {
		if(state != null) state.onErrorFFmepg(exception);
	}

/*
* mp4parser
*/



	public class VideoEditorMp4ParserListener implements Mp4ParserListener {

		@Override
		public void onStart() {
			state.onStartMp4Parser();
		}

		@Override
		public void onFinish(int jobType, String outFile) {
			state.onFinishMp4Parser(jobType, outFile);
		}
	}
}
