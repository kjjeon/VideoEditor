package com.jylee.videoeditor.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jooyoung on 2018-02-25.
 */

public class FontManager {

	private static final String TAG = "FontManager";
	private static final String ASSETS_FONT_ROOT = "fonts";

	public static final String NAUM_GOTHIC = "NanumGothic.ttf";

	private String mfontDirectory;

	private static FontManager mInstance = null;
	private FontManager(Context context){
		initFontFiles(context);
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
		return mfontDirectory + "/" + fontName;
	}

	private void initFontFiles(Context context) {

		makeFontFolder(context);

		try {
			AssetManager assetMgr = context.getAssets();
			String[] rootList = assetMgr.list(ASSETS_FONT_ROOT);

			for(String element : rootList) {
				Log.d(TAG," element = " + element);
				copyAssetAll(context, element);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void copyAssetAll(Context context, String srcPath) {
		AssetManager assetMgr = context.getAssets();
		String assets[] = null;
		try {
			assets = assetMgr.list(ASSETS_FONT_ROOT + File.separator + srcPath);
			if (assets.length == 0) {
				copyFile(context,ASSETS_FONT_ROOT + File.separator + srcPath);
			} else {
				String destPath = context.getFilesDir().getAbsolutePath() + File.separator + srcPath;

				File dir = new File(destPath);
				if (!dir.exists())
					dir.mkdir();
				for (String element : assets) {
					copyAssetAll(context,srcPath + File.separator + element);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void copyFile(Context context, String srcFile) {
		AssetManager assetMgr = context.getAssets();

		InputStream is = null;
		OutputStream os = null;
		try {
			String destFile = context.getFilesDir().getAbsolutePath() + File.separator + srcFile;
			File file = new File(destFile);
			if (file.exists()) return;

 			Log.d(TAG," copy srcFile = " + srcFile);
			is = assetMgr.open(srcFile);
			os = new FileOutputStream(destFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) != -1) {
				os.write(buffer, 0, read);
			}
			is.close();
			os.flush();
			os.close();

		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void makeFontFolder(Context context) {
		mfontDirectory = context.getFilesDir().getAbsolutePath() +  "/" + "fonts";
		File file = new File(mfontDirectory);

		if(!file.exists()) {
			Log.d(TAG,"make fonts folder = " + mfontDirectory);
			file.mkdir();
		}
	}

	@Deprecated
	public void initNanumGothic(Context context) {
		assetsToInternalStorage(context, "fonts"+"/"+NAUM_GOTHIC);
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
