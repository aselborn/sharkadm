/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
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
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import se.smhi.sharkadm.utils.ErrorLogger;

public class ShapeFileReader_TEST {

	public void addPropertyValuesToVisitLocations(String shapefilePath,
												  String shapefileName,
												  List<VisitLocation_TEST> visitLocationList) {

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
			
			try {
				
				FeatureIterator iterator = collection.features();
				Integer featureCounter = 0;
				while (iterator.hasNext()) {
					Feature feature = iterator.next();
					SimpleFeature simpleFeature = (SimpleFeature) feature;
					featureCounter += 1;
					
					Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
					
					try {
						for (VisitLocation_TEST visitLocation : visitLocationList) {
							// Different point definitions needed since referens system differ between layers.
							Point pointRt90 = (Point) reader.read(visitLocation.getRt90WktPointString());
							Point pointSweref99Tm = (Point) reader.read(visitLocation.getSweref99TmWktPointString());
							Point pointGrs80LatLong = (Point) reader.read(visitLocation.getGrs80LatLongWktPointString());
							
							if (shapefileName.equals("an_riks.shp")) {	
								if (geom.contains(pointSweref99Tm)) {
									visitLocation.setCounty(feature.getProperty("LANSNAMN").getValue().toString());
								}
							}
							else if (shapefileName.equals("ak_riks.shp")) {	
								if (geom.contains(pointSweref99Tm)) {
									visitLocation.setMunicipality(feature.getProperty("KOMMUNNAMN").getValue().toString());
								}
							}
//							else if (shapefileName.equals("Havsomr_SVAR_2016_3b.shp")) {
//							else if (shapefileName.equals("Havsomr_SVAR_2016_3b_CP1252.shp")) {
								else if (shapefileName.equals("Havsomr_SVAR_2016_3c_CP1252.shp")) {
								if (geom.contains(pointSweref99Tm)) {
									visitLocation.setSvarSeaAreaCode(feature.getProperty("CWVattenID").getValue().toString());
									visitLocation.setSvarSeaAreaName(feature.getProperty("NAMN").getValue().toString());
									visitLocation.setNationName(feature.getProperty("Country").getValue().toString());
									visitLocation.setWaterDistrict(convertVattendist(feature.getProperty("District").getValue().toString()));
									visitLocation.setTypeArea(convertKustvattentyp(feature.getProperty("TYPOMR_KOD").getValue().toString()));								
									visitLocation.setSeaBasin(convertSeaBasin(feature.getProperty("BASIN_NR").getValue().toString()));
									
									String typ_nfs06 = feature.getProperty("TYP_NFS06").getValue().toString();
									String wb = feature.getProperty("WB").getValue().toString();
									if (!typ_nfs06.equals("0")) {
										visitLocation.setWaterCategory("Havsområde innanför 1 NM");
									} else {
										if (wb.equals("Y")) { 
											visitLocation.setWaterCategory("Havsområde  mellan 1 NM och 12 NM");
										}
										else if (wb.equals("P")) { // P=preliminary.
											visitLocation.setWaterCategory("Havsområde  mellan 1 NM och 12 NM");
										}
										else {
											visitLocation.setWaterCategory("Utsjövatten");
										}
									}
									
									
									
									// TODO:
//									visitLocation.setWaterBodyUuid(feature.getProperty("UUID").getValue().toString());
								}
							}
							else if (shapefileName.equals("KONVENTION.shp")) {
								if (geom.contains(pointRt90)) {
									visitLocation.setHelcomOsparArea(feature.getProperty("KONVENTION").getValue().toString());
								}
							}
//							else if (shapefileName.equals("havsomr_y_2012_2.shp")) {
//								if (geom.contains(pointSweref99Tm)) {
//									visitLocation.setWaterCategory(convertWaterCategoryType(feature.getProperty("OMRTYP").getValue().toString()));
//								}
//							}
//							else if (shapefileName.equals("SE_clip_from_World_EEZ_v10.shp")) {
//								if (geom.contains(pointGrs80LatLong)) {
//									visitLocation.setEconomicZone(feature.getProperty("GeoName").getValue().toString());
//								}
//							}
//							else if (shapefileName.equals("REGION_ID_diss_3.shp")) {
//								if (geom.contains(pointSweref99Tm)) {
//									//visitLocation.setEconomicZone("Svensk ekonomisk zon"));
//									visitLocation.setEconomicZone(feature.getProperty("REGION_ID").getValue().toString());
//								}
//							}
						}
					} catch (Exception e) {
						System.out.println("Exception in ShapeFileReader list: " + e.getMessage());
						ErrorLogger.println("Exception in ShapeFileReaderlist: " + e.getMessage());
					}
					
				}
				iterator.close();				
//				dataStore.dispose();
								
				try {
	//				// Some stations are located on land, but should be linked to the 
	//				// nearest water area. Use parameter for 'isWithinDistance()'.
//					if (shapefileName.equals("Havsomr_SVAR_2016_3b_CP1252.shp")) {
					if (shapefileName.equals("Havsomr_SVAR_2016_3c_CP1252.shp")) {
	//				if (shapefileName.equals("havdirtyper_2012_delatKattegatt.shp")) {
						// Second iteration with 50 meters distance.
						iterator = collection.features(); // Reset iterator.
						while (iterator.hasNext()) {
							Feature feature = iterator.next();
							SimpleFeature simpleFeature = (SimpleFeature) feature;
							Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
							
							for (VisitLocation_TEST visitLocation : visitLocationList) {
	//						for (VisitLocation visitLocation : visitLocationList) {
								if (visitLocation.getSvarSeaAreaName().equals("")) {
									Point pointSweref99 = (Point) reader.read(visitLocation.getSweref99TmWktPointString());
									if (pointSweref99.isWithinDistance(geom, 50)) { // Use distance. Unit: meter.

										visitLocation.setSvarSeaAreaCode(feature.getProperty("CWVattenID").getValue().toString());
										visitLocation.setSvarSeaAreaName(feature.getProperty("NAMN").getValue().toString());
										visitLocation.setNationName(feature.getProperty("Country").getValue().toString());
										visitLocation.setWaterDistrict(convertVattendist(feature.getProperty("District").getValue().toString()));
										visitLocation.setTypeArea(convertKustvattentyp(feature.getProperty("TYPOMR_KOD").getValue().toString()));								
										visitLocation.setSeaBasin(convertSeaBasin(feature.getProperty("BASIN_NR").getValue().toString()));
										
										String typ_nfs06 = feature.getProperty("TYP_NFS06").getValue().toString();
										String wb = feature.getProperty("WB").getValue().toString();
										if (!typ_nfs06.equals("0")) {
											visitLocation.setWaterCategory("Havsområde innanför 1 NM");
										} else {
											if (wb.equals("Y")) { 
												visitLocation.setWaterCategory("Havsområde  mellan 1 NM och 12 NM");
											}
											else if (wb.equals("P")) { // P=preliminary.
												visitLocation.setWaterCategory("Havsområde  mellan 1 NM och 12 NM");
											}
											else {
												visitLocation.setWaterCategory("Utsjövatten");
											}
										}

//										visitLocation.setSvarSeaAreaName( 
//												feature.getProperty("NAMN").getValue().toString());
//	//											feature.getProperty("NAMN").getValue().toString());
//										visitLocation.setSvarSeaAreaCode( 
//												feature.getProperty("CWVattenID").getValue().toString());
//	//											feature.getProperty("HID").getValue().toString());
//										visitLocation.setWaterDistrict(
//												convertVattendist(
//														feature.getProperty("District").getValue().toString()));
//	//													feature.getProperty("Vattendist").getValue().toString()));
//										visitLocation.setTypeArea(
//												convertKustvattentyp(
//														feature.getProperty("TYPOMR_KOD").getValue().toString()));								
//	//													feature.getProperty("TYP_HAVDIR").getValue().toString()));								
									
										System.out.println("DEBUG: Second check on Havsomr_SVAR_2016_3c_CP1252.shp (50 m). Matched on: " +
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
							
							for (VisitLocation_TEST visitLocation : visitLocationList) {
	//						for (VisitLocation visitLocation : visitLocationList) {
								if (visitLocation.getSvarSeaAreaName().equals("")) {
									Point pointSweref99 = (Point) reader.read(visitLocation.getSweref99TmWktPointString());
									if (pointSweref99.isWithinDistance(geom, 200)) { // Use distance. Unit: meter.

										visitLocation.setSvarSeaAreaCode(feature.getProperty("CWVattenID").getValue().toString());
										visitLocation.setSvarSeaAreaName(feature.getProperty("NAMN").getValue().toString());
										visitLocation.setNationName(feature.getProperty("Country").getValue().toString());
										visitLocation.setWaterDistrict(convertVattendist(feature.getProperty("District").getValue().toString()));
										visitLocation.setTypeArea(convertKustvattentyp(feature.getProperty("TYPOMR_KOD").getValue().toString()));								
										visitLocation.setSeaBasin(convertSeaBasin(feature.getProperty("BASIN_NR").getValue().toString()));
										
										String typ_nfs06 = feature.getProperty("TYP_NFS06").getValue().toString();
										String wb = feature.getProperty("WB").getValue().toString();
										if (!typ_nfs06.equals("0")) {
											visitLocation.setWaterCategory("Havsområde innanför 1 NM");
										} else {
											if (wb.equals("Y")) { 
												visitLocation.setWaterCategory("Havsområde  mellan 1 NM och 12 NM");
											}
											else if (wb.equals("P")) { // P=preliminary.
												visitLocation.setWaterCategory("Havsområde  mellan 1 NM och 12 NM");
											}
											else {
												visitLocation.setWaterCategory("Utsjövatten");
											}
										}

//										visitLocation.setSvarSeaAreaName( 
//												feature.getProperty("NAMN").getValue().toString());
//	//											feature.getProperty("NAMN").getValue().toString());
//										visitLocation.setSvarSeaAreaCode( 
//												feature.getProperty("CWVattenID").getValue().toString());
//	//											feature.getProperty("HID").getValue().toString());
//										visitLocation.setWaterDistrict(
//												convertVattendist(
//														feature.getProperty("District").getValue().toString()));
//	//													feature.getProperty("Vattendist").getValue().toString()));
//										visitLocation.setTypeArea(
//												convertKustvattentyp(
//														feature.getProperty("TYPOMR_KOD").getValue().toString()));								
//	//													feature.getProperty("TYP_HAVDIR").getValue().toString()));								
									
										System.out.println("DEBUG: Third check on Havsomr_SVAR_2016_3c_CP1252.shp (200 m). Matched on: " +
												visitLocation.getSvarSeaAreaName() +
												"   Position key: " + visitLocation.getLocationId());
	//									ErrorLogger.println("");
	
									}
								}		    		  
							}
						}
						iterator.close();
					}
			} catch (Exception e) {
				System.out.println("Exception in ShapeFileReader list: " + e.getMessage());
				ErrorLogger.println("Exception in ShapeFileReaderlist: " + e.getMessage());
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
				
			} finally {
				dataStore.dispose();
			}
				
		} catch (Exception e) {
			System.out.println("Exception in ShapeFileReader: " + e.getMessage());
			ErrorLogger.println("Exception in ShapeFileReader: " + e.getMessage());
		}
	}
	
	public String convertWaterCategoryType(String waterCategoryType) {
		String descr = waterCategoryType;
//		if      (waterCategoryType.equals("1")) descr = "1 - Estuarie (övergångsvatten)";
		if      (waterCategoryType.equals("1")) descr = "Havsområde innanför 1 NM";
		else if (waterCategoryType.equals("2")) descr = "Havsområde innanför 1 NM";
		else if (waterCategoryType.equals("3")) descr = "Havsområde  mellan 1 NM och 12 NM";
		else if (waterCategoryType.equals("4")) descr = "Utsjövatten";
		else if (waterCategoryType.equals("5")) descr = "Öar med vattendelare";		
		else if (waterCategoryType.equals("6")) descr = "Övriga öar";		
		else {
			System.out.println("ShapeFileReader:convertWaterCategoryType() String not found: " + waterCategoryType);
		}
		return descr;
	}
				
	public String convertSeaBasin(String seaBasin) {
		String descr = seaBasin;
		if      (seaBasin.equals("")) descr = "";
		else if (seaBasin.equals("1")) descr = "01 - Bottenviken";
		else if (seaBasin.equals("2")) descr = "02 - Norra Kvarken";
		else if (seaBasin.equals("3")) descr = "03 - Bottenhavet";
		else if (seaBasin.equals("4")) descr = "04 - Ålands hav";
		else if (seaBasin.equals("5")) descr = "05 - Skärgårdshavet";		
		else if (seaBasin.equals("6")) descr = "06 - Finska viken";		
		else if (seaBasin.equals("7")) descr = "07 - Norra Gotlandshavet";		
		else if (seaBasin.equals("8")) descr = "08 - Västra Gotlandshavet";		
		else if (seaBasin.equals("9")) descr = "09 - Östra Gotlandshavet";		
		else if (seaBasin.equals("10")) descr = "10 - Rigabukten";		
		else if (seaBasin.equals("11")) descr = "11 - Gdanskbukten";		
		else if (seaBasin.equals("12")) descr = "12 - Bornholmshavet och Hanöbukten";		
		else if (seaBasin.equals("13")) descr = "13 - Arkonahavet och Södra Öresund";		
		else if (seaBasin.equals("14")) descr = "14 - Bälthavet";		
		else if (seaBasin.equals("15")) descr = "15 - Öresund";		
		else if (seaBasin.equals("16")) descr = "16 - Kattegatt";		
		else if (seaBasin.equals("17")) descr = "17 - Skagerrak";		
		else {
			System.out.println("convertSeaBasin() String not found: " + seaBasin);
		}
		return descr;
	}
		
	public String convertVattendist(String vattendist) {
		String descr = vattendist;
		if      (vattendist.equals(" ")) descr = "";
		else if (vattendist.equals("SE1")) descr = "1. Bottenviken (nationell del)";
		else if (vattendist.equals("SE1TO")) descr = "1. Bottenviken (Int. dist. Torneälven - Sverige)";
		else if (vattendist.equals("SENO1104")) descr = "1. Bottenviken (Int. avr. omr. Troms - Sverige)";
		else if (vattendist.equals("SENO1103")) descr = "1. Bottenviken (Int. avr. omr. Nordland - Sverige)";
		else if (vattendist.equals("SE2")) descr = "2. Bottenhavet (nationell del)";
		else if (vattendist.equals("SENO1102")) descr = "2. Bottenhavet (Int. avr. omr. Trøndelagsfylkene - Sverige)";
		else if (vattendist.equals("SE3")) descr = "3. Norra Östersjön";
		else if (vattendist.equals("SE4")) descr = "4. Södra Östersjön";
		else if (vattendist.equals("SE5")) descr = "5. Västerhavet (nationell del)";
		else if (vattendist.equals("SENO5101")) descr = "5. Västerhavet (Int. avr. omr. Glomma - Sverige)";
		else if (vattendist.equals("NO1104")) descr = "Norskt distrikt";
		else if (vattendist.equals("NO1103")) descr = "Norskt distrikt";
		else if (vattendist.equals("NO1102")) descr = "Norskt distrikt";
		else if (vattendist.equals("NO5101")) descr = "Norskt distrikt";
		else if (vattendist.equals("NOSE1")) descr = "Vatten som rinner från Norge till Svenska SE1";
		else if (vattendist.equals("NOSE2")) descr = "Vatten som rinner från Norge till Svenska SE2";
		else if (vattendist.equals("NOSE5")) descr = "Vatten som rinner från Norge till Svenska SE5";
		else if (vattendist.equals("NOSE1TO")) descr = "Vatten som rinner från Norge till Svenska SE1TO";
		else if (vattendist.equals("VHA6")) descr = "Finska delen av Torneälvens avrinningsområde";
		else if (vattendist.equals("SE0")) descr = "Övriga områden utanför Sverige";
		else {
			System.out.println("ShapeFileReader:convertVattendist() String not found: " + vattendist);
		}
		return descr;
		
	}
		
	public String convertKustvattentyp(String typomr_kod) {
		String descr = typomr_kod;
		if      (typomr_kod.equals(""))    descr = "";
		else if (typomr_kod.equals("1"))   descr = "01 - Västkustens inre kustvatten";
		else if (typomr_kod.equals("1s"))  descr = "01s - Västkustens inre kustvatten";
		else if (typomr_kod.equals("1n"))  descr = "01n - Västkustens inre kustvatten";
		else if (typomr_kod.equals("2"))   descr = "02 - Västkustens fjordar";
		else if (typomr_kod.equals("3"))   descr = "03 - Västkustens yttre kustvatten. Skagerrak";
		else if (typomr_kod.equals("4"))   descr = "04 - Västkustens yttre kustvatten. Kattegatt";
		else if (typomr_kod.equals("5"))   descr = "05 - Södra Hallands och norra Öresunds kustvatten";
		else if (typomr_kod.equals("6"))   descr = "06 - Öresunds kustvatten";
		else if (typomr_kod.equals("7"))   descr = "07 - Skånes kustvatten";
		else if (typomr_kod.equals("8"))   descr = "08 - Blekinge skärgård och Kalmarsund. Inre kustvatten";
		else if (typomr_kod.equals("9"))   descr = "09 - Blekinge skärgård och Kalmarsund. Yttre kustvatten";
		else if (typomr_kod.equals("10"))  descr = "10 - Ölands och Gotlands kustvatten";
		else if (typomr_kod.equals("11"))  descr = "11 - Gotlands nordvästra kustvatten";
		else if (typomr_kod.equals("12"))  descr = "12 - Östergötlands och Stockholms skärgård. Mellankustvatten";
		else if (typomr_kod.equals("12n")) descr = "12n - Östergötlands och Stockholms skärgård. Mellankustvatten";
		else if (typomr_kod.equals("12s")) descr = "12s - Östergötlands och Stockholms skärgård. Mellankustvatten";
		else if (typomr_kod.equals("13"))  descr = "13 - Östergötlands inre kustvatten";
		else if (typomr_kod.equals("14"))  descr = "14 - Östergötlands yttre kustvatten";
		else if (typomr_kod.equals("15"))  descr = "15 - Stockholms skärgård. Yttre kustvatten";
		else if (typomr_kod.equals("16"))  descr = "16 - Södra Bottenhavet. Inre kustvatten"; 
		else if (typomr_kod.equals("17"))  descr = "17 - Södra Bottenhavet. Yttre kustvatten";
		else if (typomr_kod.equals("18"))  descr = "18 - Norra Bottenhavet. Höga kusten. Inre kustvatten";
		else if (typomr_kod.equals("19"))  descr = "19 - Norra Bottenhavet. Höga kusten. Yttre kustvatten";
		else if (typomr_kod.equals("20"))  descr = "20 - Norra Kvarkens inre kustvatten";
		else if (typomr_kod.equals("21"))  descr = "21 - Norra Kvarkens yttre kustvatten";
		else if (typomr_kod.equals("22"))  descr = "22 - Norra Bottenviken. Inre kustvatten";
		else if (typomr_kod.equals("23"))  descr = "23 - Norra Bottenviken. Yttre kustvatten";
		else if (typomr_kod.equals("24"))  descr = "24 - Stockholms inre skärgård och Hallsfjärden";
		else if (typomr_kod.equals("25"))  descr = "25 - Göta älvs- och Nordre älvs estuarie";
		else {
			System.out.println("ShapeFileReader:convertKustvattentyp() String not found: " + typomr_kod);
		}
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
//    public static void main(String[] args) throws Exception {
//    	
//    	// TEST:
////		VisitLocationManager.instance().addVisitLocation(57.5, 18.5);
////		VisitLocationManager.instance().addVisitLocation(57.5, 17.5);
////		VisitLocationManager.instance().addVisitLocation(57.5, 16.5);
////		VisitLocationManager.instance().addVisitLocation(57.5, 15.5);
//		VisitLocationManager_TEST.instance().addVisitLocation(56.94, 12.21167);
//
//		VisitLocationManager_TEST.instance().readDataFromShapefiles();
//		
//		Display display = new Display();
//		Shell shell = new Shell(display);
//		
//        // Display a data store file chooser dialog for shapefiles.
////        File file = JFileDataStoreChooser.showOpenFile("shp", new File("\\\\winfs\\data\\prodkap\\sharkweb\\SHARK_CONFIG\\sharkweb_shapefiles"), null);
////        File file = JFileDataStoreChooser.showOpenFile("shp", new File("D:\\arnold\\2a_sharkadm\\w_sharkadm\\p_sharkadm_branch_2018\\TEST_SHARK_CONFIG\\sharkweb_shapefiles"), shell);
//        File file = JFileDataStoreChooser.showOpenFile("shp", shell);
//        if (file == null) {
//            return;
//        }
//        
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
//    }
}
