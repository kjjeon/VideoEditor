package com.jylee.videoeditor.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jooyoung on 2018-02-25.
 */

public class FontManager {

	private static final String TAG = "FontManager";
	private static final String NAUM_GOTHIC = "NanumGothic.ttf";
	private String mfontDirectory;

	private static FontManager mInstance = null;
	private FontManager(Context context){
		makeFontFolder(context);
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

	public void initNanumGothic(Context context) {
		assetsToInternalStorage(context, "fonts"+"/"+NAUM_GOTHIC);
	}

	public String getNanumGothicPath() {
		return mfontDirectory + "/" + NAUM_GOTHIC;
	}

	private void makeFontFolder(Context context) {
		mfontDirectory = context.getFilesDir().getAbsolutePath() +  "/" + "fonts";
		File file = new File(mfontDirectory);

		if(!file.exists()) {
			Log.d(TAG,"make fonts folder = " + mfontDirectory);
			file.mkdir();
		}
	}

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

	// If targetLocation does not exist, it will be created.
	private void copyDirectory(File sourceLocation , File targetLocation)
			throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists() && !targetLocation.mkdirs()) {
				throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			// make sure the directory we plan to store the recording in exists
			File directory = targetLocation.getParentFile();
			if (directory != null && !directory.exists() && !directory.mkdirs()) {
				throw new IOException("Cannot create dir " + directory.getAbsolutePath());
			}

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}
}
