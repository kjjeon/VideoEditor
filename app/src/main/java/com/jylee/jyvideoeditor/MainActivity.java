package com.jylee.jyvideoeditor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.jylee.jyvideoeditor.databinding.ActivityMainBinding;
import com.jylee.videoeditor.VideoEditorService;
import com.jylee.videoeditor.VideoEditorServiceListener;

public class MainActivity extends AppCompatActivity implements VideoEditorServiceListener {

	private VideoEditorService mVideoEditor;
	private ActivityMainBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

		int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
		if (permissionCheck == PackageManager.PERMISSION_GRANTED && permissionCheck1 == PackageManager.PERMISSION_GRANTED ) {
			Toast.makeText(this, "READ/WRITE 권한 주어져 있음", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "READ/WRITE 권한 없음", Toast.LENGTH_LONG).show();
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				//이미 사용자가 한번 거부 했으므로 권환 요청의 필요성을 설명할 필요가 있음
				Toast.makeText(this, "READ/WRITE 진짜 필요하니깐 주세요. ", Toast.LENGTH_LONG).show();
			}else{
				ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
				ActivityCompat.requestPermissions(this,new String [] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
			}
		}

		mVideoEditor = new VideoEditorService(getApplicationContext(),this);
		binding.button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				ArrayList<String> videoList = new ArrayList<String>();
//
//				for(int i=0; i<10; i++){
//					String path = "/mp4parser/make/" +
//							String.valueOf(i) +
//							".mp4";
//
//					videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + path);
//				}
//				videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/0.mp4");
//				videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/1.mp4");
//				videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/2.mp4");
//				videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/3.mp4");
//				String out = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/out.mp4";
//				String mp3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/free.m4a";
//				String intro = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/12.mp4";
//				mVideoEditor.convert(out, videoList);
//				mVideoEditor.convert(all, videoList,mp3);
//				mVideoEditor.convert(out, all, mp3);
//				mVideoEditor.drawText(out,all,"깜쭈야 일어나라");

//				mVideoEditor.makeDayVideo(out,all,"꼬꼬꼬");
//				mVideoEditor.makeFullVideo(out,videoList,mp3,"하이 깜쭈");

//				videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +  "/mp4parser/a1.mp4");
//				videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() +  "/mp4parser/a2.mp4");
//				for(int i=0; i<10; i++){
//					String path = "/mp4parser/" +
//							String.valueOf(i) +
//							".mp4";
//
//					videoList.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + path);
//				}
				binding.progress.setMax(100);
//				mVideoEditor.makeVideo(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make",
//						"day_1.mp4",
//						"intro.mp4",
//						"free.m4a",
//						"이건 파이널 영상 테스트 입니다. "
//				);

//				mVideoEditor.makeVideo(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make",
//						"day_1.mp4",
//						"emblem.mp4",
//						"이주영"
//				);
//				mVideoEditor.make(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make",
//						"day_1.mp4",
//						"emblem.mp4",
//						"이주영",
//						"나의 여행"
//				);

//				mVideoEditor.makeVideo(out,intro,videoList,mp3,"스벅 조아!");

				mVideoEditor.makeDayVideo(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make",
						"day_1.mp4",
						"이것은 데이 영상 테스트"
				);

			}
		});
		binding.button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mVideoEditor.makeFinalVideo(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make",
						"final1.mp4",
						"emblem.mp4",
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make/free.m4a",
						"이주영",
						"파이널 여행"

				);
			}
		});
		binding.button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mVideoEditor.makeFinalVideo(
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make",
						"final2.mp4",
						"emblem.mp4",
						"intro.mp4",
						Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/make/free.m4a",
						"이주영",
						"파이널 여행"

				);
			}
		});
	}

	@Override
	public void onStartToConvert() {
		Toast.makeText(this, "start converting ", Toast.LENGTH_LONG).show();
		binding.textView.setText("변환중..");
		binding.progress.setProgress(0);
	}

	@Override
	public void onFininshToConvert() {
		binding.progress.setProgress(100);
		binding.textView.setText("대기중..");
		Toast.makeText(this, "finish converting ", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onProgressToConvert(int per){

		Log.d("TAG", "progress = " + per);
		binding.progress.setProgress(per);
	}

	@Override
	public void onErrorToConvert(String message) {

	}

}
