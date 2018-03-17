package com.jylee.videoeditor.util;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by jooyoung on 2018-03-10.
 */

public class StaticVideoManager extends AssetsManager{

	private static final String TAG = "StaticVideoManager";

	private static StaticVideoManager mInstance = null;
	private StaticVideoManager(Context context){
		super(context,"static");
	}

	public static StaticVideoManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new StaticVideoManager(context);
		}
		return mInstance;
	}

	@NonNull
	public static StaticVideoManager getInstance() {
		return mInstance;
	}


}
