package com.jylee.videoeditor.mp4;

/**
 * Created by jooyoung on 2018-02-03.
 */

public interface Mp4ParserListener {
		void onStart();
		void onFininsh(int jobType,String outFile);
}
