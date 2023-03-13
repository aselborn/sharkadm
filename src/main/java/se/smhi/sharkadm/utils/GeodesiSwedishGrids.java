package se.smhi.sharkadm.utils;

/**
 * Java version of the JavaScript on this page:
 * http://latlong.mellifica.se
 * 
 * Javascript-implementation of "Gauss Conformal Projection 
 * (Transverse Mercator), Krügers Formulas".
 * - Parameters for SWEREF99 lat-long to/from RT90 and SWEREF99 
 * coordinates (RT90 and SWEREF99 are used in Swedish maps).
 * Source: http://www.lantmateriet.se/geodesi/
 * Author: Arnold Andreasson, 2007. info@mellifica.se
 * License: http://creativecommons.org/licenses/by-nc-sa/3.0/
 * 
 */

public class GeodesiSwedishGrids {
	// Ellipsoid parameters:
	private boolean parametersInitialized = false;
	private double axis; // Semi-major axis of the ellipsoid.
	private double flattening; // Flattening of the ellipsoid.
	private double central_meridian; // Central meridian for the projection.
	@SuppressWarnings("unused")
	private double lat_of_origin; // Latitude of origin. // Not used.
	private double scale; // Scale on central meridian.
	private double false_northing; // Offset for origo.
	private double false_easting; // Offset for origo.
	// Calculated values only related to ellipsoid parameters:
	private double e2;
	private double n;
	private double a_roof;
	private double A;
	private double B;
	private double C;
	private double D;
	private double beta1;
	private double beta2;
	private double beta3;
	private double beta4;
	private double delta1;
	private double delta2;
	private double delta3;
	private double delta4;
	private double Astar;
	private double Bstar;
	private double Cstar;
	private double Dstar;
	
	public GeodesiSwedishGrids() {
		// Default projection for Sweden is SWEREF 99 TM.
		swedishGridParams("sweref_99_tm");
		prepareEllipsoid();
	}

	public GeodesiSwedishGrids(String projection) {
		// Set parameters to be used in calculations.
		swedishGridParams(projection);
		prepareEllipsoid();
	}

	/**
	 * Conversion from geodetic coordinates to grid coordinates.
	 * @param latitude
	 * @param longitude
	 * @return x_y[] where x_y[0] is north (rt90:X, Sweref99TM:N) and 
	 * x_y[1] is east (rt90:Y, Sweref99TM:E). Note the strange usage of
	 * X and Y in RT90.
	 */
	public double[] geodetic_to_grid(double latitude, double longitude) {
		double[] x_y = new double[2];
		if (parametersInitialized == false) {
			return null;
		}
		double deg_to_rad = Math.PI / 180.0;
		double phi = latitude * deg_to_rad;
		double lambda = longitude * deg_to_rad;
		double lambda_zero = central_meridian * deg_to_rad;
		
		double phi_star = phi - Math.sin(phi) * Math.cos(phi) * (A + 
						B*Math.pow(Math.sin(phi), 2) + 
						C*Math.pow(Math.sin(phi), 4) + 
						D*Math.pow(Math.sin(phi), 6));
		double delta_lambda = lambda - lambda_zero;
		double xi_prim = Math.atan(Math.tan(phi_star) / Math.cos(delta_lambda));
		double eta_prim = math_atanh(Math.cos(phi_star) * Math.sin(delta_lambda));
		double x = scale * a_roof * (xi_prim +
						beta1 * Math.sin(2.0*xi_prim) * Math.cosh(2.0*eta_prim) +
						beta2 * Math.sin(4.0*xi_prim) * Math.cosh(4.0*eta_prim) +
						beta3 * Math.sin(6.0*xi_prim) * Math.cosh(6.0*eta_prim) +
						beta4 * Math.sin(8.0*xi_prim) * Math.cosh(8.0*eta_prim)) + 
						false_northing;
		double y = scale * a_roof * (eta_prim +
						beta1 * Math.cos(2.0*xi_prim) * Math.sinh(2.0*eta_prim) +
						beta2 * Math.cos(4.0*xi_prim) * Math.sinh(4.0*eta_prim) +
						beta3 * Math.cos(6.0*xi_prim) * Math.sinh(6.0*eta_prim) +
						beta4 * Math.cos(8.0*xi_prim) * Math.sinh(8.0*eta_prim)) + 
						false_easting;
		x_y[0] = Math.round(x * 1000.0) / 1000.0;
		x_y[1] = Math.round(y * 1000.0) / 1000.0;
	//	x_y[0] = x;
	//	x_y[1] = y;
		return x_y;
	}
	
	/**
	 * Conversion from grid coordinates to geodetic coordinates.
	 * @param x
	 * @param y
	 * @return
	 */
	public double[] grid_to_geodetic(double x, double y) {
		double[] lat_lon = new double[2];
		if (parametersInitialized == false) {
			return null;
		}
		double deg_to_rad = Math.PI / 180;
		double lambda_zero = central_meridian * deg_to_rad;
		double xi = (x - false_northing) / (scale * a_roof);		
		double eta = (y - false_easting) / (scale * a_roof);
		double xi_prim = xi - 
						delta1*Math.sin(2.0*xi) * Math.cosh(2.0*eta) - 
						delta2*Math.sin(4.0*xi) * Math.cosh(4.0*eta) - 
						delta3*Math.sin(6.0*xi) * Math.cosh(6.0*eta) - 
						delta4*Math.sin(8.0*xi) * Math.cosh(8.0*eta);
		double eta_prim = eta - 
						delta1*Math.cos(2.0*xi) * Math.sinh(2.0*eta) - 
						delta2*Math.cos(4.0*xi) * Math.sinh(4.0*eta) - 
						delta3*Math.cos(6.0*xi) * Math.sinh(6.0*eta) - 
						delta4*Math.cos(8.0*xi) * Math.sinh(8.0*eta);
		double phi_star = Math.asin(Math.sin(xi_prim) / Math.cosh(eta_prim));
		double delta_lambda = Math.atan(Math.sinh(eta_prim) / Math.cos(xi_prim));
		double lon_radian = lambda_zero + delta_lambda;
		double lat_radian = phi_star + Math.sin(phi_star) * Math.cos(phi_star) * 
						(Astar + 
						 Bstar*Math.pow(Math.sin(phi_star), 2) + 
						 Cstar*Math.pow(Math.sin(phi_star), 4) + 
						 Dstar*Math.pow(Math.sin(phi_star), 6));  	
		lat_lon[0] = lat_radian * 180.0 / Math.PI;
		lat_lon[1] = lon_radian * 180.0 / Math.PI;
		return lat_lon;
	}
	
	private void prepareEllipsoid() {
		e2 = flattening * (2.0 - flattening);
		n = flattening / (2.0 - flattening);
		a_roof = axis / (1.0 + n) * (1.0 + n*n/4.0 + n*n*n*n/64.0);

		// Prepare ellipsoid-based stuff for geodetic_to_grid.
		A = e2;
		B = (5.0*e2*e2 - e2*e2*e2) / 6.0;
		C = (104.0*e2*e2*e2 - 45.0*e2*e2*e2*e2) / 120.0;
		D = (1237.0*e2*e2*e2*e2) / 1260.0;
		beta1 = n/2.0 - 2.0*n*n/3.0 + 5.0*n*n*n/16.0 + 41.0*n*n*n*n/180.0;
		beta2 = 13.0*n*n/48.0 - 3.0*n*n*n/5.0 + 557.0*n*n*n*n/1440.0;
		beta3 = 61.0*n*n*n/240.0 - 103.0*n*n*n*n/140.0;
		beta4 = 49561.0*n*n*n*n/161280.0;
		
		// Prepare ellipsoid-based stuff for grid_to_geodetic.
		delta1 = n/2.0 - 2.0*n*n/3.0 + 37.0*n*n*n/96.0 - n*n*n*n/360.0;
		delta2 = n*n/48.0 + n*n*n/15.0 - 437.0*n*n*n*n/1440.0;
		delta3 = 17.0*n*n*n/480.0 - 37*n*n*n*n/840.0;
		delta4 = 4397.0*n*n*n*n/161280.0;
		
		Astar = e2 + e2*e2 + e2*e2*e2 + e2*e2*e2*e2;
		Bstar = -(7.0*e2*e2 + 17.0*e2*e2*e2 + 30.0*e2*e2*e2*e2) / 6.0;
		Cstar = (224.0*e2*e2*e2 + 889.0*e2*e2*e2*e2) / 120.0;
		Dstar = -(4279.0*e2*e2*e2*e2) / 1260.0;
	}

	/**
	 * Parameters for RT90 and SWEREF99TM.
	 * Note: Parameters for RT90 are chosen to eliminate the 
	 * differences between Bessel and GRS80-ellipsoids.
	 * Bessel-variants should only be used if lat/long are given as
	 * RT90-lat/long based on the Bessel ellipsoid (from old maps).
	 * Parameter: projection (string). Must match if-statement.
	 */
	private void swedishGridParams(String projection) {
		// RT90 parameters, GRS 80 ellipsoid.
		if (projection == "rt90_7.5_gon_v") {
			grs80_params();
			central_meridian = 11.0 + 18.375/60.0;
			scale = 1.000006000000;
			false_northing = -667.282;
			false_easting = 1500025.141;
			parametersInitialized = true;
		}
		else if (projection == "rt90_5.0_gon_v") {
			grs80_params();
			central_meridian = 13.0 + 33.376/60.0;
			scale = 1.000005800000;
			false_northing = -667.130;
			false_easting = 1500044.695;
			parametersInitialized = true;
		}
		else if (projection == "rt90_2.5_gon_v") {
			grs80_params();
			central_meridian = 15.0 + 48.0/60.0 + 22.624306/3600.0;
			scale = 1.00000561024;
			false_northing = -667.711;
			false_easting = 1500064.274;
			parametersInitialized = true;
		}
		else if (projection == "rt90_0.0_gon_v") {
			grs80_params();
			central_meridian = 18.0 + 3.378/60.0;
			scale = 1.000005400000;
			false_northing = -668.844;
			false_easting = 1500083.521;
			parametersInitialized = true;
		}
		else if (projection == "rt90_2.5_gon_o") {
			grs80_params();
			central_meridian = 20.0 + 18.379/60.0;
			scale = 1.000005200000;
			false_northing = -670.706;
			false_easting = 1500102.765;
			parametersInitialized = true;
		}
		else if (projection == "rt90_5.0_gon_o") {
			grs80_params();
			central_meridian = 22.0 + 33.380/60.0;
			scale = 1.000004900000;
			false_northing = -672.557;
			false_easting = 1500121.846;
			parametersInitialized = true;
		}
		
		// RT90 parameters, Bessel 1841 ellipsoid.
		else if (projection == "bessel_rt90_7.5_gon_v") {
			bessel_params();
			central_meridian = 11.0 + 18.0/60.0 + 29.8/3600.0;
			parametersInitialized = true;
		}
		else if (projection == "bessel_rt90_5.0_gon_v") {
			bessel_params();
			central_meridian = 13.0 + 33.0/60.0 + 29.8/3600.0;
			parametersInitialized = true;
		}
		else if (projection == "bessel_rt90_2.5_gon_v") {
			bessel_params();
			central_meridian = 15.0 + 48.0/60.0 + 29.8/3600.0;
			parametersInitialized = true;
		}
		else if (projection == "bessel_rt90_0.0_gon_v") {
			bessel_params();
			central_meridian = 18.0 + 3.0/60.0 + 29.8/3600.0;
			parametersInitialized = true;
		}
		else if (projection == "bessel_rt90_2.5_gon_o") {
			bessel_params();
			central_meridian = 20.0 + 18.0/60.0 + 29.8/3600.0;
			parametersInitialized = true;
		}
		else if (projection == "bessel_rt90_5.0_gon_o") {
			bessel_params();
			central_meridian = 22.0 + 33.0/60.0 + 29.8/3600.0;
			parametersInitialized = true;
		}
	
		// SWEREF99TM and SWEREF99ddmm  parameters.
		else if (projection == "sweref_99_tm") {
			sweref99_params();
			central_meridian = 15.00;
			lat_of_origin = 0.0;
			scale = 0.9996;
			false_northing = 0.0;
			false_easting = 500000.0;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1200") {
			sweref99_params();
			central_meridian = 12.00;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1330") {
			sweref99_params();
			central_meridian = 13.50;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1500") {
			sweref99_params();
			central_meridian = 15.00;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1630") {
			sweref99_params();
			central_meridian = 16.50;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1800") {
			sweref99_params();
			central_meridian = 18.00;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1415") {
			sweref99_params();
			central_meridian = 14.25;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1545") {
			sweref99_params();
			central_meridian = 15.75;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1715") {
			sweref99_params();
			central_meridian = 17.25;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_1845") {
			sweref99_params();
			central_meridian = 18.75;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_2015") {
			sweref99_params();
			central_meridian = 20.25;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_2145") {
			sweref99_params();
			central_meridian = 21.75;
			parametersInitialized = true;
		}
		else if (projection == "sweref_99_2315") {
			sweref99_params();
			central_meridian = 23.25;
			parametersInitialized = true;
		}
	
		// Test-case:
		//	Lat: 66 0'0", lon: 24 0'0".
		//	X:1135809.413803 Y:555304.016555.
		else if (projection == "test_case") {
			axis = 6378137.0;
			flattening = 1.0 / 298.257222101;
			central_meridian = 13.0 + 35.0/60.0 + 7.692000/3600.0;
			lat_of_origin = 0.0;
			scale = 1.000002540000;
			false_northing = -6226307.8640;
			false_easting = 84182.8790;
			parametersInitialized = true;
	
		// Not a valid projection.		
		} else {
			parametersInitialized = false;
		}
	}
	
	// Sets of default parameters.
	private void grs80_params() {
		axis = 6378137.0; // GRS 80.
		flattening = 1.0 / 298.257222101; // GRS 80.
		lat_of_origin = 0.0;
	}
	
	private void bessel_params() {
		axis = 6377397.155; // Bessel 1841.
		flattening = 1.0 / 299.1528128; // Bessel 1841.
		lat_of_origin = 0.0;
		scale = 1.0;
		false_northing = 0.0;
		false_easting = 1500000.0;
	}

	private void sweref99_params() {
		axis = 6378137.0; // GRS 80.
		flattening = 1.0 / 298.257222101; // GRS 80.
		lat_of_origin = 0.0;
		scale = 1.0;
		false_northing = 0.0;
		false_easting = 150000.0;
	}

	// Missing function in the Math library.
	private double math_atanh(double value) {
		return 0.5 * Math.log((1.0 + value) / (1.0 - value));
	}
}
