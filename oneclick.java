//This is older, more shit code. My newer scripts won't make you want to take your eyes out.
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.SkillTracker;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;

@ScriptManifest(author = "purepanic", description = "Progressive fisher that gets from 1-82 fishing.", name = "One-Click Fisher", category = Category.FISHING, version = 0.01)
public class oneclick extends AbstractScript {
	Area shrimp = new Area(3245, 3158, 3237, 3142, 0);
	Area flyfish = new Area(new Tile(3108, 3435), new Tile(3109, 3434), new Tile(3109, 3433), new Tile(3109, 3432),
			new Tile(3108, 3433), new Tile(3107, 3434), new Tile(3107, 3433), new Tile(3106, 3434),
			new Tile(3104, 3430), new Tile(3105, 3430), new Tile(3106, 3432), new Tile(3102, 3424),
			new Tile(3103, 3425), new Tile(3102, 3425), new Tile(3102, 3426), new Tile(3102, 3427));
	public long startTime;
	public long runTime;
	public int startlvl;
	public int currentlvl;
	public int gained;
	public String task = "IDLE";
	public int varplevel = 0;
	
	State state;

	@Override // Infinite loop
	public int onLoop() {

		// Determined by which state gets returned by getState() then do that case.
		switch (getState()) {
		case SHRIMP:
			if (!shrimp.contains(Players.localPlayer())) {
				if (!Players.localPlayer().isMoving()) {
					Walking.walk(shrimp.getRandomTile());
					task = "WALK";
					sleepUntil(() -> !Players.localPlayer().isMoving(), 10000);
				}
			} else if (shrimp.contains(Players.localPlayer())) {
				if (Inventory.contains("Small fishing net")) {
					NPC shrimpspot = NPCs.closest(f -> f != null && f.getName().equals("Fishing spot"));
					if (Inventory.isFull()) {
						task = "DROP";
						if (Inventory.contains("Raw shrimps")) {
							if (Inventory.dropAll("Raw shrimps", "Raw anchovies")) {
								log("Dropped all raw shrimp");
								return 1000;
							}
						}
					} else if (shrimpspot.exists()) {
						// start fishing
						task = "NET";
						if (!Players.localPlayer().isAnimating()) {
							if (shrimpspot.interact("Net")) {
								sleep(5000);
								sleepUntil(() -> !Players.localPlayer().isAnimating() | Inventory.isFull(), 60000);
							}
						}
					}
				} else {
					log("No Small fishing net");
					stop();
				}
			}
			break;
		case FLYFISH:
			if (!flyfish.contains(Players.localPlayer())) {
				if (!Players.localPlayer().isMoving()) {
					Walking.walk(flyfish.getRandomTile());
					task = "WALK";
					sleepUntil(() -> !Players.localPlayer().isMoving(), 10000);
					return 2000;
				}
			} else if (flyfish.contains(Players.localPlayer())) {
				if (Inventory.contains("Fly fishing rod") && Inventory.contains("Feather")) {
					NPC flyfishspot = NPCs.closest(r -> r != null && r.getName().equals("Rod Fishing spot"));
					if (Inventory.isFull()) {
						task = "DROP";
						if (Inventory.dropAll("Raw trout", "Raw salmon")) {
							log("dropped all stuff");
							return 1000;
						}
					} else if (flyfishspot.exists()) {
						task = "LURE";
						if (!Players.localPlayer().isAnimating()) {
							if (flyfishspot.interact("Lure")) {
								sleep(5000);
								sleepUntil(() -> !Players.localPlayer().isAnimating() | Inventory.isFull(), 60000);
							}
						}
					}
				} else {
					stop();
					log("Does not have required materials");
				}
			}
			break;
		}
		return 0;
	}

	// State names
	private enum State {
		SHRIMP, FLYFISH
	}

	// Checks if a certain condition is met, then return that state.
	private State getState() {
		if (Skills.getRealLevel(Skill.FISHING) < 20) {
			state = State.SHRIMP;
		} else if (Skills.getRealLevel(Skill.FISHING) > 19) {
			state = State.FLYFISH;
		} else {
			stop();
		}
		return state;
	}

	// When script start load this.
	public void onStart() {
		log("Bot started");
		startTime = System.currentTimeMillis();
		startlvl = Skills.getRealLevel(Skill.FISHING);
		SkillTracker.start();
	}

	// When script ends do this.
	public void onExit() {
		log("Bot Ended");
	}

	private String formatTime(final long ms) {
		long s = ms / 1000, m = s / 60, h = m / 60, d = h / 24;
		s %= 60;
		m %= 60;
		h %= 24;

		return d > 0 ? String.format("%02d:%02d:%02d:%02d", d, h, m, s) : h > 0 ? String.format("%02d:%02d:%02d", h, m, s) : String.format("%02d:%02d", m, s);
	}

	@Override
	public void onPaint(Graphics g) {
		runTime = System.currentTimeMillis() - startTime;
		currentlvl = Skills.getRealLevel(Skill.FISHING);
		gained = currentlvl - startlvl;
		g.setColor(new Color(208, 190, 155, 255));
		g.fillRect(5, 460, 110, 12);
		g.setColor(Color.GREEN);
		g.drawRect(4, 270, 401, 68);
		g.setColor(new Color(0, 0, 0, 150));
		g.fillRect(5, 271, 400, 67);

		// custom stuff I can send this if you want

		// Main paint stuff
		if (Players.localPlayer() != null) {
			g.setFont(new Font("Arial", Font.PLAIN, 13));
			g.setColor(new Color(220, 220, 220, 255));
			g.drawString("Runtime : " + formatTime(runTime), 12, 310);
			g.drawString("Fishing : " + Skills.getRealLevel(Skill.FISHING) + " | + " + gained, 239, 290);
			g.drawString("XP/HR : " + SkillTracker.getGainedExperiencePerHour(Skill.FISHING) , 239, 310);
			g.drawString("Task : " + task, 239, 330);
			g.setFont(new Font("Arial", Font.BOLD, 15));
			g.drawString("1-82 AIO fisher v" + getVersion(), 12, 290);
			g.setFont(new Font("Arial", Font.PLAIN, 10));
			g.drawString("Script by PurePanic", 12, 323);
			g.drawString("Paint by Smile", 12, 333);
			

		}
	}

}
