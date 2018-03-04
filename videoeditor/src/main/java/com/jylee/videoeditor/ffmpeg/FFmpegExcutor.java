package com.jylee.videoeditor.ffmpeg;

import android.content.Context;
import android.support.annotation.NonNull;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.jylee.videoeditor.util.FontManager;

/**
 * Created by jooyoung on 2018-02-24.
 */

public class FFmpegExcutor {
	private static FFmpegExcutor mInstance = null;
	private FFmpeg mFFmpeg = null;
	private FFmpegCmdPackage mFFmpegCmdPackage = null;

	private FFmpegExcutor(Context context) {
		mFFmpeg = FFmpeg.getInstance(context);
		mFFmpegCmdPackage = new FFmpegCmdPackage();

		try {
			mFFmpeg.loadBinary(new LoadBinaryResponseHandler() {

				@Override
				public void onStart() {
				}

				@Override
				public void onFailure() {
				}

				@Override
				public void onSuccess() {
				}

				@Override
				public void onFinish() {
				}
			});
		} catch (FFmpegNotSupportedException e) {
			// Handle if FFmpeg is not supported by device
		}

		//init font
		FontManager.getInstance(context);
	}

	public static FFmpegExcutor getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new FFmpegExcutor(context);
		}
		return mInstance;
	}

	@NonNull
	public static FFmpegExcutor getInstance() {
		return mInstance;
	}

	public void run(String cmd, final FFmpegExcutorListener listener) {
		String[] command = cmd.split(" ");
		run(command, listener);
	}

	public void run(final String[] cmd, final FFmpegExcutorListener listener) {
		try {
			// to execute "ffmpeg -version" command you just need to pass "-version"
			mFFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

				@Override
				public void onStart() {
					listener.onStart();
				}

				@Override
				public void onProgress(String message) {
					listener.onProgress(message);
				}

				@Override
				public void onFailure(String message) {
					listener.onFailure(message);
				}

				@Override
				public void onSuccess(String message) {
					listener.onSuccess(message);
				}

				@Override
				public void onFinish() {
					listener.onFinish();
				}
			});
		} catch (FFmpegCommandAlreadyRunningException e) {
			listener.onError(e.getMessage());
			// Handle if FFmpeg is already running
		}
	}

	public boolean isRunning() {
		return mFFmpeg.isFFmpegCommandRunning();
	}

	public boolean kill() {
		return mFFmpeg.killRunningProcesses();
	}

	public void setTimeOut(long timeout) {
		mFFmpeg.setTimeout(timeout);
	}

	public FFmpegCmdPackage getCmdPackage() {
		return mFFmpegCmdPackage;
	}
}

