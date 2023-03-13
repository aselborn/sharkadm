/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.utils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.StringTokenizer;


public class GeoPosition {

	private double latitude; // Unit = DD (Decimal Degree).
	
	private double longitude; // Unit = DD (Decimal Degree).
	
	public GeoPosition(double latitude, double longitude) {
		this.setLatitude(latitude);
		this.setLongitude(longitude);
	}

	public GeoPosition(String latitude, String longitude) {
		this.setLatitude(ConvUtils.convStringToDouble(latitude));
		this.setLongitude(ConvUtils.convStringToDouble(longitude));
	}

	public GeoPosition(String dbPoint) {
//		ErrorLogger.println("DbPoint: " + dbPoint);
		StringTokenizer tok = new StringTokenizer(dbPoint, "( )"); // Note: Three delimiters.
		tok.nextToken(); // Removes the string "POINT".
		this.setLatitude(new Double(tok.nextToken()));
		this.setLongitude(new Double(tok.nextToken()));		
	}

	public double getDistanceTo(GeoPosition pos2) {
		// Spherical law of cosines:
		// d = acos(sin(lat1) * sin(lat2) + cos(lat1) * cos(lat2) * cos(long2 - long1)) * R
		double lat1_rad = this.latitude * Math.PI / 180;
		double long1_rad = this.longitude * Math.PI / 180;
		double lat2_rad = pos2.latitude * Math.PI / 180;
		double long2_rad = pos2.longitude * Math.PI / 180;
		return Math.round(Math.acos(Math.sin(lat1_rad) * Math.sin(lat2_rad) + 
									Math.cos(lat1_rad) * Math.cos(lat2_rad) *
									Math.cos(long2_rad - long1_rad)) * 6371009.0);		
	}
	
	public String getDbPoint() {
		return "POINT(" + latitude + " " + longitude + ")";
	}


	public void setLatitude(double latitude) {
		// Reduce the number of significant digits. 
		BigDecimal bd = new BigDecimal(Double.toString(latitude));
//		bd = bd.round(new MathContext(6, RoundingMode.HALF_EVEN));
		bd = bd.round(new MathContext(8, RoundingMode.HALF_EVEN));
		this.latitude = bd.stripTrailingZeros().doubleValue();
//		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		// Reduce the number of significant digits. 
		BigDecimal bd = new BigDecimal(Double.toString(longitude));
//		bd = bd.round(new MathContext(6, RoundingMode.HALF_EVEN));
		bd = bd.round(new MathContext(8, RoundingMode.HALF_EVEN));
		this.longitude = bd.stripTrailingZeros().doubleValue();
//		this.longitude = longitude;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public String getLatitudeAsString() {
		DecimalFormat twoDecimalPlaces = new DecimalFormat("#0.00000");
		return twoDecimalPlaces.format(latitude).replace(",", ".");
	}

	public String getLongitudeAsString() {
		DecimalFormat twoDecimalPlaces = new DecimalFormat("#00.00000");
		return twoDecimalPlaces.format(longitude).replace(",", ".");
	}
	
	public static double convertFromBiomad(String value) {
		if (value.length() < 1) {
			return 0.0;
		}
		// Remove E or N if first character.
		if ((value.substring(0,1).equals("E")) ||
			(value.substring(0,1).equals("N"))) {
			value = value.substring(1);
		}
		
		int degmin = 0;		
		String newValue = value.replace(",", ".");
		newValue = newValue.replace(" ", "");
		int index = newValue.indexOf(".");
		if (index != -1) {
			degmin = Integer.parseInt(newValue.substring(0,index));
		} else {
			degmin = Integer.parseInt(newValue);			
		}
		double degree = Math.floor(degmin/100);		
		double minute = ConvUtils.convStringToDouble(newValue) - (degree * 100);
		
		return (double)degree + ((double)minute / 60);		
	}

	public static double convertFromDegMin(String degrees, String minutes) {
		if ((degrees.length() == 0) || (minutes.length() == 0)) {
			ErrorLogger.println("degrees or minutes missing. Degrees: " + degrees + " Minutes: " + minutes);			
			return 0.0;
		}
		
		// Remove E or N if first character.
		if ((degrees.substring(0,1).equals("E")) ||
			(degrees.substring(0,1).equals("N"))) {
			degrees = degrees.substring(1);
		}
		double deg = ConvUtils.convStringToDouble(degrees);
		double min = ConvUtils.convStringToDouble(minutes);
		
		if ((deg > 90.0) || (min > 60.0)) {
//			ErrorLogger.println("ERROR in position. Degrees: " + Double.toString(deg) + " Minutes: " + Double.toString(min));			
			// TODO: Try to fix it....
			min = min / 100;
			// TODO: Check again...
			if ((deg > 90.0) || (min > 60.0)) {
				ErrorLogger.println("ERROR in position. Degrees: " + degrees + " Minutes: " + minutes);			
				return 0;
			}
		}
		return (double)deg + ((double)min / 60);		
	}

	public static String convertToIces(double value) {
		if (value >= 0.0) {
			return	"+" + convertToDM(value); 
		} else {
			return	"-" + convertToDM(value); 
		}
	}

	public static String convertToDM(double value) {
		DecimalFormat degreeFormat = new DecimalFormat("00");
		DecimalFormat minuteFormat = new DecimalFormat("00.00");
		String signMarker = "";
		if (value < 0.0) {
			signMarker = "-";
			value -= 0.000083; // Round (= 0.005 min).
		} else {
			value += 0.000083; // Round (= 0.005 min).
		}
		int degrees = (int) Math.floor(Math.abs(value));
		double minutes = (Math.abs(value) - degrees) * 60;

		String degString = degreeFormat.format(degrees);
		String minString = minuteFormat.format(Math.floor(minutes*100)/100);

		String dmString = signMarker + degString + " " + minString;
		return dmString.replace(",", ".");
	}

	// Test cases.
	public static void main(String[] args) {
		ErrorLogger.println("From biomad to DD:");
		GeoPosition pos = new GeoPosition(convertFromBiomad("5815,8100"), convertFromBiomad("1128,6500"));
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		
		ErrorLogger.println("From biomad to DD:");
		pos = new GeoPosition(convertFromBiomad("5815,810055"), convertFromBiomad("1128,650055"));
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		
		ErrorLogger.println("From biomad to DD:");
		pos = new GeoPosition(convertFromBiomad("5815,81005555"), convertFromBiomad("1128,65005555"));
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		
		ErrorLogger.println("From biomad to DD:");
		pos = new GeoPosition(convertFromBiomad("5815,81"), convertFromBiomad("1128,65"));
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		
		ErrorLogger.println("From biomad to DD:");
		pos = new GeoPosition(convertFromBiomad("5845"), convertFromBiomad("930"));
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		
		ErrorLogger.println("From biomad to DD:");
		pos = new GeoPosition(convertFromBiomad("5815,81"), convertFromBiomad("928,65"));
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		
		ErrorLogger.println("From db (integer) to db:");
		pos = new GeoPosition("POINT(10 20)");
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		ErrorLogger.println("DbPoint: " + pos.getDbPoint());
		
		ErrorLogger.println("From db to db:");
		pos = new GeoPosition("POINT(10.23455 20.234567)");
		ErrorLogger.println("Latit: " + pos.getLatitude());
		ErrorLogger.println("Longi: " + pos.getLongitude());		
		ErrorLogger.println("DbPoint: " + pos.getDbPoint());
		
		ErrorLogger.println("From db to ices:");
		pos = new GeoPosition("POINT(8.9999 20.11111111111)");
		ErrorLogger.println("Latit: " + convertToIces(pos.getLatitude()));
		ErrorLogger.println("Longi: " + convertToIces(pos.getLongitude()));		
		
		ErrorLogger.println("To ices:");
		pos = new GeoPosition("POINT(8.99999 179.111111)");
		ErrorLogger.println("Latit: " + convertToIces(pos.getLatitude()));
		ErrorLogger.println("Longi: " + convertToIces(pos.getLongitude()));		
		
		ErrorLogger.println("From biomad to ices:");
		pos = new GeoPosition(convertFromBiomad("5721,7571"), convertFromBiomad("1802,7971"));
		ErrorLogger.println("Latit: " + convertToIces(pos.getLatitude()));
		ErrorLogger.println("Longi: " + convertToIces(pos.getLongitude()));		
		
		// TODO ERROR:  5705,0000 is converted to +18 04.999. 
		ErrorLogger.println("From biomad to ices:");
		pos = new GeoPosition(convertFromBiomad("5705,0000"), convertFromBiomad("1805,0000"));
		ErrorLogger.println("Latit: " + convertToIces(pos.getLatitude()));
		ErrorLogger.println("Longi: " + convertToIces(pos.getLongitude()));		
		
	}
	
}
