package com.jylee.videoeditor;

/**
 * Created by jooyoung on 2018-02-03.
 */

public interface VideoEditorServiceListener {
	void onStartToConvert();
	void onFininshToConvert();
	void onProgressToConvert(int per);
	void onErrorToConvert();

}
