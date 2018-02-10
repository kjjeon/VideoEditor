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

		Movie result = makeMovieFromTrack(videoTracks, audioTracks);
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

	private Movie makeMovieFromTrack(List<Track> videoTracks, List<Track> audioTracks){

		Movie result = new Movie();

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
			mListener.onFininsh(mp4ParserProperty.getJobType(),mp4ParserProperty.getOutPath());
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