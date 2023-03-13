package sharkadm.locations_TEST;





public class LocationManager {

	private static LocationManager instance = new LocationManager(); // Singleton.

	private LocationManager() { // Singleton.
	}

	public static LocationManager instance() { // Singleton.
		return instance;
	}

	public void clear() {
		
	}

	public void loadFiles() {
		System.out.println("LocationManager: loadFiles().");
	}

}
