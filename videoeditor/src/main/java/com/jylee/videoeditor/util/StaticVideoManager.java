package com.jylee.videoeditor.util;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by jooyoung on 2018-03-10.
 */

public class StaticVideoManager extends AssetsManager{

	private static final String TAG = "StaticVideoManager";
	private Context context;

	private static StaticVideoManager mInstance = null;
	private StaticVideoManager(Context context)
	{
		super(context,"static", false);
		this.context = context;
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

	public void copyFileFromAsset(String srcName, String destAbsFilePath) {
		copyFileFromAsset(this.context, getBaseName() + File.separator + srcName, destAbsFilePath);
	}

}
