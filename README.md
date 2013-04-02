D3 Bot
=============

This is a D3 bot that runs a loop of leoric's manor, D3B has spatial awareness, tracks gold per hour (GPH), reacts and uses skills based on your health, and can log everything to a dropbox file so you can monitor it on the go

BEFORE USING THIS
-------------

Please ensure the currently opened quest is the last one in act 1. The program should not eat too much power but it is doing some image recognition on a 700x700 square around your character to look for enemies and items. If this is too much please reduce 'awareness_degree'. 
The default is 4 meaning it examines the 700x700 image in 175x175 (700/4) or 30625 places. It is buffered in memory though so this should not be too resource intensive.

Why Leoric's Manor
-------------

I chose to make the bot run through this section as it has good mob density, is easy to do in ~1 minute, and is easy to do at higher MPs, an average run with goldfind should net around 25k per run, or 1.5m gold per hour (not including drops).
We also don't need to do any pathing and save a lot of cpu cycles :)

Optimal settings
-------------

The defaults pixel coordinates it checks against are sized to fit a 1920x1080 resolution, mouse_click_offset_x and mouse_click_offset_y may need to be adjusted depending on your screensize.
The run timing may be too fast (and your character won't make it to the final stairs, instead getting stuck in the room prior), either adjust the in_game_steps values in the run_leoric() function or adjust the run_speed (recommended).
The skills I am using might not make sense for your character, please take a second to scan through the functions and adjust anything that might need to be fine tuned (marked with a //FINE TUNE THIS DEPENDING comment)

Will I get caught
-------------

No. Be kind and courteous to everyone on your friends list (or remove them). The program used the java.awt.Robot class which is very similar to an AutoIt bot. The most likely way you will be banned is from a player reporting you.