package com.jylee.videoeditor.mp4;

import android.os.AsyncTask;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

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

	private ConvertAsyncTask mTask = null;
	private Mp4ParserListener mListener  = null;

	public Mp4Parser(Mp4ParserListener listener) {
		mListener = listener;
	}

	public void startToAppend(String output, ArrayList<String> videoList ) {
		if (mTask == null) {
			mTask = new ConvertAsyncTask();
			Mp4ParserProperty property = new Mp4ParserProperty(Mp4ParserProperty.JOB_APPEND,output,videoList);
			mTask.execute(property);
		}
	}

	public void startToAddBgm(String output, String video, String bgm) {
		if (mTask == null) {
			mTask = new ConvertAsyncTask();
			ArrayList<String> videoList = new ArrayList<String>();
			videoList.add(video);
			Mp4ParserProperty property = new Mp4ParserProperty(Mp4ParserProperty.JOB_ADDBGM, output, videoList, bgm);
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

	private boolean appendMovie(Mp4ParserProperty property) {
		if (property == null)
			return false;
		if (property.checkNotNullForAppend())
			return false;


		List<Movie> inMovies = new ArrayList<Movie>();
		for (String videoUri : property.videoList) {
			try {
				inMovies.add(MovieCreator.build(videoUri));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		List<Track> videoTracks = new LinkedList<Track>();
		List<Track> audioTracks = new LinkedList<Track>();

		for (Movie m : inMovies) {
			for (Track t : m.getTracks()) {
				if (t.getHandler().equals("soun")) {
					audioTracks.add(t);
				}
				if (t.getHandler().equals("vide")) {
					videoTracks.add(t);
				}
			}
		}

		Movie result = new Movie();

		if (!audioTracks.isEmpty()) {
			try {
				result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		if (!videoTracks.isEmpty()) {
			try {
				result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		Container container = new DefaultMp4Builder().build(result);
		if (saveOutputfile(property.outPath,container) == false){
			return false;
		}

		return true;
	}

	private boolean addBgm(Mp4ParserProperty property) {
		if (property == null)
			return false;
		if (property.checkNotNullForAddBgm()) {
			return false;
		}

		List<Movie> inMovies = new ArrayList<Movie>();
		for (String videoUri : property.videoList) {
			try {
				inMovies.add(MovieCreator.build(videoUri));
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		List<Track> videoTracks = new LinkedList<Track>();
		List<Track> audioTracks = new LinkedList<Track>();

		for (Movie m : inMovies) {
			for (Track t : m.getTracks()) {
				if (t.getHandler().equals("vide")) {
					videoTracks.add(t);
				}
			}
		}
		try {
			Movie m4aAudio =  MovieCreator.build(property.bgm);
			for (Track t : m4aAudio.getTracks()) {
				if (t.getHandler().equals("soun")) {
					audioTracks.add(t);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		Movie result = new Movie();
		try {
			if (audioTracks.size() > 0){
				result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
			}
			if (videoTracks.size() > 0){
				result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}

		Container container = new DefaultMp4Builder().build(result);
		if (saveOutputfile(property.outPath,container) == false){
			return false;
		}
		return true;
	}

	private boolean saveOutputfile(String outPath, Container outContainer)
	{
		FileChannel fc = null;

		try {
//			String outPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/mp4parser/out.mp4";

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

		//		FileOutputStream fos;
//		try {
//			fos = new FileOutputStream(outputFile);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//			return false;
//		}
//		BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
//		try {
//			out.writeContainer(byteBufferByteChannel);
//			byteBufferByteChannel.close();
//			fos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	public class ConvertAsyncTask extends AsyncTask<Mp4ParserProperty, Integer, Void> {

		private int mTaskType = Mp4ParserProperty.JOB_READY;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(mTaskType == Mp4ParserProperty.JOB_APPEND)
				mListener.onStartToAppend();
			else if(mTaskType == Mp4ParserProperty.JOB_ADDBGM)
				mListener.onStartToAddBgm();
		}

		@Override
		protected Void doInBackground(Mp4ParserProperty... property) {
			mTaskType = property[0].type;
			switch(property[0].type){
				case Mp4ParserProperty.JOB_APPEND:
					appendMovie(property[0]);
					break;
				case Mp4ParserProperty.JOB_ADDBGM:
					addBgm(property[0]);
					break;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			mTask = null;
			if(mTaskType == Mp4ParserProperty.JOB_APPEND)
				mListener.onFininshToAppend();
			else if(mTaskType == Mp4ParserProperty.JOB_ADDBGM)
				mListener.onFininshToAddBgm();
		}
	}

	public class Mp4ParserProperty {
		private final static int JOB_READY = 0;
		private final static int JOB_APPEND = 1;
		private final static int JOB_ADDBGM = 2;

		private ArrayList<String> videoList = null;
		private String bgm = null;
		private String outPath = null;
		public int type = JOB_READY;

		public Mp4ParserProperty(int type, String outPath, ArrayList<String> videoList)
		{
			this.type = type;
			this.videoList = videoList;
			this.outPath = outPath;
		}

		public Mp4ParserProperty(int type, String outPath, ArrayList<String> videoList,String bgm)
		{
			this.type = type;
			this.videoList = videoList;
			this.bgm = bgm;
			this.outPath = outPath;
		}

		public boolean checkNotNullForAppend(){
			if (videoList.isEmpty())
				return true;
			if (outPath == null)
				return true;
			return false;
		}

		public boolean checkNotNullForAddBgm(){
			if (videoList.isEmpty()) {
				return true;
			}
			if (bgm == null) {
				return true;
			}
			if (outPath == null){
				return true;
			}
			return false;
		}
	}

}

//		String[] list = property.videoList.toArray(new String[property.videoList.size()]);


//	Movie[] inMovies = new Movie[count];
//	for (int i = count - 1; i >= 0; i--) {
//		File file = new File(lv1List.get(i));
//		if (file.exists())
//	{ try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } }
//	List videoTracks = new LinkedList ();
//	List audioTracks = new LinkedList ();
//	for (Movie m : inMovies)
//	{ for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } }
//	try {
//		MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath));
//		CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort);
//	}catch (Exception e) {
//		e.printStackTrace();
//	}
//		Movie result = new Movie();
//		try {
//			if (audioTracks.size() > 0){
//				result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
//			}
//			if (videoTracks.size() > 0){
//				result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//
//		try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }   Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }   Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }   Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }   Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }  Movie[] inMovies = new Movie[count]; for (int i = count - 1; i >= 0; i--) { File file = new File(lv1List.get(i)); if (file.exists()) { try { inMovies[counter] = MovieCreator.build(file.getAbsolutePath()); counter++; } catch (Exception e) { Log.d("mp4parse", e.getMessage()); } } } List videoTracks = new LinkedList (); List audioTracks = new LinkedList (); for (Movie m : inMovies) { for (Track t : m.getTracks()) { if (t.getHandler().equals("vide")) { videoTracks.add(t); } } } try { MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(audiopath)); CroppedTrack aacTrackShort = new CroppedTrack(aacTrack, 1, aacTrack.getSamples().size()); audioTracks.add(aacTrackShort); } catch (Exception e) { e.printStackTrace(); } Movie result = new Movie(); try { if (audioTracks.size() > 0) { result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()]))); } if (videoTracks.size() > 0) { result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()]))); } } catch (Exception e) { e.printStackTrace(); } try { Container out = new DefaultMp4Builder().build(result); String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MERGEDoutput" + Long.toString(System.currentTimeMillis()) + ".mp4"; FileOutputStream fos = new FileOutputStream(new File(filename)); out.writeContainer(fos.getChannel()); fos.close(); } catch (Exception e) { }


//
// if(CUSTOMMUSIC==true) {
//		 //removing headers
//		 Mp3File mp3file = new Mp3File(realpathtomusic);
//		 if (mp3file.hasId3v1Tag()) {
//		 mp3file.removeId3v1Tag();
//		 Log.d("MP3agic", "removeId3v1Tag");
//		 }
//		 if (mp3file.hasId3v2Tag()) {
//		 mp3file.removeId3v2Tag();
//		 Log.d("MP3agic", "removeId3v2Tag");
//		 }
//		 if (mp3file.hasCustomTag()) {
//		 mp3file.removeCustomTag();
//		 Log.d("MP3agic", "removeCustomTag");
//		 }
//
//		 String tempdir = String.format(Environment.getExternalStorageDirectory() + "/Logit/temp.mp3");
//		 File file = new File(tempdir);
//		 if (file.exists())
//		 file.delete();
//		 mp3file.save(tempdir);
//		 MP3TrackImpl aacTrack = new MP3TrackImpl(new FileDataSourceImpl(tempdir));
//		 CroppedTrack croppedaacTrack = new CroppedTrack(aacTrack, 0, (long) ((lengthInSeconds * 1000) / 26));
//		 result.addTrack(croppedaacTrack);
//		 }
//		 else{
//		 AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(realpathtomusic));
//		 CroppedTrack croppedaacTrack = new CroppedTrack(aacTrack, 0, (long) ((lengthInSeconds * 1000) / 26));
//		 result.addTrack(croppedaacTrack);
//		 }
//
//		 if (videoTracks.size() > 0) {
//		 result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
//		 }
//
//		 BasicContainer out = (BasicContainer) new DefaultMp4Builder().build(result);