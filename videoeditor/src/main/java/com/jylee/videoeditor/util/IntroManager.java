package com.jylee.videoeditor.util;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Created by jooyoung on 2018-03-10.
 */

public class IntroManager extends AssetsManager{

	private static final String TAG = "IntroManager";

	private static IntroManager mInstance = null;
	private IntroManager(Context context){
		super(context,"intro");
	}

	public static IntroManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new IntroManager(context);
		}
		return mInstance;
	}

	@NonNull
	public static IntroManager getInstance() {
		return mInstance;
	}


}
