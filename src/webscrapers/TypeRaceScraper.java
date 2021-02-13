package webscrapers;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import profiles.TypeRaceProfile;

@SuppressWarnings("unused")
public class TypeRaceScraper extends WebScraper {
	
	private final TypeRaceProfile thisProfile;
	private final Document profilePage;
	private final Document dataPage;
	private String toFileString;
	private static final String BASE_IMPORT_URI;
	private static final String BASE_PROF_URI;
	private static final String BASE_DATA_URI;
	private static final Map<String, Integer> monthsOfTheYear;
	
	/* Didn't want to make a switch case or endless if statements so map */
	static {
		BASE_IMPORT_URI = "http://typeracerdata.com/import?username=";
		BASE_PROF_URI = "https://data.typeracer.com/pit/profile?user=";
		BASE_DATA_URI = "http://typeracerdata.com/profile?username=";
		monthsOfTheYear = Stream.of(new Object[][] { { "Jan.", 1 }, { "Feb.", 2 }, 
					   { "March", 3 }, { "Apr.", 4 }, { "May", 5 }, { "June", 6 }, 
					   { "July", 7 }, { "Aug.", 8 }, { "Sept.", 9 }, { "Oct.", 10 }, 
					   { "Nov.", 11 }, { "Dec.", 12 } 
					   }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
	}
	

	/* need the profile to scrape and need the docs for the pages */
	public TypeRaceScraper(TypeRaceProfile n) throws MalformedURLException {
		thisProfile = n;
		profilePage = grabDocFromURL(new URL(BASE_PROF_URI + thisProfile.getName()));
		dataPage = grabDocFromURL(new URL(BASE_DATA_URI + thisProfile.getName()));
		
		if (profilePage.title().equals("Profile Not Found (TypeRacer Pit Stop)") ||
			profilePage.title().equals("TypeRacer Pit Stop")) 
		{
			System.out.println("account not found");
			System.exit(1);
			
			/* If the profile exists but not on typeracerdata.com, import it */
		} else if (dataPage.title().equals("TypeRacer Data - Profiles - Account not found")) {
			updateRaces(thisProfile);
		}
	}
	
	/* typeracerdata.com doesn't auto update profiles from typeracer */
	public TypeRaceScraper updateRaces(TypeRaceProfile profile) {
		try {
			Jsoup.connect(BASE_IMPORT_URI + profile.getName()).get();
		} catch (Exception e) { e.printStackTrace(); }
		return this;
	}

	/* how the profile gets basically all of its data */
	@Override
	public WebScraper getData() {
		getUserData();
		setUserData();
		setData(new HashMap<String, String>());
		getRaceData();
		setRaceData();
		return this;
	}
	
	public void setRaceData() {
		
		HashMap<String, String> data = getScrapedData();
		ArrayList<String> raceStats = thisProfile.getRaceStats();
		
		raceStats.add("Total Races: " + data.get("Total Races"));
		raceStats.add("Best Last 10 Races: " + data.get("Best Last 10 Races"));
		raceStats.add("Best Race: " + data.get("Best Race"));
		raceStats.add("Average Of Fastest 10: " + data.get("Average Of Fastest 10"));
		raceStats.add("Average of Fastest Races of All Texts: " + data.get("Average of Fastest Races of All Texts"));
		raceStats.add("Wins: " + data.get("Wins"));
		raceStats.add("Points: " + data.get("Points"));
		raceStats.add("Average Career Speed: " + data.get("Average Career Speed"));
		raceStats.add("Overall Accuracy: " + data.get("Overall Accuracy"));
		raceStats.add("Average Speed of all 100% Accuracy Races: " + data.get("Average Speed of all 100% Accuracy Races"));
		raceStats.add("Career Standard Deviation: " + data.get("Career Standard Deviation"));
		raceStats.add("Coefficient of Variation: " + data.get("Coefficient of Variation"));
		raceStats.add("Top Marathon: " + data.get("Top Marathon"));
		
		Document allRacesPage = null;
		int totalRaces = Integer.parseInt(data.get("Total Races").replace(",", ""));
		try {
			// data.get("Total Races").replace(",", "")
			allRacesPage = grabDocFromURL(new URL(BASE_DATA_URI + thisProfile.getName() + "&last=" + totalRaces));
		} catch (MalformedURLException e) { e.printStackTrace(); }
		
		/* Collects all links from the page that contains all the races & separates the universe and race links */
		Elements links = allRacesPage.select("table.profile tbody tr td a");
		ArrayList<String> everylink = (ArrayList<String>) links.eachAttr("abs:href");
		ArrayList<String> universes = new ArrayList<String>();
		ArrayList<String> raceLinks = new ArrayList<String>();
		String linksString = "";
		
		for (String n : everylink) {
			if (!n.contains("months?") && !n.contains("&universe=")) {
				linksString += n + "\n";
				raceLinks.add(n);
				
			} else if (n.contains("&universe=")) {
				universes.add(n);
			}
		}
		
		/* groups the typeracerdata.com links and data.typeracer.com links together for every race */
		ArrayList<String> groupedLinks = new ArrayList<String>();
		for (int i = 0; i < raceLinks.size(); i+=2) {
			groupedLinks.add(raceLinks.get(i) + "\n" + raceLinks.get(i+1));
		}
		
		ArrayList<String> sortedLinks = new ArrayList<String>();
		for (int i = groupedLinks.size() - 1; i >= 0; i--) {
			sortedLinks.add(groupedLinks.get(i));
		}
		
		/* Adjusts the html to only start where the race table starts and ends */
		Elements all = allRacesPage.select("table.profile tbody tr th, td");
		String allString = all.toString();
		String allRaces = allString.substring(allString.indexOf("<th>Race</th>"));
		allRaces = allRaces.substring(allRaces.indexOf("<td>" + totalRaces + ".</td>"), allRaces.indexOf("<th>Universe</th>"));
		
		ArrayList<String> raceDetails = (ArrayList<String>) all.eachText();
		ArrayList<String> temp = new ArrayList<String>();
		for (String n : raceDetails) {
			if (n.equals("Points")) {
				temp = new ArrayList<String>();
			} else if (n.equals("Universe")){
				break;
			} else {
				temp.add(n);
			}
		}
		raceDetails = temp;
		
		/* puts race data in a manageable format */
		temp = new ArrayList<String>();
		for (int i = 0; i < raceDetails.size(); i+=7) {
			String race = raceDetails.get(i) 
					+ "|" + raceDetails.get(i+1)
					+ "|" + raceDetails.get(i+2)
					+ "|" + raceDetails.get(i+4)
					+ "|" + raceDetails.get(i+5)
					+ "|" + raceDetails.get(i+6);
			temp.add(race);
		}
		raceDetails = temp;
		
		thisProfile.setRaceData(raceDetails);
		
		String out = "";
		for (String n : raceDetails) {
			out += n + "\n";
		}
		
		toFileString = out;
		outputToFile("C:\\Users\\(name)\Desktop\\something.txt");
	}

	public void setUserData() {
		
		HashMap<String, String> data = getScrapedData();
		
		thisProfile.setLocation(data.get("Location"));
		thisProfile.setGender(data.get("Gender"));
		thisProfile.setAge(Integer.parseInt(data.get("Age")));
		
		try {
			thisProfile.setProfPicURL(new URL(data.get("Profile Picture URL")));
		} catch (MalformedURLException e) { e.printStackTrace(); }
		
		String date = data.get("Racing Since");
		int month = monthsOfTheYear.get(date.substring(0, date.indexOf(" ")).trim());
		int day = Integer.parseInt(date.substring(date.indexOf(" "), date.indexOf(",")).trim());
		int year = Integer.parseInt(date.substring(date.indexOf(", ")+2));
		thisProfile.setOnSince(LocalDate.of(year, month, day));
		
		String bio = data.get("Bio");
		if (bio != null) {
			ArrayList<URL> links = new ArrayList<URL>();
			while (bio.contains("https://")) {
				String link = bio.substring(bio.indexOf("https://"));
				if (link.indexOf(" ") != -1) {
					link = link.substring(0, link.indexOf(" ")).trim();
				}
				
				URL url = null;
				try {
					url = new URL(link);
				} catch (MalformedURLException e) { e.printStackTrace(); }
				
				links.add(url);
				
				bio = bio.replace(link, "").trim();
			}
			thisProfile.setBio(bio);
			thisProfile.setExternalLinks(links);
		}
		
		thisProfile.setWpmPercentile(data.get("WPM Percentile"));
		thisProfile.setSkillLevel(data.get("Skill Level"));
		thisProfile.setExpLevel(data.get("Experience Level"));
		thisProfile.setKeyboardLayout(data.get("Keyboard Layout"));
		thisProfile.setMembership(data.get("Membership"));
	}
	
	public void getRaceData() {
		List<String> profileRaceData = dataPage.selectFirst("table.profile").getElementsByTag("tr").eachText();
		HashMap<String, String> data = getScrapedData();
		
		String temp1 = profileRaceData.get(0);
		String totalRaces = temp1.substring(temp1.indexOf(" ") + 1).trim();
		data.put("Total Races", totalRaces);
		
		String temp2 = profileRaceData.get(1);
		String bestOfLastTen = temp2.substring(temp2.indexOf("races") + 5).trim();
		data.put("Best Last 10 Races", bestOfLastTen);
		
		String temp3 = profileRaceData.get(2);
		String bestSingle = temp3.substring(temp3.indexOf("race") + 4).trim();
		data.put("Best Race", bestSingle);
		
		String temp4 = profileRaceData.get(3);
		String avgOfFastestTen = temp4.substring(temp4.indexOf("races") + 5).trim();
		data.put("Average Of Fastest 10", avgOfFastestTen);
		
		String temp5 = profileRaceData.get(4);
		String fastOfEachTextAvg = temp5.substring(temp5.indexOf("average") + 7).trim();
		data.put("Average of Fastest Races of All Texts", fastOfEachTextAvg);
		
		String temp6 = profileRaceData.get(5);
		String totalWins = temp6.substring(temp6.indexOf("Wins") + 4).trim();
		data.put("Wins", totalWins);
		
		String temp7 = profileRaceData.get(6);
		String totalPoints = temp7.substring(temp7.indexOf("s") + 2).trim();
		data.put("Points", totalPoints);
		
		String temp8 = profileRaceData.get(7);
		String averageCareerSpeed = temp8.substring(temp8.indexOf("speed") + 5).trim();
		data.put("Average Career Speed", averageCareerSpeed);
		
		String temp9 = profileRaceData.get(8);
		String accuracy = temp9.substring(temp9.indexOf("acy") + 3).trim();
		data.put("Overall Accuracy", accuracy);
		
		String temp10 = profileRaceData.get(9);
		String avgSpeedOfAll100Races = temp10.substring(temp10.indexOf("races") + 5).trim();
		data.put("Average Speed of all 100% Accuracy Races", avgSpeedOfAll100Races);
		
		String temp11 = profileRaceData.get(10);
		String standardDeviation = temp11.substring(temp11.indexOf("deviation") + 9).trim();
		data.put("Career Standard Deviation", standardDeviation);
		
		String temp12 = profileRaceData.get(11);
		String variation = temp12.substring(temp12.indexOf("variation") + 9).trim();
		data.put("Coefficient of Variation", variation);
		
		String temp13 = profileRaceData.get(12);
		String marathon = temp13.substring(temp13.indexOf("marathon") + 8).trim();
		data.put("Top Marathon", marathon);
		
		setData(data);
	}
	
	public void getUserData() {
		List<String> profileDetailsTable = profilePage.select("table.profileDetailsTable table tr").eachText();
		HashMap<String,String> data = getScrapedData();
		
		
		for (String n : profileDetailsTable) {
			if (n.contains("WPM Percentile")) {
				String percentile = n.replace("WPM Percentile ", "").trim();
				data.put("WPM Percentile", percentile);
			}
			if (n.contains("Skill Level")) {
				String skill = n.replace("Skill Level ", "").trim();
				data.put("Skill Level", skill);
			}
			if (n.contains("Experience Level")) {
				String level = n.replace("Experience Level ", "").trim();
				data.put("Experience Level", level);
			}
			if (n.contains("Racing Since")) {
				String since = n.replace("Racing Since ", "").trim();
				data.put("Racing Since", since);
			}
			if (n.contains("Keyboard")) {
				String kbl = n.replace("Keyboard ", "").trim();
				data.put("Keyboard Layout", kbl);
			}
			if (n.contains("Membership")) {
				String membership = n.replace("Membership ", "").trim();
				data.put("Membership", membership);
			}
		}
		
		List<String> profilePersonalInfoTable = profilePage.select("table.personalInfoTable tr").eachText();
		for (String n : profilePersonalInfoTable) {
			if (n.contains("Age")) {
				String age = n.replace("Age ", "").trim();
				data.put("Age", age);
			}
			if (n.contains("Gender")) {
				String gender = n.replace("Gender ", "").trim();
				data.put("Gender", gender);
			}
			if (n.contains("Location")) {
				String location = n.replace("Location ", "").trim();
				data.put("Location", location);
			}
			if (n.contains("About Me")) {
				String bio = n.replace("About Me ", "").trim();
				data.put("Bio", bio);
			}
		}
		
		String pic = profilePage.selectFirst("div.profileDetailsTable__avatar img[src]").toString();
		pic = "https://data.typeracer.com" + pic.substring(pic.indexOf("\"")+1, pic.indexOf("\" title"));
		data.put("Profile Picture URL", pic);
		
		setData(data);
		
	}
  
  /* This isn't the best... it kinda doesn't work (I think the typing on excel can be changed) */
	public void outputToExcel(String dir) {
		
		@SuppressWarnings("resource")
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sp = wb.createSheet(thisProfile.getName() + "'s Race Data");
		int totalRaces =  Integer.parseInt(thisProfile.getRaceStats().get(0).replace(",", "").replace("Total Races: ", ""));
		
		for (int i = 0; i < totalRaces; i++) {
			Row row = sp.createRow(i);
			Cell raceNum = row.createCell(0);
			Cell dateAndTime = row.createCell(1);
			Cell WPM = row.createCell(2);
			Cell placeInRace = row.createCell(3);
			Cell accuracy = row.createCell(4);
			Cell points = row.createCell(5);
			
			ArrayList<Cell> cells = new ArrayList<>(Arrays.asList(raceNum, dateAndTime, WPM, placeInRace, accuracy, points));
			String race = thisProfile.getRaceData().get(i);
			
			String nStr = "";
			for (Cell n : cells) {
				if (n == points) {
					race = race.substring(race.indexOf("|")+1);
					n.setCellValue(race);
				}
				else if (race.contains("|")) {
					nStr = race.substring(0,race.indexOf("|"));
					race = race.substring(race.indexOf("|")+1);
					n.setCellValue(nStr);
				}
			}
		}
		
		// Row test = sp.createRow(5000);
		// Cell test1 = test.createCell(0);
		// test1.setCellValue(0);
		
		try {
			OutputStream out = new FileOutputStream(dir);
			wb.write(out);
			System.out.println("Made excel file at directory... " + dir);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	/* if want to use the data in external resources 
	 * This is really only programming side right now it just puts what I want */
	@Override
	public void outputToFile(String dir) {
		
		try {
			File out = new File(dir);
			FileWriter fw = new FileWriter(out);
			out.createNewFile();
			fw.write(toFileString);
			fw.close();
			
		} catch (IOException e) {
			System.out.println("Things went boom...");
			e.printStackTrace();
		}
	}
}
