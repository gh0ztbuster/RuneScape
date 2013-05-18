package scripts.jjsgopvip.methods;

import org.tribot.api.DynamicClicking;
import org.tribot.api.NPCChat;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.TranslatableString;

import scripts.jjsgopvip.enums.Wizard;
import scripts.jjsgopvip.utils.General;
import scripts.jjsgopvip.utils.Task;

public class Lobby {
	// Model IDs
	private final static long[] wizardIDs = {4192333971L, 4110945950L, 2956298545L,
									  		 4274446209L, 363022839L, 363022839L,
									  		 2870077833L, 1839433773L, 1926485814L};
	private final static long acanthaID = 3304958504L;
	private final static long viefID = 2646550081L;
	// English, German, French, Portuguese
	private final static TranslatableString joinOption1 = new TranslatableString("Join", "Mitspielen", "Rejoindre", "Participar"),
											joinOption2 = new TranslatableString("Join", "Beitreten", "Rejoindre", "Participar"),
											sureToJoinHeaderString = new TranslatableString("sure you want to join", "dass du mitmachen willst", "vouloir participer", "mesmo se juntar"),
											yesString = new TranslatableString("Yes", "Ja", "Oui", "Sim"),
											wizardHeaderString = new TranslatableString("Wizard", "Magier", "Magicien", "Mago"),
											viefHeaderString = new TranslatableString("Wizard Vief", "Magier Vief", "Magicien Vief", "Mago Vief"),
											acanthaHeaderString = new TranslatableString("Wizard Acantha", "Magierin Acantha", "Magicienne Acantha", "Maga Acantha");

	// Attempts to join the wizard of choice
	public static boolean joinWizard(Wizard wizard){
		switch(wizard){
		
			case ACANTHA: 	if(DynamicClicking.clickScreenModel(General.getScreenModel(acanthaID), 3)){
								return Timing.waitChooseOption(joinOption1.getTranslation(), 500) 
									   || 
									   Timing.waitChooseOption(joinOption2.getTranslation(), 500);
							}
							break;

			case VIEF: 		if(DynamicClicking.clickScreenModel(General.getScreenModel(viefID), 3)){
								return Timing.waitChooseOption(joinOption1.getTranslation(), 500) 
									   || 
									   Timing.waitChooseOption(joinOption2.getTranslation(), 500);
							}
							break;
			
			case WIZARD: 	if(DynamicClicking.clickScreenModel(General.getScreenModel(wizardIDs), 3)){
								return Timing.waitChooseOption(joinOption1.getTranslation(), 500)
									   ||
									   Timing.waitChooseOption(joinOption2.getTranslation(), 500);
							}
							break;
		}
		return false;
	}

	// Will handle the chat with one of the wizards to join
	public static boolean confirmJoin(){
		// Wait until we have stopped moving
		General.waitUntilNotMoving();
		long t = System.currentTimeMillis();
		
		while(Timing.timeFromMark(t) < 3000){
			// Checking if we need to confirm with yes
			String header = NPCChat.getName();
			if(header.contains(sureToJoinHeaderString.getTranslation())){
				//System.out.println("We need to confirm if we need to join");
				String[] options = NPCChat.getOptions();
				for(String option : options){
					//System.out.println("Option: " + option);
					if(option.contains(yesString.getTranslation())){
						//System.out.println("Found Yes");
						NPCChat.selectOption(yesString.getTranslation(), true);
					}
				}
			}
			
			// Checking if we got kicked
			int time;
			if((time = gotKicked()) > 0){
				handleKick(time);
				return false;
			}
			
			// Checking for barrier generator
			if(General.inTeam()){
				System.out.println("Joined a team succesfully!");
				return true;
			}

			Task.sleep(50, 100);
		}

		return false;
	}
	
	// Checks if we got kicked based on NPC message
	public static int gotKicked(){
		// Check if we are talking to wizard/vief/acantha
		String name = NPCChat.getName();
		//System.out.println("NPC name: " + name);
		if(name.contains(wizardHeaderString.getTranslation()) ||
		   name.contains(acanthaHeaderString.getTranslation()) ||
		   name.contains(viefHeaderString.getTranslation())){
			//System.out.println("Talking to Wizard/Vief/Acantha");
			
			// Checking if there is a number in the String
			String msg = NPCChat.getMessage();
			//System.out.println("NPC message: " + msg);
			
			try {
				int time = Integer.parseInt(msg.replaceAll("\\D", ""));
				if(time > 0 && time <= 10){
					System.out.println("We have been kicked, waiting for " + time + " minutes");
					return time;
				}
			} catch (final NumberFormatException ignored) {}
		}

		return 0;
	}
	
	// Handles a kick, randomly walks around until the time is over.
	public static void handleKick(int time){
		System.out.println("Kick handler active for " + time + " minutes!");
		long endTime = System.currentTimeMillis() + General.randomRange((55000 * time), (65000 * time));
		
		while(System.currentTimeMillis() < endTime){
			long timeLeft = endTime - System.currentTimeMillis();
			System.out.println("Time left until kick is over: " + timeLeft);
			
			switch(General.randomRange(0, 9)){
				case 0: Walking.walkToRandomSpotInLobby(); break;
			}
			
			Task.sleep(1000, 3000);
		}
		
		System.out.println("Kick handler finished, we can join a team again");
	}
	
	// Attempts to join the team of choice
	public static boolean joinTeam(Wizard wizard){
		if(joinWizard(wizard)){
			return confirmJoin();
		}else{
			Walking.walkToWizard(wizard);
		}
		
		return false;
	}
}