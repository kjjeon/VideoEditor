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
		mConcatTextVideo = new ConcatTextVideo(mListener);
	}


	public boolean makeVideo(String rootDirectory, String outputName , String introName, String text)
	{
		//TODO: intro file path
//		String introAbsFileName = mContext.getFilesDir().getAbsolutePath() + "/intro/" +  introName;
		String introAbsFileName =introName;

		return mConcatTextVideo.makeVideo(rootDirectory, outputName, introAbsFileName, "", text);
	}

	public boolean makeVideo(String rootDirectory, String outputName , String introName, String audioName , String text)
	{
		//TODO: intro file path
//		String introAbsFileName = mContext.getFilesDir().getAbsolutePath() + "/intro/" +  introName;
		String introAbsFileName =introName;
		return mConcatTextVideo.makeVideo(rootDirectory, outputName, introAbsFileName, audioName, text);
	}

	/**
	 *  현재 여부를 리턴 한다.
	 * @return
	 */
	public boolean isRunning()
	{
		return mConcatTextVideo.isRunning();
	}

	/**
	 *  변환을 취소한다.
	 * @return
	 */
	public boolean cancel()
	{
		return mConcatTextVideo.cancel();
	}

	/**
	 *
	 * @param outputAbsFilePath output file 절대 경로
	 * @param introAbsFilePath intro file 절대 경로
	 * @param videoAbsFileList 합쳐질 비디오 절대 경로 (단 ffmpeg lib 사용 시 output file  경로 보다 상위 폴더에 있으면 안된다. 동일 폴더 또는 하위 폴더로 위치)
	 * @param audioAbsFilePath 배경음악 음원 절대 경로
	 * @param text intro Video 에 추가 될 text
	 * @return false : 이미 사용중 , true: 변환 진행
	 */
	public boolean makeVideo(String outputAbsFilePath, String introAbsFilePath,  ArrayList<String> videoAbsFileList, String audioAbsFilePath, String text)
	{
		return mConcatTextVideo.makeVideo(outputAbsFilePath, introAbsFilePath, videoAbsFileList, audioAbsFilePath, text);
	}
}
