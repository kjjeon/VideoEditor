package com.jylee.videoeditor.custom;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

import lombok.Data;

/**
 * Created by jooyoung on 2018-03-03.
 */

public @Data
class ConcatTextVideoProperty {

	public enum StateType {
		WATTING, GET_INFO, ADD_TEXT, MERGE_VIDEO, MERGE_AUDIO,
	}


	private StateType status = StateType.WATTING;
	private String text1="";
	private String text2="";
	private String text3="";
	private String output ="";
	private String intro="";
	private String ending="";
	private String emblem="";
	private String audio="";
	private ArrayList<String> videoList = new ArrayList<String>();

	private int duration;
	private String videoTbr=""; //video tbr info  for ffmpeg


	//		private String makeFolder =  mContext.getFilesDir().getAbsolutePath() + "/" + "ffmpeg" + "/";
	private String makeFolder;
	private String mp4Step1File;
	private String mp4Step2File;

	public void setTempFolder(String makeFolder) {
		this.makeFolder = makeFolder;
		this.mp4Step1File = this.makeFolder + "/" +  "temp_step1.mp4";
		this.mp4Step2File = this.makeFolder + "/" +  "temp_step2.mp4";
	}

	public int searchVideo(String videoFolder) {
		File dir = new File(makeFolder);

		File[] matchingFiles =dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
//				return name.startsWith("contents") && name.endsWith("mp4");
				return  name.endsWith("mp4") && !name.contains("temp_step1.mp4") && !name.contains("temp_step2.mp4");
			}
		});

		videoList.clear();
		if(matchingFiles != null) {
			for (File file : matchingFiles) {
				videoList.add(file.getAbsolutePath());
			}
			Collections.sort(videoList);
		}
		return videoList.size();
	}
}