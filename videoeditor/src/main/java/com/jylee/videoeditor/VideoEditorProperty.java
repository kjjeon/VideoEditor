package com.jylee.videoeditor;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-03-03.
 */

public class VideoEditorProperty {

	public enum StateType {
		WATTING, GET_INFO, ADD_TEXT, MERGE_VIDEO,
	}

	private StateType status = StateType.WATTING;
	private String text;
	private String output;
	private String intro;
	private String audio;
	private ArrayList<String> videoList;
	private int duration;

	//		private String makeFolder =  mContext.getFilesDir().getAbsolutePath() + "/" + "ffmpeg" + "/";
	private String makeFolder =  "/storage/emulated/0/Download/mp4parser/" ;
	private String tempMp4File =  makeFolder +  "temp.mp4";

	public VideoEditorProperty(String jobDirectory) {
		makeFolder = jobDirectory;
	}

	public String getMakeFolder() {
		return makeFolder;
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