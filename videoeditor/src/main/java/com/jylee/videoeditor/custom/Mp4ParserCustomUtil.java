package com.jylee.videoeditor.custom;

import com.jylee.videoeditor.mp4.Mp4Parser;

import java.util.ArrayList;

/**
 * Created by jooyoung on 2018-03-17.
 */

public class Mp4ParserCustomUtil {

	private Mp4Parser mp4Parser;

	public Mp4ParserCustomUtil(Mp4Parser mp4Parser) {
		this.mp4Parser = mp4Parser;
	}

	public void convert(String outputFile, ArrayList<String> videoList) {
		mp4Parser.run(outputFile, videoList,null);
	}

	public void convert(String outputFile, ArrayList<String> videoList, ArrayList<String> audioList) {
		mp4Parser.run(outputFile, videoList, audioList);
	}

	public void convert(String outputFile,  ArrayList<String> videoList, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		mp4Parser.run(outputFile, videoList, audioList);
	}

	public void convert(String outputFile, String video, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mp4Parser.run(outputFile, videoList, audioList);
	}

	public void convert(String outputFile, String video, String audio, String text) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mp4Parser.run(outputFile, videoList, audioList);
	}
}
