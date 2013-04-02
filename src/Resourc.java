import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.Icon;
import javax.swing.ImageIcon;


public class Resourc {
	
	public Resourc() {
	}
	public void playSound(final String url) {
		if (!Main.enable_sound) return;
		try {
		    AudioInputStream stream;
		    Clip clip;
	        clip = AudioSystem.getClip();
	        stream = AudioSystem.getAudioInputStream(this.getClass().getResource("/resource/" + url));
	        clip.open(stream);
	        clip.start(); 

		} catch (Exception e) {
			Main.add_text("ERROR: can not play sound (" + url + ")");
			e.printStackTrace();
		}
	}
}
