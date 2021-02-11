package extras;

import java.net.MalformedURLException;
import profiles.TypeRaceProfile;


public class Tester {
    public static void main(String[] args) throws MalformedURLException {
        // username, platform
        TypeRaceProfile prof = new TypeRaceProfile("test", "typeracer");
        // make excel file at dir
        prof.getScraper().outputToExcel("C:\\Users\\" + prof.getName() + ".xlsx");
    }
}
