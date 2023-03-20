/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.datasets.calc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.*;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.Point;
//import com.vividsolutions.jts.io.WKTReader;

import se.smhi.sharkadm.utils.ErrorLogger;
import se.smhi.sharkadm.utils.GeodesiSwedishGrids;

public class BqiShapefileReader {

	private static BqiShapefileReader instance = new BqiShapefileReader(); // Singleton.
	
	private FeatureCollection collection = null;
	private WKTReader reader = null;
	
	private BqiShapefileReader() { // Singleton.
		
	}
	
	public static BqiShapefileReader instance() { // Singleton.
		return instance;
	}
	
	public void clearAll() {
		this.collection = null;
		this.reader = null;
	}
	
	// ===== NEW Shapefiles =====	
	
	public String getWestEastFromShapeFile(Double latitude, Double longitude) {
		// From latlong to SWEREF99TM.
		GeodesiSwedishGrids sweref99tm = new GeodesiSwedishGrids();
		double[] n_e = sweref99tm.geodetic_to_grid(latitude, longitude);
		double Sweref99TMLatitude = n_e[0];
		double Sweref99TMLongitude = n_e[1];
		// Well-known-text format 
		String mWktPointString = "POINT (" + Sweref99TMLongitude + " " + Sweref99TMLatitude + ")"; // Note order: long/lat.
		// Result.
//		String typ_nfs06 = "";
		String type_area = "";
		String basin = "";
		String westEast = "";		
		// Load shape file if not loaded before.
		if (this.collection == null) {
//			String shapefilePath = "D:\\\\arnold\\2_sharkadm\\w_sharkadm\\p_sharkadm\\TEST_SHARK_CONFIG\\sharkweb_shapefiles\\";
//			String shapefileName = "havdirtyper_2012_delatKattegatt.shp";
			String shapefilePath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\sharkweb_shapefiles\\";
			String shapefileName = "Havsomr_SVAR_2016_3c_CP1252.shp";

			File file = new File(shapefilePath + shapefileName);

			try {
				//Map connect = new HashMap();
				Map<String, Object> connect = new HashMap<>();

				//connect.put("url", file.toURL());
				connect.put("url", file.toPath());

				FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);

				//DataStore dataStore = DataStoreFinder.getDataStore(connect);
				String[] typeNames = dataStore.getTypeNames();
				String typeName = typeNames[0];
				
				FeatureSource featureSource = dataStore.getFeatureSource(typeName);
				this.collection = featureSource.getFeatures();
	//			FeatureIterator iterator = collection.features();
				
				//GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
				this.reader = new WKTReader(geometryFactory);

				//GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
				//this.reader = new WKTReader();
				
			} catch (Exception e) {
				System.out.println("Exception in ShapeFileReader: " + e.getMessage());
				ErrorLogger.println("Exception in ShapeFileReader: " + e.getMessage());
			}
		}
		// Search for matching feature.
		try {
			Point pointSweref99Tm = (Point) this.reader.read(mWktPointString);
			FeatureIterator iterator = this.collection.features();
			while (iterator.hasNext()) {
				Feature feature = iterator.next();
				SimpleFeature simpleFeature = (SimpleFeature) feature;
				Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();

				if (geom.contains(pointSweref99Tm)) {
//					typ_nfs06 = feature.getProperty("TYP_HAVDIR").getValue().toString();
					
					type_area = feature.getProperty("TYPOMR_KOD").getValue().toString();								
					basin = feature.getProperty("BASIN_NR").getValue().toString();
					
					break;
				}
			}
			iterator.close();
		} catch (Exception e) {
			System.out.println("BQIm Exception in ShapeFileReader: " + e.getMessage());
			ErrorLogger.println("BQIm Exception in ShapeFileReader: " + e.getMessage());
		}
		// Convert to WEST/EAST text.		
		if      (type_area.equals("1"))   westEast = "WEST"; // "01 - V�stkustens inre kustvatten";
		else if (type_area.equals("1s"))  westEast = "WEST"; // "01s - V�stkustens inre kustvatten";
		else if (type_area.equals("1n"))  westEast = "WEST"; // "01n - V�stkustens inre kustvatten";
		else if (type_area.equals("2"))   westEast = "WEST"; // "02 - V�stkustens fjordar";
		else if (type_area.equals("3"))   westEast = "WEST"; // "03 - V�stkustens yttre kustvatten. Skagerrak";
		else if (type_area.equals("4"))   westEast = "WEST"; // "04 - V�stkustens yttre kustvatten. Kattegatt";
		else if (type_area.equals("5"))   westEast = "WEST"; // "05 - S�dra Hallands och norra �resunds kustvatten";
		else if (type_area.equals("6"))   westEast = "WEST"; // "06 - �resunds kustvatten";
		else if (type_area.equals("7"))   westEast = "EAST"; // "07 - Sk�nes kustvatten";
		else if (type_area.equals("8"))   westEast = "EAST"; // "08 - Blekinge sk�rg�rd och Kalmarsund. Inre kustvatten";
		else if (type_area.equals("9"))   westEast = "EAST"; // "09 - Blekinge sk�rg�rd och Kalmarsund. Yttre kustvatten";
		else if (type_area.equals("10"))  westEast = "EAST"; // "10 - �lands och Gotlands kustvatten";
		else if (type_area.equals("11"))  westEast = "EAST"; // "11 - Gotlands nordv�stra kustvatten";
		else if (type_area.equals("12"))  westEast = "EAST"; // "12 - �sterg�tlands och Stockholms sk�rg�rd. Mellankustvatten";
		else if (type_area.equals("12n")) westEast = "EAST"; // "12n - �sterg�tlands och Stockholms sk�rg�rd. Mellankustvatten";
		else if (type_area.equals("12s")) westEast = "EAST"; // "12s - �sterg�tlands och Stockholms sk�rg�rd. Mellankustvatten";
		else if (type_area.equals("13"))  westEast = "EAST"; // "13 - �sterg�tlands inre kustvatten";
		else if (type_area.equals("14"))  westEast = "EAST"; // "14 - �sterg�tlands yttre kustvatten";
		else if (type_area.equals("15"))  westEast = "EAST"; // "15 - Stockholms sk�rg�rd. Yttre kustvatten";
		else if (type_area.equals("16"))  westEast = "EAST"; // "16 - S�dra Bottenhavet. Inre kustvatten"; 
		else if (type_area.equals("17"))  westEast = "EAST"; // "17 - S�dra Bottenhavet. Yttre kustvatten";
		else if (type_area.equals("18"))  westEast = "EAST"; // "18 - Norra Bottenhavet. H�ga kusten. Inre kustvatten";
		else if (type_area.equals("19"))  westEast = "EAST"; // "19 - Norra Bottenhavet. H�ga kusten. Yttre kustvatten";
		else if (type_area.equals("20"))  westEast = "EAST"; // "20 - Norra Kvarkens inre kustvatten";
		else if (type_area.equals("21"))  westEast = "EAST"; // "21 - Norra Kvarkens yttre kustvatten";
		else if (type_area.equals("22"))  westEast = "EAST"; // "22 - Norra Bottenviken. Inre kustvatten";
		else if (type_area.equals("23"))  westEast = "EAST"; // "23 - Norra Bottenviken. Yttre kustvatten";
		else if (type_area.equals("24"))  westEast = "EAST"; // "24 - Stockholms inre sk�rg�rd och Hallsfj�rden";
		else if (type_area.equals("25"))  westEast = "WEST"; // "25 - G�ta �lvs- och Nordre �lvs estuarie";

		else if (basin.equals("1")) westEast = "EAST"; // "01 - Bottenviken";
		else if (basin.equals("2")) westEast = "EAST"; // "02 - Norra Kvarken";
		else if (basin.equals("3")) westEast = "EAST"; // "03 - Bottenhavet";
		else if (basin.equals("4")) westEast = "EAST"; // "04 - �lands hav";
		else if (basin.equals("5")) westEast = "EAST"; // "05 - Sk�rg�rdshavet";		
		else if (basin.equals("6")) westEast = "EAST"; // "06 - Finska viken";		
		else if (basin.equals("7")) westEast = "EAST"; // "07 - Norra Gotlandshavet";		
		else if (basin.equals("8")) westEast = "EAST"; // "08 - V�stra Gotlandshavet";		
		else if (basin.equals("9")) westEast = "EAST"; // "09 - �stra Gotlandshavet";		
		else if (basin.equals("10")) westEast = "EAST"; // "10 - Rigabukten";		
		else if (basin.equals("11")) westEast = "EAST"; // "11 - Gdanskbukten";		
		else if (basin.equals("12")) westEast = "NOT-USED"; // "12 - Bornholmshavet och Han�bukten";		
		else if (basin.equals("13")) westEast = "NOT-USED"; // "13 - Arkonahavet och S�dra �resund";		
		else if (basin.equals("14")) westEast = "EAST"; // "14 - B�lthavet";		
		else if (basin.equals("15")) westEast = "WEST"; // "15 - �resund";		
		else if (basin.equals("16")) westEast = "WEST"; // "16 - Kattegatt";		
		else if (basin.equals("17")) westEast = "WEST"; // "17 - Skagerrak";		

		else {
			System.out.println("BQIm: WestEast NOT FOUND. Lat: " + latitude.toString() + " Long: " + longitude.toString());
			ErrorLogger.println("BQIm: WestEast NOT FOUND. Lat: " + latitude.toString() + " Long: " + longitude.toString());
		}
//		System.out.println("DEBUG: westEast: " + westEast);

		return westEast;
	}

	
// ===== OLD Shapefiles =====	
	
//	public String getWestEastFromShapeFile(Double latitude, Double longitude) {
//		// From latlong to SWEREF99TM.
//		GeodesiSwedishGrids sweref99tm = new GeodesiSwedishGrids();
//		double[] n_e = sweref99tm.geodetic_to_grid(latitude, longitude);
//		double Sweref99TMLatitude = n_e[0];
//		double Sweref99TMLongitude = n_e[1];
//		// Well-known-text format 
//		String mWktPointString = "POINT (" + Sweref99TMLongitude + " " + Sweref99TMLatitude + ")"; // Note order: long/lat.
//		// Result.
//		String typ_nfs06 = "";
//		String westEast = "";		
//		// Load shape file if not loaded before.
//		if (this.collection == null) {
//			String shapefilePath = "\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\sharkweb_shapefiles\\";
//			String shapefileName = "havdirtyper_2012_delatKattegatt.shp";
//			File file = new File(shapefilePath + shapefileName);
//			try {
//				Map connect = new HashMap();
//				connect.put("url", file.toURL());
//				
//				DataStore dataStore = DataStoreFinder.getDataStore(connect);
//				String[] typeNames = dataStore.getTypeNames();
//				String typeName = typeNames[0];
//				
//				FeatureSource featureSource = dataStore.getFeatureSource(typeName);
//				this.collection = featureSource.getFeatures();
//	//			FeatureIterator iterator = collection.features();
//				
//				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
//				this.reader = new WKTReader(geometryFactory);
//				
//			} catch (Exception e) {
//				System.out.println("Exception in ShapeFileReader: " + e.getMessage());
//				ErrorLogger.println("Exception in ShapeFileReader: " + e.getMessage());
//			}
//		}
//		// Search for matching feature.
//		try {
//			Point pointSweref99Tm = (Point) this.reader.read(mWktPointString);
//			FeatureIterator iterator = this.collection.features();
//			while (iterator.hasNext()) {
//				Feature feature = iterator.next();
//				SimpleFeature simpleFeature = (SimpleFeature) feature;
//				Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();				
//				if (geom.contains(pointSweref99Tm)) {
//					typ_nfs06 = feature.getProperty("TYP_HAVDIR").getValue().toString();
//					break;
//				}
//			}
//			iterator.close();
//		} catch (Exception e) {
//			System.out.println("Exception in ShapeFileReader: " + e.getMessage());
//			ErrorLogger.println("Exception in ShapeFileReader: " + e.getMessage());
//		}
//		// Convert to WEST/EAST text.		
//		if      (typ_nfs06.equals("1"))   westEast = "WEST"; // "01 - V�stkustens inre kustvatten";
//		else if (typ_nfs06.equals("1s"))  westEast = "WEST"; // "01s - V�stkustens inre kustvatten";
//		else if (typ_nfs06.equals("1n"))  westEast = "WEST"; // "01n - V�stkustens inre kustvatten";
//		else if (typ_nfs06.equals("2"))   westEast = "WEST"; // "02 - V�stkustens fjordar";
//		else if (typ_nfs06.equals("3"))   westEast = "WEST"; // "03 - V�stkustens yttre kustvatten. Skagerrak";
//		else if (typ_nfs06.equals("4"))   westEast = "WEST"; // "04 - V�stkustens yttre kustvatten. Kattegatt";
//		else if (typ_nfs06.equals("5"))   westEast = "WEST"; // "05 - S�dra Hallands och norra �resunds kustvatten";
//		else if (typ_nfs06.equals("6"))   westEast = "WEST"; // "06 - �resunds kustvatten";
//		else if (typ_nfs06.equals("7"))   westEast = "EAST"; // "07 - Sk�nes kustvatten";
//		else if (typ_nfs06.equals("8"))   westEast = "EAST"; // "08 - Blekinge sk�rg�rd och Kalmarsund. Inre kustvatten";
//		else if (typ_nfs06.equals("9"))   westEast = "EAST"; // "09 - Blekinge sk�rg�rd och Kalmarsund. Yttre kustvatten";
//		else if (typ_nfs06.equals("10"))  westEast = "EAST"; // "10 - �lands och Gotlands kustvatten";
//		else if (typ_nfs06.equals("11"))  westEast = "EAST"; // "11 - Gotlands nordv�stra kustvatten";
//		else if (typ_nfs06.equals("12"))  westEast = "EAST"; // "12 - �sterg�tlands och Stockholms sk�rg�rd. Mellankustvatten";
//		else if (typ_nfs06.equals("12n")) westEast = "EAST"; // "12n - �sterg�tlands och Stockholms sk�rg�rd. Mellankustvatten";
//		else if (typ_nfs06.equals("12s")) westEast = "EAST"; // "12s - �sterg�tlands och Stockholms sk�rg�rd. Mellankustvatten";
//		else if (typ_nfs06.equals("13"))  westEast = "EAST"; // "13 - �sterg�tlands inre kustvatten";
//		else if (typ_nfs06.equals("14"))  westEast = "EAST"; // "14 - �sterg�tlands yttre kustvatten";
//		else if (typ_nfs06.equals("15"))  westEast = "EAST"; // "15 - Stockholms sk�rg�rd. Yttre kustvatten";
//		else if (typ_nfs06.equals("16"))  westEast = "EAST"; // "16 - S�dra Bottenhavet. Inre kustvatten"; 
//		else if (typ_nfs06.equals("17"))  westEast = "EAST"; // "17 - S�dra Bottenhavet. Yttre kustvatten";
//		else if (typ_nfs06.equals("18"))  westEast = "EAST"; // "18 - Norra Bottenhavet. H�ga kusten. Inre kustvatten";
//		else if (typ_nfs06.equals("19"))  westEast = "EAST"; // "19 - Norra Bottenhavet. H�ga kusten. Yttre kustvatten";
//		else if (typ_nfs06.equals("20"))  westEast = "EAST"; // "20 - Norra Kvarkens inre kustvatten";
//		else if (typ_nfs06.equals("21"))  westEast = "EAST"; // "21 - Norra Kvarkens yttre kustvatten";
//		else if (typ_nfs06.equals("22"))  westEast = "EAST"; // "22 - Norra Bottenviken. Inre kustvatten";
//		else if (typ_nfs06.equals("23"))  westEast = "EAST"; // "23 - Norra Bottenviken. Yttre kustvatten";
//		else if (typ_nfs06.equals("24"))  westEast = "EAST"; // "24 - Stockholms inre sk�rg�rd och Hallsfj�rden";
//		else if (typ_nfs06.equals("25"))  westEast = "WEST"; // "25 - G�ta �lvs- och Nordre �lvs estuarie";
//		
//		return westEast;
//	}
	

//	// TEST
//	public static void main(String[] args) throws Exception {
//		String westEast = "";
//		westEast = BqiShapefileReader.instance().getWestEastFromShapeFile(65.718058, 22.507482);
//		System.out.println("Test: " + westEast);
//		westEast = BqiShapefileReader.instance().getWestEastFromShapeFile(65.71, 22.50);
//		System.out.println("Test: " + westEast);
//		westEast = BqiShapefileReader.instance().getWestEastFromShapeFile(56.524275, 12.688346);
//		System.out.println("Test: " + westEast);
//		BqiShapefileReader.instance().clearAll();
//    }
}
