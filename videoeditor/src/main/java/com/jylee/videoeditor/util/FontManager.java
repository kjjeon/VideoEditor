package com.jylee.videoeditor.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jooyoung on 2018-02-25.
 */

public class FontManager extends AssetsManager{

	private static final String TAG = "FontManager";

	public static final String DEFAULT_FONT = "NanumMyeongjoExtraBold.ttf";

	private static FontManager mInstance = null;

	private FontManager(Context context){
		super(context,"fonts",true);
	}
	public static FontManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new FontManager(context);
		}
		return mInstance;
	}

	@NonNull
	public static FontManager getInstance() {
		return mInstance;
	}

	public String getFontFilePath(String fontName) {
		return getDirectory() + "/" + fontName;
	}


	@Deprecated
	public void initFont(Context context) {
		assetsToInternalStorage(context, "fonts"+"/"+ DEFAULT_FONT);
	}

	@Deprecated
	private void assetsToInternalStorage(Context context, String src) {

		AssetManager assetManager = context.getAssets();
		String internalDir = context.getFilesDir().getAbsolutePath();
		File file = new File(internalDir + "/" + src);
		if(!file.exists()) {
			Log.d(TAG,"file not exists create file = "+ file.getAbsolutePath());
			try {
				InputStream in = assetManager.open(src);
				OutputStream out = new FileOutputStream(internalDir + "/" + src);
				byte[] buffer = new byte[1024];
				int read = in.read(buffer);
				while (read != -1) {
					out.write(buffer, 0, read);
					read = in.read(buffer);
				}
			} catch (Exception e) {
				e.getMessage();
			}
		}
	}
}
