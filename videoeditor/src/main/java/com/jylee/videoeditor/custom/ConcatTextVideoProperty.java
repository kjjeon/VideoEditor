package com.jylee.videoeditor.custom;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-03-03.
 */

public class ConcatTextVideoProperty {

	public enum StateType {
		WATTING, GET_INFO, ADD_TEXT, MERGE_VIDEO, MERGE_AUDIO,
	}

	private StateType status = StateType.WATTING;
	private String text;
	private String output;
	private String intro;
	private String audio;
	private ArrayList<String> videoList;
	private int duration;



	//		private String makeFolder =  mContext.getFilesDir().getAbsolutePath() + "/" + "ffmpeg" + "/";
	private String makeFolder;
	private String mp4Step1File;
	private String mp4Step2File;

	public void setMakeFolder(String makeFolder) {
		this.makeFolder = makeFolder;
		this.mp4Step1File = this.makeFolder + "/" +  "temp1.mp4";
		this.mp4Step2File = this.makeFolder + "/" +  "temp2.mp4";
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

	public String getMp4Step1File() {
		return mp4Step1File;
	}

	public String getMp4Step2File() {
		return mp4Step2File;
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