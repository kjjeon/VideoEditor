package com.jylee.videoeditor;

import android.content.Context;

import com.jylee.videoeditor.custom.ConcatTextVideoContext;
import com.jylee.videoeditor.ffmpeg.FFmpegExcutor;
import com.jylee.videoeditor.util.IntroManager;

/**
 * Created by jooyoung on 2018-03-04.
 */

public class VideoEditorService {

	private Context mContext = null;
	private ConcatTextVideoContext mConcatTextVideoContext = null;
	private VideoEditorServiceListener mListener = null;


	public VideoEditorService(Context context, VideoEditorServiceListener listener) {
		mContext = context;
		mListener = listener;
		//Init
		FFmpegExcutor.getInstance(context);
		IntroManager.getInstance(context);
		mConcatTextVideoContext = new ConcatTextVideoContext();
	}

	public boolean makeDayVideo(String rootDirectory, String outputFileName, String text)
	{
		return mConcatTextVideoContext.makeDayVideo(rootDirectory, outputFileName, text, mListener);
	}

	public boolean makeFinalVideo(String rootDirectory, String outputFileName, String emblemFileName,
								  String introFileName, String audioAbsFilePath, String id, String title)
	{
//		String introAbsFileName = mContext.getFilesDir().getAbsolutePath() + "/intro/" +  introName;
//		String introAbsFileName =IntroManager.getInstance().getDirectory() + "/" + introFileName;
		return mConcatTextVideoContext.makeFinalVideo(rootDirectory, outputFileName, emblemFileName, introFileName, audioAbsFilePath, id, title, mListener);
	}

	public boolean makeFinalVideo(String rootDirectory, String outputFileName, String emblemFileName,
								  String audioAbsFilePath,  String id, String title)
	{
		return mConcatTextVideoContext.makeFinalVideo(rootDirectory, outputFileName, emblemFileName, "",audioAbsFilePath, id, title, mListener);
	}


	/**
	 *  현재 여부를 리턴 한다.
	 * @return
	 */
	public boolean isRunning()
	{
		return mConcatTextVideoContext.isRunning();
	}

	/**
	 *  변환을 취소한다.
	 * @return
	 */
	public boolean cancel()
	{
		return mConcatTextVideoContext.cancel();
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
//	public boolean makeVideo(String outputAbsFilePath, String introAbsFilePath,  ArrayList<String> videoAbsFileList, String audioAbsFilePath, String text)
//	{
//		return mConcatTextVideo.makeVideo(outputAbsFilePath, introAbsFilePath, videoAbsFileList, audioAbsFilePath, text);
//	}
}
