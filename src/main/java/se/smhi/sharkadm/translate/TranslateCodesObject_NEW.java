/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.translate;

public class TranslateCodesObject_NEW {
	
	private String field = "";
	private String public_value = "";
	private String code = "";
	private String swedish = "";
	private String english = "";
	private String ices_biology = "";
	private String ices_physical_and_chemical = "";
	private String bodc_nerc = "";
	private String darwincore = "";
	

	// External.
	private String filter = "";
	private String synonyms = "";
	
	public String getHeader() {
		String header = "";
		header += "field" + "\t";
		header += "public_value" + "\t";
		header += "code" + "\t";
		header += "swedish" + "\t";
		header += "english" + "\t";
		header += "ices_biology" + "\t";
		header += "ices_physical_and_chemical" + "\t";
		header += "bodc_nerc" + "\t";
		header += "darwincore" + "\t";
		
		return header;
	}

	public String getRow() {
		String row = "";
		row += this.field + "\t";
		row += this.public_value + "\t";
		row += this.code + "\t";
		row += this.swedish + "\t";
		row += this.english + "\t";
		row += this.ices_biology + "\t";
		row += this.ices_physical_and_chemical + "\t";
		row += this.bodc_nerc + "\t";
		row += this.darwincore;
		
		return row;
	}

	public String getField() {
		return this.field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getPublicValue() {
		return this.public_value;
	}

	public String getCode() {
		return code;
	}

	public String getFieldAndPublicValue() {
		String fieldValue = "";
		fieldValue += this.field + "<+>";
		fieldValue += this.public_value;
		
		return fieldValue;
	}

	public String getSwedish() {
		return this.swedish;
	}

	public String getEnglish() {
		return this.english;
	}

	public String getSynonyms() {
		return this.synonyms;
	}

	public void parseRow(String[] importHeader, String[] importRow) {
		Integer index = 0;
		for (String headerItem : importHeader) {
			if (headerItem.equals("field")) {
				this.field = importRow[index].trim();
			}
			else if (headerItem.equals("public_value")) {
				this.public_value = importRow[index].trim();
			}
			else if (headerItem.equals("code")) {
				this.code = importRow[index].trim();
			}
			else if (headerItem.equals("swedish")) {
				this.swedish = importRow[index].trim();
			}
			else if (headerItem.equals("english")) {
				this.english = importRow[index].trim();
			}
			else if (headerItem.equals("ices_biology")) {
				this.ices_biology = importRow[index].trim();
			}
			else if (headerItem.equals("ices_physical_and_chemical")) {
				this.ices_physical_and_chemical = importRow[index].trim();
			}
			else if (headerItem.equals("bodc_nerc")) {
				this.bodc_nerc = importRow[index].trim();
			}
			else if (headerItem.equals("darwincore")) {
				this.darwincore = importRow[index].trim();
			}
			else if (headerItem.equals("filter")) {
				this.filter = importRow[index].trim();
			}
			else if (headerItem.equals("synonyms")) {
				this.synonyms = importRow[index].trim();
			}			
			
			index += 1;
		}
	}

}
