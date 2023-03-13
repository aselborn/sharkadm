/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

public class TaxonNameObject {
	private int taxonNametype;
	private String name = "";
	private String author = "";
	private String nameValidFromDate = "";
	private String nameValidToDate = "";
	private int nameValidityCode;
	private boolean validName = false;
	private String comment = "";
	
	public boolean isEqual(TaxonNameObject anotherTaxonNameObject) {
		if ((this.taxonNametype == anotherTaxonNameObject.taxonNametype) &&
			(this.name.equals(anotherTaxonNameObject.name)) &&
			(author.equals(anotherTaxonNameObject.author)) &&
			(nameValidFromDate.equals(anotherTaxonNameObject.nameValidFromDate)) &&
			(nameValidToDate.equals(anotherTaxonNameObject.nameValidToDate)) &&
			(nameValidityCode == anotherTaxonNameObject.nameValidityCode) &&
			(validName == anotherTaxonNameObject.validName) &&
			(comment.equals(anotherTaxonNameObject.comment))) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNameValidFromDate() {
		return nameValidFromDate;
	}
	public void setNameValidFromDate(String nameValidFromDate) {
		this.nameValidFromDate = nameValidFromDate;
	}
	public int getNameValidityCode() {
		return nameValidityCode;
	}
	public void setNameValidityCode(int nameValidityCode) {
		this.nameValidityCode = nameValidityCode;
	}
	public String getNameValidToDate() {
		return nameValidToDate;
	}
	public void setNameValidToDate(String nameValidToDate) {
		this.nameValidToDate = nameValidToDate;
	}
	public int getTaxonNametype() {
		return taxonNametype;
	}
	public void setTaxonNametype(int taxonNametype) {
		this.taxonNametype = taxonNametype;
	}	
	public boolean isValidName() {
		return validName;
	}
	public void setValidName(boolean validName) {
		this.validName = validName;
	}
}
