package com.jylee.videoeditor.mp4;

import android.os.AsyncTask;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jooyoung on 2018-02-03.
 */

public class Mp4Parser {
	private static final String TAG = "Mp4Parser";
	private ConvertAsyncTask mTask = null;
	private Mp4ParserListener mListener  = null;

	public Mp4Parser(Mp4ParserListener listener) {
		mListener = listener;
	}

	public void run(String output, ArrayList<String> videoList, ArrayList<String> audioList ) {
		if (mTask == null) {
			mTask = new ConvertAsyncTask();
			Mp4ParserProperty property;
			if (audioList == null) {
				property = new Mp4ParserProperty(Mp4ParserProperty.JOB_TYPE_APPEND, output, videoList);
			}else{
				property = new Mp4ParserProperty(Mp4ParserProperty.JOB_TYPE_ADDBGM, output, videoList, audioList);
			}
			mTask.execute(property);
		}
	}

	public boolean isRunning() {
		if (mTask == null) {
			return false;
		} else {
			return true;
		}
	}

	private boolean convertMedia(Mp4ParserProperty property) {

		if (property == null)
			return false;
		if (property.getJobType() == Mp4ParserProperty.JOB_TYPE_APPEND && property.checkNotNullForAppend())
			return false;
		if (property.getJobType() == Mp4ParserProperty.JOB_TYPE_ADDBGM && property.checkNotNullForAddBgm())
			return false;

		List<Track> audioTracks;
		List<Track> videoTracks;

		switch (property.getJobType()){
			case Mp4ParserProperty.JOB_TYPE_APPEND:
			{
				List<Movie> inMovies = fileToMoveList(property.getVideoList());
				audioTracks = getVideoTrackFromMovieList(inMovies);
				videoTracks = getAudioTrackFromMovieList(inMovies);
				break;
			}
			case Mp4ParserProperty.JOB_TYPE_ADDBGM:
			{
				List<Movie> videos = fileToMoveList(property.getVideoList());
				List<Movie> audios = fileToMoveList(property.getAudioList());
				audioTracks = getVideoTrackFromMovieList(videos);
				videoTracks = getAudioTrackFromMovieList(audios);
				break;
			}

			default:
				return false;
		}

		Movie result = makeMovieFromTrack(videoTracks, audioTracks, property.isShortest());
		Container container = new DefaultMp4Builder().build(result);
		if (saveOutputfile(property.getOutPath(), container) == false) {
			return false;
		}
		return true;
	}

	private List<Movie> fileToMoveList(ArrayList<String> sources){

		List<Movie> inMovies = new ArrayList<Movie>();
		for (String src : sources) {
			try {
				inMovies.add(MovieCreator.build(src));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return inMovies;
	}

	private List<Track> getVideoTrackFromMovieList(List<Movie> movies) {
		List<Track> videoTracks = new LinkedList<Track>();

		for (Movie m : movies) {
			for (Track t : m.getTracks()) {
				if (t.getHandler().equals("vide")) {
					videoTracks.add(t);
				}
			}
		}
		return videoTracks;
	}
	private List<Track> getAudioTrackFromMovieList(List<Movie> movies) {
		List<Track> audioTracks = new LinkedList<Track>();
		for (Movie m : movies) {
			for (Track t : m.getTracks()) {
				if (t.getHandler().equals("soun")) {
					audioTracks.add(t);
				}
			}
		}
		return audioTracks;
	}

	private Movie makeMovieFromTrack(List<Track> videoTracks, List<Track> audioTracks, boolean shortest){

		Movie result = new Movie();
		boolean editor = false;
		if (shortest && !audioTracks.isEmpty() && !videoTracks.isEmpty()) {
			if (!audioTracks.isEmpty() && !videoTracks.isEmpty()) {
				try {
					Track aTrack = new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]));
					Track vTrack = new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]));

					Log.d(TAG, "aTrack duration = " + aTrack.getDuration() +
							",aTrack getTimescale = " + aTrack.getTrackMetaData().getTimescale() +
							",a time = " + aTrack.getDuration()/aTrack.getTrackMetaData().getTimescale() +
							",vTrack duration = " + vTrack.getDuration()+
							",vTrack getTimescale = " + vTrack.getTrackMetaData().getTimescale()+
							",v time = " + vTrack.getDuration()/vTrack.getTrackMetaData().getTimescale() );

					Track sampleTrack;
					long endTime;

					if (aTrack.getDuration() > vTrack.getDuration()) {
						sampleTrack = aTrack;
						endTime = vTrack.getDuration()/vTrack.getTrackMetaData().getTimescale();
						result.addTrack(vTrack);
					} else {
						sampleTrack = vTrack;
						endTime = aTrack.getDuration()/aTrack.getTrackMetaData().getTimescale();
						result.addTrack(aTrack);
					}

					long startTime = 0;
					long currentSample = 0;
					double currentTime = 0;
					long startSample = -1;
					long endSample = -1;

					Log.d(TAG, "sampleTrack.getSampleDurations().length = " + sampleTrack.getSampleDurations().length);
					for (int i = 0; i < sampleTrack.getSampleDurations().length; i++) {
						if (currentTime <= startTime) {
							// current sample is still before the new starttime
							startSample = currentSample;
						}
						if (currentTime <= endTime) {
							// current sample is after the new start time and still before the new endtime
							endSample = currentSample;
						} else {
							// current sample is after the end of the cropped video
							break;
						}
						currentTime += (double) sampleTrack.getSampleDurations()[i] / (double) sampleTrack.getTrackMetaData().getTimescale();
						currentSample++;
					}
					Log.d(TAG, "endSample = " + endSample);
					result.addTrack(new CroppedTrack(sampleTrack, startSample, endSample));

				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		} else {
			if (!audioTracks.isEmpty()) {
				try {
					result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
			if (!videoTracks.isEmpty()) {
				try {
					result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}
			}
		}


//		TextTrackImpl subTitleEng = new TextTrackImpl();
//		subTitleEng.getTrackMetaData().setLanguage("kor");
//
//		subTitleEng.getSubs().add(new TextTrackImpl.Line(0, 1000, "Five"));
//		subTitleEng.getSubs().add(new TextTrackImpl.Line(1000, 2000, "Four"));
//		subTitleEng.getSubs().add(new TextTrackImpl.Line(2000, 3000, "Three"));
//		subTitleEng.getSubs().add(new TextTrackImpl.Line(3000, 4000, "Two"));
//		subTitleEng.getSubs().add(new TextTrackImpl.Line(4000, 5000, "one"));
//		result.addTrack(subTitleEng);
		return result;
	}

	private boolean saveOutputfile(String outPath, Container outContainer)
	{
		FileChannel fc = null;

		try {
			fc = new RandomAccessFile(String.format(outPath), "rw").getChannel();
			outContainer.writeContainer(fc);
			fc.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public class ConvertAsyncTask extends AsyncTask<Mp4ParserProperty, Integer, Void> {

		private int mTaskType = Mp4ParserProperty.JOB_TYPE_READY;
		private Mp4ParserProperty mp4ParserProperty;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mListener.onStart();

		}

		@Override
		protected Void doInBackground(Mp4ParserProperty... property) {
			mp4ParserProperty = property[0];
			convertMedia(mp4ParserProperty);
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			mTask = null;
			mListener.onFinish(mp4ParserProperty.getJobType(),mp4ParserProperty.getOutPath());
		}
	}
}