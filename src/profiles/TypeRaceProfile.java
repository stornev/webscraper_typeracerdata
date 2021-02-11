package profiles;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;

import extras.TypeRace;
import webscrapers.TypeRaceScraper;

@SuppressWarnings("unused")
public class TypeRaceProfile extends Profile {
	
	private TypeRaceScraper scraper;
	private String wpmPercentile;
	private String skillLevel;
	private String expLevel;
	private String keyboardLayout;
	private String membership;
	private ArrayList<String> raceStats = new ArrayList<String>();
	private ArrayList<String> raceData = new ArrayList<String>();

	private static final DateTimeFormatter SHORT_STYLE;
	
	static {
		SHORT_STYLE = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	}
	
	public TypeRaceProfile(String name, String platform) throws MalformedURLException {
		super(name, platform);
		scraper = new TypeRaceScraper(this);
		scraper.updateRaces(this)
			   .getData();
	}
	
	public String getWpmPercentile() {
		return wpmPercentile;
	}

	public void setWpmPercentile(String wpmPercentile) {
		this.wpmPercentile = wpmPercentile;
	}

	public String getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(String skillLevel) {
		this.skillLevel = skillLevel;
	}

	public String getExpLevel() {
		return expLevel;
	}

	public void setExpLevel(String expLevel) {
		this.expLevel = expLevel;
	}

	public String getKeyboardLayout() {
		return keyboardLayout;
	}

	public void setKeyboardLayout(String keyboardLayout) {
		this.keyboardLayout = keyboardLayout;
	}

	public String getMembership() {
		return membership;
	}

	public void setMembership(String membership) {
		if (membership.equals("Basic") || membership.equals("Premium")) {
			this.membership = membership;
		}
	}

	public ArrayList<String> getRaceStats() {
		return raceStats;
	}

	public void setRaceStats(ArrayList<String> raceStats) {
		this.raceStats = raceStats;
	}
	
	public ArrayList<String> getRaceData() {
		return raceData;
	}

	public void setRaceData(ArrayList<String> raceData) {
		this.raceData = raceData;
	}
	
	public TypeRaceScraper getScraper() {
		return scraper;
	}

	@Override
	public boolean equals(Object other) {
		// TODO determine if one profile is another
		return false;
	}

	@Override
	public String toString() {
		
		String linesOfRaceStats = "";
		for (String n : raceStats) linesOfRaceStats += n + "\n";
		
		if (getExternalLinks() != null) {
			
			String linesOfLinks = "";
			for (URL x : getExternalLinks()) linesOfLinks += x.toString() + "\n";
			
			return getName() + " is on " + getPlatform() + ".\nProfile Info:\n"
                                     + "Gender: " + getGender() + "\nAge: " + getAge()
                                     + "\nFrom: " + getLocation() + "\nJoined On: "
                                     + getOnSince().format(SHORT_STYLE) + "\nBio: " + getBio()
                                     + "\nExternal Links:\n" + linesOfLinks + "\nRace Details:"
                                     + "\nWPM Percentile: " + wpmPercentile + "\nSkill Level: "
                                     + skillLevel + "\nExperience Level: " + expLevel
                                     + "\nKeyboard Layout: " + keyboardLayout + "\nMembership: "
                                     + membership + "\n\n" + linesOfRaceStats;
			
		}
		
		return getName() + " is on " + getPlatform() + ".\nProfile Info:\n" 
                                 + "Gender: " + getGender() + "\nAge: " + getAge() 
                                 + "\nFrom: " + getLocation() + "\nJoined On: " 
                                 + getOnSince().format(SHORT_STYLE) + "\nBio: " + getBio()
                                 + "\n\nRace Details:" + "\nWPM Percentile: " + wpmPercentile 
                                 + "\nSkill Level: " + skillLevel + "\nExperience Level: " 
                                 + expLevel + "\nKeyboard Layout: " + keyboardLayout 
                                 + "\nMembership: " + membership + "\n\n" + linesOfRaceStats;
	}
}
