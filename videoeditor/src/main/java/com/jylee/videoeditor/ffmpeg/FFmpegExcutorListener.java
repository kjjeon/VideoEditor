package com.jylee.videoeditor.ffmpeg;

/**
 * Created by jooyoung on 2018-02-24.
 */

public interface FFmpegExcutorListener {
	void onStart() ;
	void onProgress(String message);
	void onFailure(String message);
	void onSuccess(String message);
	void onFinish();
	void onError(String exception);
}
