package com.jylee.videoeditor;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutorListener;
import com.jylee.videoeditor.mp4.Mp4Parser;
import com.jylee.videoeditor.mp4.Mp4ParserListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jooyoung on 2018-02-03.
 * facade pattern
 */

public class VideoEditorService implements FFmpegExcutorListener {
	private static final String TAG = "VideoEditorService";

	private Context mContext = null;
	private Mp4Parser mMp4Parser = null;
	private VideoEditorServiceListener mListener = null;
	private VideoEditorProperty mProperty = null;


	public  VideoEditorService(Context context, VideoEditorServiceListener listener) {
		mContext = context;
		mListener = listener;
		VideoEditorMp4ParserListener videoEditorMp4ParserListener= new VideoEditorMp4ParserListener();
		mMp4Parser = new Mp4Parser(videoEditorMp4ParserListener);
		FFmpegExcutor.getInstance(context);
		mProperty = new VideoEditorProperty("/storage/emulated/0/Download/mp4parser/");
	}

	public void makeVideo(String outputFile,String introFile,  ArrayList<String> videoList, String audio, String text)
	{
		if(mProperty.getStatus() == VideoEditorProperty.StateType.WATTING) {
			mProperty.setStatus(VideoEditorProperty.StateType.GET_INFO);
			mProperty.setIntro(introFile);
			mProperty.setVideoList(videoList);
			mProperty.setOutput(outputFile);
			mProperty.setAudio(audio);
			mProperty.setText(text);
			getInfo(outputFile);
		}
	}

	private void getInfo(String fileName)
	{
		FFmpegExcutor.getInstance().run(FFmpegExcutor.getInstance().getCmdPackage().getInfo(fileName),this);
	}


	private void drawText(String outputFile, String inputFile, String text) {
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getToAddTextCmd(outputFile, inputFile, text, 72, 52, 50,"white", 0, 5, 1280, 720);
//		FFmpegExcutor.getInstance(mContext).run(FFmpegCmdPackage.getInstance().getToAddTextCmd(outputFile, inputFile, text ),this);
		FFmpegExcutor.getInstance().run(cmd,this);
	}

	private void concetVideo(String outputFile, ArrayList<String> videoList, String mp3) {
		StringBuffer fileList = new StringBuffer ();

		for (String video : videoList){
			File file = new File(video);
			if(file.exists()) {
				video = video.replace(mProperty.getMakeFolder(),"");
				fileList.append("file \'");
				fileList.append(video);
				fileList.append("\'\n");
			}
		}

		Log.d("TAG",fileList.toString());
		writeFile(mProperty.getMakeFolder() +  "fflist.txt",fileList.toString());
		final String[] cmd = FFmpegExcutor.getInstance().getCmdPackage().getConcatVideoCmd(outputFile,mProperty.getMakeFolder()+ "/" + "fflist.txt");
		FFmpegExcutor.getInstance().run(cmd,this);

	}

	private void writeFile(String filePath ,String text) {
		try {
			File file = new File(filePath);
			FileOutputStream outStream = new FileOutputStream(file);
			outStream.write(text.getBytes());
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	private  void copy(File src, File dst) throws IOException {
		try (InputStream in = new FileInputStream(src)) {
			try (OutputStream out = new FileOutputStream(dst)) {
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
		}
	}

	//ffmpeg
	@Override
	public void onStart() {
		Log.d(TAG,"start ffmpeg converting");
		mListener.onProgressToConvert(0);
	}

	@Override
	public void onProgress(String message) {
		Log.d("TAG","onProgress = " + message);
	}

	@Override
	public void onFailure(String message) {
		if(mProperty.getStatus()  == VideoEditorProperty.StateType.GET_INFO){
			String[] lines = message.split(System.getProperty("line.separator"));
			for(String  str : lines) {
				Pattern word = Pattern.compile("Duration:");
				Matcher match = word.matcher(str);
				if(match.find()) {
					Log.d("TAG"," str = " + str);
					mProperty.setDuration(10);
				}else {
					mProperty.setDuration(-1);
				}
			}
		}
		Log.d("TAG","onFailure = " + message);
	}

	@Override
	public void onSuccess(String message) {
		Log.d(TAG,"onSuccess = " + message);
	}

	@Override
	public void onFinish() {
		Log.d(TAG,"onFinish");
		if(mProperty.getStatus()  == VideoEditorProperty.StateType.GET_INFO){
			mProperty.setStatus(VideoEditorProperty.StateType.ADD_TEXT);
			drawText(mProperty.getMp4TempFile(), mProperty.getIntro(), mProperty.getText());
		} else if(mProperty.getStatus()  == VideoEditorProperty.StateType.ADD_TEXT){
			mProperty.setStatus(VideoEditorProperty.StateType.MERGE_VIDEO);
			ArrayList <String> list = mProperty.getVideoList();
			list.add(0,mProperty.getMp4TempFile());
			Log.d(TAG,"list = " + list.toString());
			concetVideo(mProperty.getOutput(),list,mProperty.getAudio());
//			convert(mProperty.getOutput(),list,mProperty.getAudio());
		}else {
			mProperty.setStatus(VideoEditorProperty.StateType.WATTING);
		}
	}

	@Override
	public void onError(String exception) {
		Log.d(TAG,"onError = " + exception);
	}



/*
* mp4parser api
* have not used this library.
 */

	private void convert(String outputFile, ArrayList<String> videoList) {
		mMp4Parser.run(outputFile, videoList,null);
	}

	private void convert(String outputFile, ArrayList<String> videoList, ArrayList<String> audioList) {
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	private void convert(String outputFile,  ArrayList<String> videoList, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	private void convert(String outputFile, String video, String audio) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mMp4Parser.run(outputFile, videoList, audioList);
	}

	private void convert(String outputFile, String video, String audio, String text) {
		ArrayList<String> audioList = new ArrayList<String>();
		audioList.add(audio);
		ArrayList<String> videoList = new ArrayList<String>();
		videoList.add(video);
		mMp4Parser.run(outputFile, videoList, audioList);
	}


	public class VideoEditorMp4ParserListener implements Mp4ParserListener {

		@Override
		public void onStart() {

		}

		@Override
		public void onFininsh(int jobType, String outFile) {

		}
	}

}
