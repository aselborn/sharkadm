/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

import java.util.Comparator;

public class TaxonNameComparator implements Comparator<Object> {
	public int compare(Object firstTaxonNode1, Object secondTaxonNode2) {
		String firstName;
		try {
			firstName = ((TaxonNode) firstTaxonNode1).getTaxonObject()
					.getValidNameObject().getName();
		} catch (Exception e) {
			firstName = "";
		}
		String secondName;
		try {
			secondName = ((TaxonNode) secondTaxonNode2).getTaxonObject()
					.getValidNameObject().getName();
		} catch (Exception e) {
			secondName = "";
		}
		return firstName.compareTo(secondName);
	}
}
