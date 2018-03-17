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

	/**
	 *
	 * @param rootDirectory 작업 디렉토리
	 * @param outputFileName 아웃 파일 이름 (작업 디렉토리의 부모 디렉토리에 생성됨)
	 * @param endingFileName 엔딩 비디오 파일 이름 (asset에 있는 파일 이름 이며, 내부 어플 디렉토리의 intro 폴더에서 가져옴)
	 * @param text 삽입 할 텍스트를 입력
	 * @return
	 */
	public boolean makeDayVideo(String rootDirectory, String outputFileName, String endingFileName, String text)
	{
		return mConcatTextVideoContext.makeDayVideo(rootDirectory, outputFileName, endingFileName, text, mListener);
	}


	public boolean makeFinalVideo(String rootDirectory, String outputFileName, String emblemFileName, String endingFileName,
								  String audioAbsFilePath,  String id, String title)
	{
		return mConcatTextVideoContext.makeFinalVideo(rootDirectory, outputFileName, emblemFileName,"", endingFileName,audioAbsFilePath, id, title, mListener);
	}

	public boolean makeFinalVideo(String rootDirectory, String outputFileName, String emblemFileName,
								  String introFileName, String endingFileName,
								  String audioAbsFilePath, String id, String title)
	{
//		String introAbsFileName = mContext.getFilesDir().getAbsolutePath() + "/intro/" +  introName;
//		String introAbsFileName = IntroManager.getInstance().getDirectory() + "/" + introFileName;
		return mConcatTextVideoContext.makeFinalVideo(rootDirectory, outputFileName, emblemFileName, introFileName, endingFileName, audioAbsFilePath, id, title, mListener);
	}

	/**
	 *  현재 동작 여부를 리턴 한다.
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


}
