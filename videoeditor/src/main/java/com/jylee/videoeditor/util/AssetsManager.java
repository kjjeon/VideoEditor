package com.jylee.videoeditor.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lombok.Getter;

/**
 * Created by jooyoung on 2018-03-10.
 */

public class AssetsManager {

	private static final String TAG = "AssetManager";

	private @Getter
	String directory;
	private String baseName;

	public AssetsManager(Context context, String folder){
		this.directory = context.getFilesDir().getAbsolutePath() +  "/" + folder;
		this.baseName = folder;
		copyFiles(context);
	}

	private void copyFiles(Context context) {
		makeFolder(directory);
		try {
			android.content.res.AssetManager assetMgr = context.getAssets();
			String[] rootList = assetMgr.list(baseName);

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
		android.content.res.AssetManager assetMgr = context.getAssets();
		String assets[] = null;
		try {
			assets = assetMgr.list(baseName + File.separator + srcPath);
			if (assets.length == 0) {
				copyFile(context,baseName + File.separator + srcPath);
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
		android.content.res.AssetManager assetMgr = context.getAssets();

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

	private void makeFolder(String folder) {

		File file = new File(folder);

		if(!file.exists()) {
			Log.d(TAG,"make  folder = " + folder);
			file.mkdir();
		}
	}
}
