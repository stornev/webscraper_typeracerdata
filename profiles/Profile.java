package profiles;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;

public abstract class Profile {
	private String platform;		// i.e. twitter, typeracer, etc.
	private String name;			// first last (@name)
	private String location;		// geograhic
	private String gender;
	private int age;
	private String bio;
	private ArrayList<URL> externalLinks;
	private LocalDate onSince;	// on platform since
	private URL profPicURL;
	
	/* Max amount to create a profile  */
	public Profile(String platform, String name, String location,
                   String gender, int age, String bio,
                   ArrayList<URL> links, LocalDate onSince, URL profPicURL) 
	{
		this.platform = platform;
		this.name = name;
		this.location = location;
		this.gender = gender;
		this.age = age;
		this.bio = bio;
		this.externalLinks = links;
		this.onSince = onSince;
		this.profPicURL = profPicURL;
	}
	
	/* Minimum amount to create a profile */
	public Profile(String name, String platform) {
		this.name = name;
		this.platform = platform;
	}
	
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void setGender(String gender) {
		gender = gender.trim();
		if (gender.toLowerCase().equals("male") || 
			gender.toLowerCase().equals("female") || 
			gender.toLowerCase().equals("other")) 
		{
			this.gender = gender;
		} else {
			System.out.println("Gender must be male, female, or other.");
		}
	}
	
	public void setAge(int age) {
		if (age < 125) {
			this.age = age;
		} else {
			System.out.println("Age must be within the range of possibility.");
		}
	}
	
	public void setBio(String bio) {
		this.bio = bio;
	}
	
	public void setExternalLinks(ArrayList<URL> links) {
		externalLinks = links;
	}
	
	public void setOnSince(LocalDate onSince) {
		int yearOfSince = onSince.getYear();
		int yearOfBirth = LocalDate.now().getYear() - (age + 1);
		if (yearOfSince > yearOfBirth) {
			this.onSince = onSince;
		} else {
			System.out.println("The year of onSince must be greater than your age of birth.");
		}
	}
	
	public void setProfPicURL(URL profPicURL) {
		this.profPicURL = profPicURL;
	}
	
	public String getPlatform() {
		return platform;
	}
	
	public String getName() {
		return name;
	}
	
	public String getLocation() {
		return location;
	}
	
	public String getGender() {
		return gender;
	}
	
	public int getAge() {
		return age;
	}
	
	public String getBio() {
		return bio;
	}
	
	public ArrayList<URL> getExternalLinks() {
		return externalLinks;
	}
	
	public LocalDate getOnSince() {
		return onSince;
	}
	
	public URL getProfPicURL() {
		return profPicURL;
	}
	
	/* implement your own to check if two profiles are the same */
	@Override
	public abstract boolean equals(Object other);
	
	/* string representation of a profile */
	@Override
	public abstract String toString();
}
