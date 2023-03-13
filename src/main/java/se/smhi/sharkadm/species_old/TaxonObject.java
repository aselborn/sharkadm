/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

import java.util.ArrayList;
import java.util.List;

public class TaxonObject {
	private TaxonNode taxonNode;
	private int dyntaxaId;
//	private int taxonTypeId;
	private String taxonRank = "";
	private String author = "";
	private String validFromDate = "";
	private String validToDate = "";
	private String changedDate = "";
	private String addedDate = "";
	private boolean active = false;
	private String comment = "";

	private String recommendedGUID = ""; // WoRMS LSID, etc.
	
    public String getRecommendedGUID() {
		return recommendedGUID;
	}

	public void setRecommendedGUID(String recommendedGUID) {
		this.recommendedGUID = recommendedGUID;
	}

	private List<TaxonNameObject> taxonNames = new ArrayList<TaxonNameObject>();

	public String getTaxonName(int typeCode) {
		for (TaxonNameObject nameObject : taxonNames) {
			if ((nameObject.getTaxonNametype() == typeCode)) {
				return nameObject.getName();
			}
		}
		return "";
	}
	
	public TaxonNameObject getValidNameObject() {
		TaxonNameObject tmpNameObject = null;
		
		for (TaxonNameObject nameObject : taxonNames) {
			if (nameObject.isValidName()) {
				tmpNameObject  = nameObject;
			}
		}
		return tmpNameObject;
	}
	
	public String getSynonymNames() {
		String synonyms = "";
		String delimiter = "";
		
		for (TaxonNameObject nameObject : taxonNames) {
			if ((nameObject.getTaxonNametype() == 0) && 
				(!nameObject.isValidName()) && 
				(nameObject.getNameValidityCode() == 0)) {
				if (nameObject.getAuthor().equals("")) {
					synonyms  += delimiter + nameObject.getName();
					delimiter = ". ";
				} else {
					synonyms  += delimiter + nameObject.getName() + " " + nameObject.getAuthor();
					delimiter = ". ";
				}
			}
		}
		if (!synonyms.equals("")) {
			synonyms += ".";
		}
		return synonyms;
	}
	
	public List<TaxonNameObject> getTaxonNames() {
		return taxonNames;
	}
	
	public void addTaxonNames(TaxonNameObject name) {
		taxonNames.add(name);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getAddedDate() {
		return addedDate;
	}

	public void setAddedDate(String addedDate) {
		this.addedDate = addedDate;
	}

	public String getChangedDate() {
		return changedDate;
	}

	public void setChangedDate(String changedDate) {
		this.changedDate = changedDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public TaxonNode getTaxonNode() {
		return taxonNode;
	}

	public void setTaxonNode(TaxonNode taxonNode) {
		this.taxonNode = taxonNode;
	}

	public int getDyntaxaId() {
		return dyntaxaId;
	}

	public void setDyntaxaId(int dyntaxaId) {
		this.dyntaxaId = dyntaxaId;
	}

//	public int getTaxonTypeId() {
//		return taxonTypeId;
//	}
//
//	public void setTaxonTypeId(int taxonTypeId) {
//		this.taxonTypeId = taxonTypeId;
//	}

	public String getTaxonRank() {
		return taxonRank;
	}

	public void setTaxonRank(String taxonRank) {
		this.taxonRank = taxonRank;
	}

	public String getValidFromDate() {
		return validFromDate;
	}

	public void setValidFromDate(String validFromDate) {
		this.validFromDate = validFromDate;
	}

	public String getValidToDate() {
		return validToDate;
	}

	public void setValidToDate(String validToDate) {
		this.validToDate = validToDate;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
}
