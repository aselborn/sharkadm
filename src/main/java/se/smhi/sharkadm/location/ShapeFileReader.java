/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2015 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.location;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTSFactoryFinder;
//import org.opengis.feature.Feature;
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.geometry.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import se.smhi.sharkadm.utils.ErrorLogger;

public class ShapeFileReader {

	public void addPropertyValuesToVisitLocations(String shapefilePath,
												  String shapefileName,
												  List<VisitLocation> visitLocationList) {

		File file = new File(shapefilePath + shapefileName);
		try {
			Map connect = new HashMap();
			connect.put("url", file.toURL());
			
			DataStore dataStore = DataStoreFinder.getDataStore(connect);
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];
			
			System.out.println("Reading shapefile " + typeName);
			ErrorLogger.println("Reading shapefile " + typeName);
			
			FeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureCollection collection = featureSource.getFeatures();
//			FeatureIterator iterator = collection.features();
			
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
			WKTReader reader = new WKTReader();
			
//			try {
//					
				FeatureIterator iterator = collection.features();
				Integer featureCounter = 0;
				while (iterator.hasNext()) {
					Feature feature = iterator.next();
					SimpleFeature simpleFeature = (SimpleFeature) feature;
					featureCounter += 1;
					
					Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
					
						for (VisitLocation visitLocation : visitLocationList) {
							// Different point definitions needed since referens system differ between layers.
							Point pointRt90 = (Point) reader.read(visitLocation.getRt90WktPointString());
							Point pointSweref99Tm = (Point) reader.read(visitLocation.getSweref99TmWktPointString());
							Point pointGrs80LatLong = (Point) reader.read(visitLocation.getGrs80LatLongWktPointString());
							
							if (shapefileName.equals("KOMMUNER_LAN.shp")) {	
								if (geom.contains(pointRt90)) {
//								if (pointRt90.isWithinDistance(geom, 0)) {
									visitLocation.setCounty(feature.getProperty("LANSNAMN").getValue().toString());
									visitLocation.setMunicipality(feature.getProperty("KOMMUNNAMN").getValue().toString());
									if (!feature.getProperty("LANSNAMN").getValue().toString().equals("Utanför gränser")) {
										// All geometries are valid, except 'Utanför gränser'.
										visitLocation.setEconomicZone("Svensk ekonomisk zon"); 
									}
								}
							}
							else if (shapefileName.equals("havsomr_y_2012_2.shp")) {
								if (geom.contains(pointSweref99Tm)) {
//								if (pointSweref99.isWithinDistance(geom, 0)) {
//									visitLocation.setWaterDistrict(convertVattendist(feature.getProperty("VATTENDIST").getValue().toString()));
									visitLocation.setWaterDistrict(convertVattendist(feature.getProperty("Vattendist").getValue().toString()));
									visitLocation.setWaterCategory(convertWaterCategoryType(feature.getProperty("OMRTYP").getValue().toString()));
								}
							}
							else if (shapefileName.equals("havdirtyper_2012_delatKattegatt.shp")) {
								if (geom.contains(pointSweref99Tm)) {
//								if (pointSweref99.isWithinDistance(geom, 0)) {
									visitLocation.setTypeArea(convertKustvattentyp(feature.getProperty("TYP_HAVDIR").getValue().toString()));								
									visitLocation.setSvarSeaAreaName(feature.getProperty("NAMN").getValue().toString());
									visitLocation.setSvarSeaAreaCode(feature.getProperty("HID").getValue().toString());
								}
							}
							else if (shapefileName.equals("KONVENTION.shp")) {
								if (geom.contains(pointRt90)) {
//								if (pointRt90.isWithinDistance(geom, 0)) {
									visitLocation.setHelcomOsparArea(feature.getProperty("KONVENTION").getValue().toString());
								}
							}		    		  
	
//							else if (shapefileName.equals("MSFD_areas_TM.shp")) {
//								if (geom.contains(pointSweref99Tm)) {
							else if (shapefileName.equals("MSFD_areas.shp")) {
								if (geom.contains(pointGrs80LatLong)) {
									visitLocation.setEconomicZone("Svensk ekonomisk zon"); // All geometries are valid.
									visitLocation.setSeaBasin(feature.getProperty("Name").getValue().toString());
								}
							}
						}
				}
				iterator.close();
								
//				// Some stations are located on land, but should be linked to the 
//				// nearest water area. Use parameter for 'isWithinDistance()'.
				if (shapefileName.equals("havdirtyper_2012_delatKattegatt.shp")) {
					// Second iteration with 50 meters distance.
					iterator = collection.features(); // Reset iterator.
					while (iterator.hasNext()) {
						Feature feature = iterator.next();
						SimpleFeature simpleFeature = (SimpleFeature) feature;
						Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
						
						for (VisitLocation visitLocation : visitLocationList) {
							if (visitLocation.getSvarSeaAreaName().equals("")) {
								Point pointSweref99 = (Point) reader.read(visitLocation.getSweref99TmWktPointString());
								if (pointSweref99.isWithinDistance(geom, 50)) { // Use distance. Unit: meter.
									visitLocation.setSvarSeaAreaName( 
											feature.getProperty("NAMN").getValue().toString());
									visitLocation.setSvarSeaAreaCode( 
											feature.getProperty("HID").getValue().toString());
									visitLocation.setWaterDistrict(
											convertVattendist(
												feature.getProperty("Vattendist").getValue().toString()));
									visitLocation.setTypeArea(
											convertKustvattentyp(
												feature.getProperty("TYP_HAVDIR").getValue().toString()));								
								
									System.out.println("DEBUG: Second check on havdirtyper_2012_delatKattegatt.shp (50 m). Matched on: " +
											visitLocation.getSvarSeaAreaName() +
											"   Position key: " + visitLocation.getLocationId());

								}
							}		    		  
						}
					}
					iterator.close();

					// Third iteration with 200 meters distance.
					iterator = collection.features(); // Reset iterator.
					while (iterator.hasNext()) {
						Feature feature = iterator.next();
						SimpleFeature simpleFeature = (SimpleFeature) feature;
						Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
						
						for (VisitLocation visitLocation : visitLocationList) {
							if (visitLocation.getSvarSeaAreaName().equals("")) {
								Point pointSweref99 = (Point) reader.read(visitLocation.getSweref99TmWktPointString());
								if (pointSweref99.isWithinDistance(geom, 200)) { // Use distance. Unit: meter.
									visitLocation.setSvarSeaAreaName( 
											feature.getProperty("NAMN").getValue().toString());
									visitLocation.setSvarSeaAreaCode( 
											feature.getProperty("HID").getValue().toString());
									visitLocation.setWaterDistrict(
											convertVattendist(
												feature.getProperty("Vattendist").getValue().toString()));
									visitLocation.setTypeArea(
											convertKustvattentyp(
												feature.getProperty("TYP_HAVDIR").getValue().toString()));								
								
									System.out.println("DEBUG: Third check on havdirtyper_2012_delatKattegatt.shp (200 m). Matched on: " +
											visitLocation.getSvarSeaAreaName() +
											"   Position key: " + visitLocation.getLocationId());
//									ErrorLogger.println("");

								}
							}		    		  
						}
					}
					iterator.close();
				}
				
//				// DEBUG:
//				if (shapefileName.equals("havdirtyper_2012_delatKattegatt.shp")) {
//					for (VisitLocation visitLocation : visitLocationList) {
//						if (visitLocation.getSvarSeaAreaName().equals("")) {
//							System.out.println("DEBUG: havdirtyper_2012_delatKattegatt.shp. Missing: " +
//									visitLocation.getPositionKey());
//						}
//					}
//				}

				System.out.println("Finished shapefile " + typeName + ".  Number of features: " + featureCounter.toString());
				ErrorLogger.println("Finished shapefile " + typeName + ".  Number of features: " + featureCounter.toString());
				
//			} finally {
//				iterator.close();
//			}
		} catch (Exception e) {
			System.out.println("Exception in ShapeFileReader: " + e.getMessage());
			ErrorLogger.println("Exception in ShapeFileReader: " + e.getMessage());
		}
	}
	
	public String convertWaterCategoryType(String waterCategoryType) {
				String descr = "";
				if (waterCategoryType.equals("1"))      descr = "1 - Estuarie (övergångsvatten)";
				else if (waterCategoryType.equals("2")) descr = "2 - Havsområde innanför 1 NM";
				else if (waterCategoryType.equals("3")) descr = "3 - Havsområde  mellan 1 NM och 12 NM";
				else if (waterCategoryType.equals("4")) descr = "4 - Utsjövatten";
				else if (waterCategoryType.equals("5")) descr = "5 - Öar med vattendelare";		
				else if (waterCategoryType.equals("6")) descr = "6 - Övriga öar";		

				return descr;
			}
				
	public String convertVattendist(String vattendist) {
		String descr = "";
		if (vattendist.equals("1")) descr = "Bottenvikens vattendistrikt";
		else if (vattendist.equals("2")) descr = "Bottenhavets vattendistrikt";
		else if (vattendist.equals("3")) descr = "Norra Östersjöns vattendistrikt";
		else if (vattendist.equals("4")) descr = "Södra Östersjöns vattendistrikt";
		else if (vattendist.equals("5")) descr = "Västerhavets vattendistrikt";		

		return descr;
		
		// 1 Bottenvikens vattendistrikt
		// 2 Bottenhavets vattendistrikt
		// 3 Norra Östersjöns vattendistrikt
		// 4 Södra Östersjöns vattendistrikt 
		// 5 Västerhavets vattendistrikt
	}
		
	public String convertKustvattentyp(String typ_nfs06) {
		String descr = "";
		if (typ_nfs06.equals("1"))        descr = "01 - Västkustens inre kustvatten";
		else if (typ_nfs06.equals("1s"))  descr = "01s - Västkustens inre kustvatten";
		else if (typ_nfs06.equals("1n"))  descr = "01n - Västkustens inre kustvatten";
		else if (typ_nfs06.equals("2"))   descr = "02 - Västkustens fjordar";
		else if (typ_nfs06.equals("3"))   descr = "03 - Västkustens yttre kustvatten. Skagerrak";
		else if (typ_nfs06.equals("4"))   descr = "04 - Västkustens yttre kustvatten. Kattegatt";
		else if (typ_nfs06.equals("5"))   descr = "05 - Södra Hallands och norra Öresunds kustvatten";
		else if (typ_nfs06.equals("6"))   descr = "06 - Öresunds kustvatten";
		else if (typ_nfs06.equals("7"))   descr = "07 - Skånes kustvatten";
		else if (typ_nfs06.equals("8"))   descr = "08 - Blekinge skärgård och Kalmarsund. Inre kustvatten";
		else if (typ_nfs06.equals("9"))   descr = "09 - Blekinge skärgård och Kalmarsund. Yttre kustvatten";
		else if (typ_nfs06.equals("10"))  descr = "10 - Ölands och Gotlands kustvatten";
		else if (typ_nfs06.equals("11"))  descr = "11 - Gotlands nordvästra kustvatten";
		else if (typ_nfs06.equals("12"))  descr = "12 - Östergötlands och Stockholms skärgård. Mellankustvatten";
		else if (typ_nfs06.equals("12n")) descr = "12n - Östergötlands och Stockholms skärgård. Mellankustvatten";
		else if (typ_nfs06.equals("12s")) descr = "12s - Östergötlands och Stockholms skärgård. Mellankustvatten";
		else if (typ_nfs06.equals("13"))  descr = "13 - Östergötlands inre kustvatten";
		else if (typ_nfs06.equals("14"))  descr = "14 - Östergötlands yttre kustvatten";
		else if (typ_nfs06.equals("15"))  descr = "15 - Stockholms skärgård. Yttre kustvatten";
		else if (typ_nfs06.equals("16"))  descr = "16 - Södra Bottenhavet. Inre kustvatten"; 
		else if (typ_nfs06.equals("17"))  descr = "17 - Södra Bottenhavet. Yttre kustvatten";
		else if (typ_nfs06.equals("18"))  descr = "18 - Norra Bottenhavet. Höga kusten. Inre kustvatten";
		else if (typ_nfs06.equals("19"))  descr = "19 - Norra Bottenhavet. Höga kusten. Yttre kustvatten";
		else if (typ_nfs06.equals("20"))  descr = "20 - Norra Kvarkens inre kustvatten";
		else if (typ_nfs06.equals("21"))  descr = "21 - Norra Kvarkens yttre kustvatten";
		else if (typ_nfs06.equals("22"))  descr = "22 - Norra Bottenviken. Inre kustvatten";
		else if (typ_nfs06.equals("23"))  descr = "23 - Norra Bottenviken. Yttre kustvatten";
		else if (typ_nfs06.equals("24"))  descr = "24 - Stockholms inre skärgård och Hallsfjärden";
		else if (typ_nfs06.equals("25"))  descr = "25 - Göta älvs- och Nordre älvs estuarie";
		
//		return typ_nfs06 + " - " + descr;
		return descr;

		// 1	Västkustens inre kustvatten
		// 2	Västkustens fjordar
		// 3	Västkustens yttre kustvatten. Skagerrak
		// 4	Västkustens yttre kustvatten. Kattegatt
		// 5	Södra Hallands och norra Öresunds kustvatten
		// 6	Öresunds kustvatten
		// 7	Skånes kustvatten
		// 8	Blekinge skärgård och Kalmarsund, inre kustvatten
		// 9	Blekinge skärgård och Kalmarsund, yttre kustvatten
		// 10	Ölands och Gotlands kustvatten
		// 11	Gotlands nordvästra kustvatten
		// 12	Östergötlands och Stockholms skärgård, mellankustvatten
		// 13	Östergötlands inre kustvatten
		// 14	Östergötlands yttre kustvatten
		// 15	Stockholms skärgård, yttre kustvatten
		// 16	Södra Bottenhavet, inre kustvatten 
		// 17	Södra Bottenhavet, yttre kustvatten
		// 18	Norra Bottenhavet, Höga kusten, inre kustvatten
		// 19	Norra Bottenhavet, Höga kusten, yttre kustvatten
		// 20	Norra Kvarkens inre kustvatten
		// 21	Norra Kvarkens yttre kustvatten
		// 22	Norra Bottenviken, inre kustvatten
		// 23	Norra Bottenviken, yttre kustvatten
		// 24	Stockholms inre skärgård och Hallsfjärden
		// 25	Göta älvs- och Nordre älvs estuarie
	}
	
    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile and displays its
     * contents on the screen in a map frame.
     */
    public static void main(String[] args) throws Exception {
    	
    	// TEST:
//		VisitLocationManager.instance().addVisitLocation(57.5, 18.5);
//		VisitLocationManager.instance().addVisitLocation(57.5, 17.5);
//		VisitLocationManager.instance().addVisitLocation(57.5, 16.5);
//		VisitLocationManager.instance().addVisitLocation(57.5, 15.5);
		VisitLocationManager.instance().addVisitLocation(56.94, 12.21167);

		VisitLocationManager.instance().readDataFromShapefiles();
		
//        // Display a data store file chooser dialog for shapefiles.
//        File file = JFileDataStoreChooser.showOpenFile("shp", new File("\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\sharkweb_shapefiles"), null);
//        if (file == null) {
//            return;
//        }
//        FileDataStore store = FileDataStoreFinder.getDataStore(file);
//        SimpleFeatureSource featureSource = store.getFeatureSource();
//        // Create a map content and add our shapefile to it.
//        MapContent map = new MapContent();
//        map.setTitle("Quickstart");        
//        Style style = SLD.createSimpleStyle(featureSource.getSchema());
//        Layer layer = new FeatureLayer(featureSource, style);
//        map.addLayer(layer);
//        // Now display the map.
//        JMapFrame.showMap(map);
    }
}
