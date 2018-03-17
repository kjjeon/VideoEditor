package com.jylee.videoeditor.custom;

import com.jylee.videoeditor.VideoEditorServiceListener;

/**
 * Created by jooyoung on 2018-03-17.
 */

public interface State {
	boolean start(ConcatTextVideoContext context, ConcatTextVideoProperty property, VideoEditorServiceListener listener);
	boolean cancel();
	//ffmepg
	void onStartFFmepg();
	void onProgressFFmepg(String message);
	void onFailureFFmepg(String message);
	void onSuccessFFmepg(String message);
	void onFinishFFmepg();
	void onErrorFFmepg(String exception);
	//mp4parser
	void onStartMp4Parser();
	void onFinishMp4Parser(int jobType, String outFile);
}
