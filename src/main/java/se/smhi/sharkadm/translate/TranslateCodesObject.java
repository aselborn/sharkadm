/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.translate;

public class TranslateCodesObject {
	
	private String internalKey = "";
	private String internalValue = "";
	private String synonyms = "";
	private String shortName = "";
	private String englishName = "";
	private String swedishName = "";
	private String prefix = "";
	public String getInternalKey() {
		return internalKey;
	}
	public void setInternalKey(String internalKey) {
		this.internalKey = internalKey;
	}
	public String getInternalValue() {
		return internalValue;
	}
	public void setInternalValue(String internalValue) {
		this.internalValue = internalValue;
	}
	public String getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getEnglishName() {
		return englishName;
	}
	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}
	public String getSwedishName() {
		return swedishName;
	}
	public void setSwedishName(String swedishName) {
		this.swedishName = swedishName;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}	
 		
}
