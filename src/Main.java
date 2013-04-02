import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;
import com.sun.awt.AWTUtilities;


public class Main {

	/**
	 * @AUTHOR SGB
	 * @category Diablo 3 leoric manor runs
	 * @URL https://github.com/SPGB/d3b
	 * note: this was created for 1920x1080 res and the skills may need to be adjusted, 
	 * I am running this on a DH with grenades / spike traps on LMB/RMB
	 * with these skills: 1,gloom 2,guardian turret 3,smoke screen 4,ferret
	 * ferret helps a lot with gold find
	 */
	  static int mouse_click_offset_x = 0; //for users with width != 1920
	  static int mouse_click_offset_y = 0; //for users with height != 1080
	  static double run_speed = 1; //adjust depending on your characters speed
	  static int awareness_degree = 4; //how closely to look for enemies and items
	  
	  static int main_text_int = Integer.parseInt( "FFFFFF",16); //main text color for overlay
	  static int main_background_int = Integer.parseInt( "FFFFFF",16); //background is by default transparent
	  static Color main_text = Color.white;
	  static Color main_background = new Color(main_background_int);
	  static Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	  static JWindow  frame = new JWindow ();
	  static Robot rob;
	  static Point mouse_location;
	  static double version = 0.9;
	  static long combat_time = 0;
	  static boolean debug = true;
	  static String[] log = new String[18];
	  static String last_message = "";
	  static String action = "";
	  static String log_file = "C:\\d3b_log.txt"; //use whatever file location you want here, feeding it into the dropbox folder lets you check on your bot across devices
	  static Resourc re;
	  static int combat_looting_counter = 0; //only loot in combat 1/2 the time
	  static boolean enemy_bar = false;	
	  static boolean enemy_detection = false;
	  static boolean enable_sound = true;
	  static boolean window_hidden = true;
	  static boolean start_to_close = false;
	  static long start_close_time;
	  static int[] compare_color_quest_0 = new int[]{60,10,0};
	  static int[] compare_color_quest_1 = new int[]{15,2,0};
	  static int[] compare_color_frame = new int[]{139,131,121};
	  static int[] compare_color_waypoint = new int[]{45,35,30};
	  static int[] compare_color_dead = new int[]{255,255,255};
	  static int[] compare_color_health = new int[]{150,20,10};
	  static int[] compare_color_damaged = new int[]{255,240,0};
	  static int[] compare_color_legendary = new int[]{191,100,47};
	  static int[] compare_color_set = new int[]{1,205,1};
	  static int[] compare_color_gem = new int[]{153,187,255};
	  static int[] compare_color_monster = new int[]{240,1,1};
	  static double in_game_steps = 0;
	  
	  static int runs = 0;
	  static int deaths = 0;
	  static int streak = 0;
	  static long last_gloom = 0; //ensure it's not cast once in 5 seconds
	  static long last_ss = 0;
	  static long last_awareness_check = 0;
	  static long time_between_awareness_checks = 1000;
	  static long start_time = 0;
	  static long end_time = 0;
	  static double avg_time = 0;
	  static long last_enemy_spotted = 0;
	  
	  //caching
	  static int starting_gold = 0;
	  static int ending_gold = 0;
	  static int net_gold = 0;
	  static int avg_gold = 0;
	  static int hourly_gold = 0;
	  
	  static Dimension dim;
	  static BufferedImage img = new BufferedImage(305, 12, BufferedImage.TYPE_INT_RGB); //create an image to store the screen capture
	  static BufferedImage img_spatial_awareness_check = new BufferedImage(700, 700, BufferedImage.TYPE_INT_RGB); //create an image to store the screen capture
	  static BufferedImage img_gold_check = new BufferedImage(115, 18, BufferedImage.TYPE_INT_RGB); //create an image to store the screen capture
	  static JTextPane text = new JTextPane() {
		private static final long serialVersionUID = 1L;
		{setOpaque(false);}
	  	  {setForeground(main_background);}		
	    };
	    static JScrollPane sp = new JScrollPane(text);   // JTextArea is placed in a JScrollPane.
	    
	public static void main(String[] args) {
	        for (int i = 0; i < log.length; i++) log[i] = ""; //init the log
		   add_text("D3B Initializing - version " + version);  //let everyone know what program is running
		   re = new Resourc(); //for loading sound files
		   Toolkit toolkit =  Toolkit.getDefaultToolkit ();
		   dim = toolkit.getScreenSize();
		   add_text("Screensize: " + screenSize.width + "x" + screenSize.height);
		   add_text("debug: " + ((debug)? "yes" : "no"));
		   re.playSound("beep.wav");
		   try {
				rob = new Robot();
		   } catch (AWTException e1) {
			   add_text("ERROR: Could not load java.awt(Robot)");
			   System.exit(0);
		   }
		   
		   new Thread(new Runnable() { //our main loop's thread
			    	public void run() {
			    		while(true) {
				    		try {
				    			main_thread();
				    			Thread.sleep(75);
				    		} catch(Exception e) { 
				    			add_text("ERROR: " + e.getMessage());
				    			e.printStackTrace();
				    		} 
			    		}
			      }
		   }).start();
		   
		   text.setEditable(false);
		   text.setFocusable(false);
		   DefaultCaret caret = (DefaultCaret)text.getCaret();
		   caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	
		   com.sun.awt.AWTUtilities.setWindowOpaque(frame, false);
		   frame.setBackground(new Color(0f, 0f, 0f, 0.1f));
		   text.setBackground(new Color(0f, 0f, 0f, 0.1f));
		   sp.setBackground(new Color(0f, 0f, 0f, 0.1f));
	       frame.setAlwaysOnTop(true);
	       frame.setFocusable(false);
	       frame.setVisible(true);
	       //click through
	       System.setProperty("sun.java2d.noddraw", "true");
	       AWTUtilities.setWindowOpaque (frame, false );
	       AWTUtilities.setWindowOpacity (frame, 0.5f );


	       frame.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);
	       frame.getContentPane().setLayout(new java.awt.BorderLayout());
		   frame.add(text);
		   frame.setLocation(screenSize.width - 300,screenSize.height / 2);
		   frame.setPreferredSize(new Dimension(300, 300));
		   sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		   sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		   
		   frame.pack();
	}

	public static void main_thread() {	
		check_exit();	//move cursor to top left then bottom left to exit
		check_frame(); //check if we're in game
		if (in_game_steps == 0) {
			update_quest(); //we're in lobby - enter new game
			return;
		}
		if (!window_hidden) {
			run_leoric(); //the meat of the program
			if (in_game_steps > 155) awareness_check(); //check for enemies and loot
		}
		if (in_game_steps > 290) {
			if (has_health(90)) {
				if (check_armor()) {
					repair();
				} else {
					leave_game();
				}
			}
		}
	}
	public static void run_leoric() {
		if (in_game_steps == 10) {
			runs++;
			int success_rate = (int) (100.0 - (((double) deaths / (double) runs) * 100.0));
			add_text("--starting run--");
			check_gold();
			start_time = System.currentTimeMillis();	
			add_text("run: " + runs + " (" + success_rate + "% success)");
			add_text("streak: " + streak + ", deaths: " + deaths);
			key("4"); //FINE TUNE THIS DEPENDING (currently used to summon ferrets)
		}
		if (in_game_steps == 15) mouse_click(550,370); //go to waypoint
		if (in_game_steps == 35) {
			Color j = rob.getPixelColor(30,30); //TODO: switch from fixed position
			if (pixel_compare(j.getRed(), j.getGreen(), j.getBlue(), compare_color_waypoint)) {
				if (debug) add_text("found waypoint");
				mouse_click(465,820); //scroll on waypoint
			} else {
				 mouse_click(screenSize.width / 2,(screenSize.height / 2) - 35);
				return;
			}
		}
		if (in_game_steps == 50) mouse_click(300,690); //click waypoint

		if (in_game_steps == 75) mouse_click(1320,710); //open doors
		if (in_game_steps == 90) mouse_click(955,510); //ensure they are open
		if (in_game_steps == 100) mouse_click(955,510); //ensure they are open
		if (in_game_steps == 115) {
			mouse_click(1320,835); //check body
			mouse_click(1320,835); //check body
		}
		if (in_game_steps == 125) {
			//if (debug) add_text("moving to stairs");
			mouse_click(screenSize.width - 20,215); //go up stairs
		}
		if (in_game_steps == 135) mouse_click(screenSize.width - 20,215); //go up stairs

		if (in_game_steps > 155 && in_game_steps < 189) { //go to corner
			if (has_health(90)) {
				if (in_game_steps > 167) {
					mouse_click(960, 800); //end corner run
				} else {
					mouse_click(1190, 900); //begin corner run
				}
			} else {
				in_game_steps--;
			}
			smokescreen_if_hurt();
			skills_if_hurt();
			if (in_game_steps > 160 && in_game_steps % 2 == 0) attack_if_hurt(100, screenSize.height - 100);
		}
		if (in_game_steps == 189 && last_gloom == 0) { key("1"); } 
		if (in_game_steps > 188 && in_game_steps < 270) { //move to final stairs
			if (in_game_steps < 250) {
				attack(screenSize.width - 100, 0);
			} else {
				if (in_game_steps % 2 == 0) {
					attack(screenSize.width - 100, screenSize.height - 200);
				} else {
					attack(screenSize.width / 2, screenSize.height / 2);
				}
			}
			skills_if_hurt();
			if (in_game_steps > 220 && in_game_steps % 2 == 0) { //only take steps when at full hp
				if (has_health(90)) {
					mouse_click(1520, 110);
				} else {
					in_game_steps--;
				}
			}
		}
		in_game_steps += run_speed;
		check_dead();
	}
	public static void awareness_check() { //scan across looking for the orange glow, if found follow it to the base and click
		if (last_awareness_check > System.currentTimeMillis()) return; //only once every 500ms
		last_awareness_check = System.currentTimeMillis() + time_between_awareness_checks; // + 500ms
		int awareness_baseline_x = (screenSize.width / 2) - 350, awareness_baseline_y = (screenSize.height / 2) - 350;
		img_spatial_awareness_check = rob.createScreenCapture(new Rectangle(awareness_baseline_x,awareness_baseline_y,700,700)); //grab the screen
		int approach_rgb = 0;
		int approach_b, approach_g, approach_r;
		for (int x = 0; x < 700; x += awareness_degree) {
			for (int y = 0; y < 700; y += awareness_degree) {
				approach_rgb = img_spatial_awareness_check.getRGB( x,y ); //get rgb values
				approach_b = (approach_rgb)&0xFF; //these look to be switched for some reason IMPORTANT!
				approach_g = (approach_rgb>>8)&0xFF; //green
				approach_r = (approach_rgb>>16)&0xFF; //blue
				if (y > 2 && pixel_compare(approach_r, approach_g, approach_b, compare_color_legendary)) {
					approach_rgb = img_spatial_awareness_check.getRGB( x,y - 2); //get rgb values
					approach_b = (approach_rgb)&0xFF; //these look to be switched for some reason IMPORTANT!
					approach_g = (approach_rgb>>8)&0xFF; //green
					approach_r = (approach_rgb>>16)&0xFF; //blue
					if (approach_b < 30 && approach_g < 30 && approach_r < 30) {
						add_text("*found legendary");
						re.playSound("beep.wav");
						mouse_click(awareness_baseline_x + x, awareness_baseline_y + y);
						last_enemy_spotted = System.currentTimeMillis() + 4000;
					break;
					}
				}
				if (pixel_compare(approach_r, approach_g, approach_b, compare_color_set)) {
					add_text("*found set item");
					re.playSound("beep.wav");
					mouse_click(awareness_baseline_x + x, awareness_baseline_y + y);
					last_enemy_spotted = System.currentTimeMillis() + 4000;
					break;
				}
				if (pixel_compare(approach_r, approach_g, approach_b, compare_color_monster)) {
					attack(awareness_baseline_x + x, awareness_baseline_y + y + 50);
					last_enemy_spotted = System.currentTimeMillis() + 2000;
					last_awareness_check = System.currentTimeMillis() + 200; // + 500ms
					break;
				}
				if (y > 2 && pixel_compare(approach_r, approach_g, approach_b, compare_color_gem)) {
					approach_rgb = img_spatial_awareness_check.getRGB( x,y - 2); //get rgb values
					approach_b = (approach_rgb)&0xFF; //these look to be switched for some reason IMPORTANT!
					approach_g = (approach_rgb>>8)&0xFF; //green
					approach_r = (approach_rgb>>16)&0xFF; //blue
					if (approach_b < 30 && approach_g < 30 && approach_r < 30) {
						//add_text("*found gem or potion");
						mouse_click(awareness_baseline_x + x, awareness_baseline_y + y);
						last_enemy_spotted = System.currentTimeMillis() + 2000;
						break;
					}
				}
			}
		}
		
	}
	public static void check_gold() {
		key("i");
		img_gold_check = rob.createScreenCapture(new Rectangle(1457+mouse_click_offset_x,510+mouse_click_offset_y,115,18)); //grab the screen
		int temp_gold = 0;
		temp_gold += check_number(40) * 1000000; //millions
		temp_gold +=  check_number(53) * 100000;
		temp_gold +=  check_number(63) * 10000;
		temp_gold +=  check_number(73) * 1000; //thousands
		temp_gold +=  check_number(86) * 100; //hundreds
		temp_gold +=  check_number(95) * 10; //hundreds
		temp_gold +=  check_number(104) * 1; //hundreds
		key("i");
		if (starting_gold == 0) {
			starting_gold = temp_gold;
			add_text("starting gold: " + starting_gold);
		} else {
			ending_gold = temp_gold;
			net_gold = ending_gold - starting_gold;
			if (avg_gold == 0) avg_gold = net_gold;
			avg_gold = (avg_gold + net_gold) / 2;
			starting_gold = ending_gold;	
			hourly_gold = (int) (((1.0 * avg_gold) / avg_time) * 60);
			add_text("net gold: " + net_gold + ", gold per hour: " + hourly_gold);
		}
	}
	public static int check_number(int x) {
		int white_count_0 = 0, white_count_1 = 0, white_count_2 = 0, white_count_3 = 0; //number of white pixels, at two places
		int text_sample = 0;
		int sample_r = 0, sample_g = 0, sample_b = 0;
		for (int i = 0; i < 10; i++) { //go across and count the white pixels
			text_sample = img_gold_check.getRGB( x + i,0); //get rgb values
			sample_b = (text_sample)&0xFF; //these look to be switched for some reason IMPORTANT!
			sample_g = (text_sample>>8)&0xFF; //green
			sample_r = (text_sample>>16)&0xFF; //blue
			if (sample_b > 230 && sample_g > 230 && sample_r > 230) white_count_0++;
			
			text_sample = img_gold_check.getRGB( x + i,3); //get rgb values
			sample_b = (text_sample)&0xFF; //these look to be switched for some reason IMPORTANT!
			sample_g = (text_sample>>8)&0xFF; //green
			sample_r = (text_sample>>16)&0xFF; //blue
			if (sample_b > 230 && sample_g > 230 && sample_r > 230) white_count_1++;
			
			text_sample = img_gold_check.getRGB( x + i,7); //get rgb values
			sample_b = (text_sample)&0xFF; //these look to be switched for some reason IMPORTANT!
			sample_g = (text_sample>>8)&0xFF; //green
			sample_r = (text_sample>>16)&0xFF; //blue
			if (sample_b > 230 && sample_g > 230 && sample_r > 230) white_count_2++;
			
			text_sample = img_gold_check.getRGB( x + i,11); //get rgb values
			sample_b = (text_sample)&0xFF; //these look to be switched for some reason IMPORTANT!
			sample_g = (text_sample>>8)&0xFF; //green
			sample_r = (text_sample>>16)&0xFF; //blue
			if (sample_b > 230 && sample_g > 230 && sample_r > 230) white_count_3++;
		}
		
		if (white_count_0 == 0 && white_count_1 == 1 && white_count_2 == 1 && white_count_3 == 1) return 1;
		if (white_count_0 == 2 && white_count_1 == 2 && white_count_2 == 1 && white_count_3 == 7) return 2;
		if (white_count_0 == 2 && white_count_1 == 0 && white_count_2 == 1 && white_count_3 == 2) return 3;
		if (white_count_0 == 0 && white_count_1 == 0 && white_count_2 == 1 && white_count_3 == 0) return 4;
		if (white_count_0 == 5 && white_count_1 == 0 && white_count_2 == 1 && white_count_3 == 2) return 5;
		if (white_count_0 == 2 && white_count_1 == 1 && white_count_2 == 2 && white_count_3 == 1) return 6;
		if (white_count_0 == 7 && white_count_1 == 1 && white_count_2 == 1 && white_count_3 == 0) return 7;
		if (white_count_0 == 2 && white_count_1 == 1 && white_count_2 == 2 && white_count_3 == 3) return 8;
		if (white_count_0 == 2 && white_count_1 == 2 && white_count_2 == 1 && white_count_3 == 2) return 9;
		return 0; 
	}
	public static boolean has_health(int percent) { //this sometimes gives false positives, but better on the safe side
		int confidence = 0; //it is setup with a confidence int instead of just a true false bool so that it can easily be expanded later to give less false positives at a higher cycle cost
		Color j = rob.getPixelColor(540 + mouse_click_offset_x,1040 - percent + mouse_click_offset_y);
		if (j.getRed() > 110 && j.getGreen() < 45 && j.getBlue() < 45) confidence++;
		if (confidence > 0) {
			return true;
		}
		return false;
	}
	
	public static boolean check_armor() {
		//check if top right is red
		Color j = rob.getPixelColor(1518+mouse_click_offset_x,38+mouse_click_offset_y); //TODO: switch from fixed position
		if (pixel_compare(j.getRed(), j.getGreen(), j.getBlue(), compare_color_damaged)) {
			return true;
		}
		return false;
	}
	public static void repair() {
		add_text("repairing");
		key("t");
		try {
			Thread.sleep(8000 + get_random_int());
		} catch (InterruptedException e) {	}
		mouse_click(screenSize.width - 100, 50); //move right
		try {
			Thread.sleep(7000 + get_random_int());
		} catch (InterruptedException e) {	}
		mouse_click(730, 210); //click merchant
		mouse_click(730, 210); //click merchant
		try {
			Thread.sleep(4000 + get_random_int());
		} catch (InterruptedException e) {	}
		mouse_click(510, 490); //click tab
		mouse_click(280, 590); //repair all
		try {
			Thread.sleep(1000 + get_random_int());
		} catch (InterruptedException e) {	}
		key("ESC");
		leave_game();
	}
	public static void skills_if_hurt() { //1 and 2
		if (last_gloom > System.currentTimeMillis()) return;
		if (!has_health(70)) {
			key("1");
			key("2"); //turret
			last_gloom = System.currentTimeMillis() + 4500;
			for (int i = 0; i < 2; i++) {
				rob.mousePress(InputEvent.BUTTON3_DOWN_MASK);
				try {
					Thread.sleep(40 + get_random_int());
				} catch (InterruptedException e) {	}
				rob.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
			}
		}
		if (!has_health(20)) {
			if (debug) add_text("using potion");
			key("q");
		}
	}
	public static void smokescreen_if_hurt() {
		if (last_ss > System.currentTimeMillis()) return;
		if (!has_health(30)) {
			last_ss = System.currentTimeMillis() + 8000; //FINE TUNE THIS DEPENDING (currently used to track guardian turret cooldown)
			key("3");//FINE TUNE THIS DEPENDING (currently used to lingering fog)
			key("2"); //FINE TUNE THIS DEPENDING (currently used to summon guardian turret)
		}
	}
	public static void attack_if_hurt(int x, int y) {
		if (!has_health(90)) {
			key("1"); //FINE TUNE THIS DEPENDING (currently used to summon gloom)
			attack(x, y);
			last_gloom = System.currentTimeMillis() + 4500; //FINE TUNE THIS DEPENDING (currently used to track my last gloom cast / cooldown)
			in_game_steps--;
		}
	}
	public static void check_dead() {
		Color j = rob.getPixelColor(543+mouse_click_offset_x,333+mouse_click_offset_y); //TODO: switch from fixed position
		Color j2 = rob.getPixelColor(1268+mouse_click_offset_x,330+mouse_click_offset_y); //TODO: switch from fixed position
		if (pixel_compare(j.getRed(), j.getGreen(), j.getBlue(), compare_color_dead) && pixel_compare(j2.getRed(), j2.getGreen(), j2.getBlue(), compare_color_dead)) {
			deaths++;
			streak = 0;
			add_text("you have died (" + deaths + " times over " + runs + " runs)");
			try {
				Thread.sleep(5000 + get_random_int());
			} catch (InterruptedException e) {	}
			mouse_click(920,850);
			try {
				Thread.sleep(1000 + get_random_int());
			} catch (InterruptedException e) {	}
			leave_game();
		}
	}
	public static void leave_game() {
		if (last_enemy_spotted > System.currentTimeMillis()) return; //still doing something
		in_game_steps = 0;
		streak++;
		end_time = System.currentTimeMillis();
		add_text("--completed run--");
		double last_run_length = (end_time - start_time) / 1000.0 / 60.0;
		if (avg_time == 0) avg_time = last_run_length; else avg_time = (avg_time + last_run_length) / 2;
		add_text("run length: " + (Math.round(last_run_length * 100) / 100.0) + " minutes");
		if (avg_time != 0) add_text("average run length: " + (Math.round(avg_time * 100) / 100.0) + " minutes");
		try {
			Thread.sleep(500 + get_random_int());
		} catch (InterruptedException e) {	}
		key("ESC");
		try {
			Thread.sleep(1000 + get_random_int());
		} catch (InterruptedException e) {	}
		mouse_click(900,580);
	}
	public static void check_exit() {
		mouse_location = new Point(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y);
		if (mouse_location.x == 0 && mouse_location.y == 0 && start_to_close == false) {
			add_text("Confirm closing by moving mouse to bottom left");
			re.playSound("beep.wav");
			start_to_close = true;
			start_close_time = System.currentTimeMillis();
		}
		if (start_to_close && mouse_location.x == 0 && mouse_location.y == dim.height - 1) {
			if (System.currentTimeMillis() - start_close_time < 5000) {
				re.playSound("beep.wav");
				System.exit(0);
			}
			start_to_close = false;
		}
	}
	public static void update_quest() {
		Color j = rob.getPixelColor(150+mouse_click_offset_x,420+mouse_click_offset_y);
		if (pixel_compare(j.getRed(), j.getGreen(), j.getBlue(), compare_color_quest_0)) { //at very beginning
			mouse_click(130,500);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {	}
			return;
		}
		Color j2 = rob.getPixelColor(150+mouse_click_offset_x,420+mouse_click_offset_y);
		if (pixel_compare(j2.getRed(), j2.getGreen(), j2.getBlue(), compare_color_quest_1)) { //at quest select
			if (debug) add_text("selecting quest");
			mouse_click(820,360); //move scroll bar
			try {
				Thread.sleep(1000 + get_random_int());
			} catch (InterruptedException e) {	}
			mouse_click(630,710); //click quest
			try {
				Thread.sleep(1000 + get_random_int());
			} catch (InterruptedException e) {	}
			//click select 
			mouse_click(1113,866);
			try {
				Thread.sleep(1000 + get_random_int());
			} catch (InterruptedException e) {	}
			//confirm
			mouse_click(880,636);
			try {
				Thread.sleep(1000 + get_random_int());
			} catch (InterruptedException e) {	}
			//click play
			mouse_click(330,420);
			try {
				Thread.sleep(1000 + get_random_int());
			} catch (InterruptedException e) {	}
			in_game_steps++;
		}
	}
	
	public static void check_frame() {
		Color j = rob.getPixelColor(504+mouse_click_offset_x,1023+mouse_click_offset_y);
		if (pixel_compare(j.getRed(), j.getGreen(), j.getBlue(), compare_color_frame)) {
			window_hidden = false; //in game
		} else { //can't find pixel
			window_hidden = true; //in lobby
		}
	}
	public static void attack(int x, int y) {
		rob.keyPress(KeyEvent.VK_SPACE);
		rob.mouseMove(x, y);
		rob.mousePress(InputEvent.BUTTON1_MASK);
		try {
			Thread.sleep((long) ((Math.random() * 10) + 100));
		} catch (InterruptedException e) {
			add_text("error trying to sleep on mouse click");
		}
		rob.keyRelease(KeyEvent.VK_SPACE);
		rob.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	public static void mouse_click(int x, int y) {
		rob.mouseMove(x + mouse_click_offset_x, y + mouse_click_offset_y);
		rob.mousePress(InputEvent.BUTTON1_MASK);
		try {
			Thread.sleep((long) ((Math.random() * 10) + 100));
		} catch (InterruptedException e) {
			add_text("error trying to sleep on mouse click");
		}
		rob.mouseRelease(InputEvent.BUTTON1_MASK);
	}
	//presses a key for a semi-random duration
	public static void key(String s) {
		int keyCode = 0;
		int length = 100;
		if (s.equals("f1"))
			keyCode = KeyEvent.VK_F1;  
		if (s.equals("f"))
			keyCode = KeyEvent.VK_F;  
		if (s.equals("w")) {
			keyCode = KeyEvent.VK_W;  
			length = 1000;
		}
		if (s.equals("t")) {
			keyCode = KeyEvent.VK_T;  
			length = 1000;
		}
		if (s.equals("i"))
			keyCode = KeyEvent.VK_I;  
		if (s.equals("r"))
			keyCode = KeyEvent.VK_R;  
		if (s.equals("q"))
			keyCode = KeyEvent.VK_Q;  
		if (s.equals("d")) {
			keyCode = KeyEvent.VK_D;  
			length = 1000;
		}
		if (s.equals("-"))
			keyCode = KeyEvent.VK_MINUS;  
		if (s.equals("TAB"))
			keyCode = KeyEvent.VK_TAB; 
		if (s.equals("ESC"))
			keyCode = KeyEvent.VK_ESCAPE; 
		if (s.equals("1"))
			keyCode = KeyEvent.VK_1; 
		if (s.equals("2"))
			keyCode = KeyEvent.VK_2; 
		if (s.equals("3"))
			keyCode = KeyEvent.VK_3; 
		if (s.equals("4"))
			keyCode = KeyEvent.VK_4; 
		if (s.equals("6"))
			keyCode = KeyEvent.VK_6; 			
		try {
			rob.keyPress(keyCode);
			Thread.sleep(length + get_random_int());
			rob.keyRelease(keyCode);
		} catch (Exception e) {
			add_text("ERROR: keypress fail (" + e.toString() + ")");
			e.printStackTrace();
		}
		
	}
	
	//compares the rgb values for a pixel with what the expected range should be
	public static boolean pixel_compare(int r, int g, int b, int[] color) {
		int variance = 5; //can be this amount above or below
		int color_r = color[0];
		int color_g = color[1];
		int color_b = color[2];
		return (r > color_r - variance && r < color_r + variance && 
			g > color_g - variance && g < color_g + variance &&
			b > color_b - variance && b < color_b + variance);
	}
	
	public static int get_random_int() {
		return (int) (Math.random() * 80);
	}
		    
	public static void add_text(String s) {
		Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        s = "[" + sdf.format(cal.getTime()) + "] " + s + "\n";
        for (int i = 0; i < log.length; i++) {
	        if (log[i].equals("")) {
	        	log[i] = s;
	        	break;
	        }
        }
        if (!log[log.length - 1].equals("")) {
            for (int i = 1; i < log.length; i++) {
            	log[i-1] = log[i];
            }
            log[log.length - 1] = s;
        }
        String compiled = "";
        for (String c : log) compiled = compiled + c;
		
		try {
			text.setText(compiled);        
			text.setCaretPosition(text.getDocument().getLength());
			FileWriter outFile = new FileWriter(log_file, true);
			outFile.append(s);
			outFile.close();
		} catch (Exception e){
			if (debug) add_text("chat error");
		}
	}
}
