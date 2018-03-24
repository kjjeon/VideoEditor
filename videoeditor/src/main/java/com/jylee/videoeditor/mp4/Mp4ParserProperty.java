package com.jylee.videoeditor.mp4;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-02-10.
 */

public class Mp4ParserProperty {
	public final static int JOB_TYPE_READY = 0;
	public final static int JOB_TYPE_APPEND = 1;
	public final static int JOB_TYPE_ADDBGM = 2;

	private ArrayList<String> videoList = null;
	private ArrayList<String> audioList = null;
	private boolean shortest = false;
	private String outPath = null;
	private int jobType = JOB_TYPE_READY;

	public Mp4ParserProperty(int type, String outPath, ArrayList<String> videoList)
	{
		this.jobType = type;
		this.videoList = videoList;
		this.outPath = outPath;
	}

	public Mp4ParserProperty(int type, String outPath, ArrayList<String> videoList,ArrayList<String> audioList)
	{
		this.jobType = type;
		this.videoList = videoList;
		this.audioList = audioList;
		this.outPath = outPath;
	}

	public boolean isShortest() {
		return shortest;
	}

	public void setShortest(boolean shortest) {
		this.shortest = shortest;
	}

	public ArrayList<String> getVideoList() {
		return videoList;
	}

	public ArrayList<String> getAudioList() {
		return audioList;
	}

	public String getOutPath() {
		return outPath;
	}

	public int getJobType() {
		return jobType;
	}

	public boolean checkNotNullForAppend(){
		if (videoList.isEmpty())
			return true;
		if (outPath == null)
			return true;
		return false;
	}

	public boolean checkNotNullForAddBgm(){
		if (videoList.isEmpty()) {
			return true;
		}
		if (audioList == null) {
			return true;
		}
		if (outPath == null){
			return true;
		}
		return false;
	}
}
