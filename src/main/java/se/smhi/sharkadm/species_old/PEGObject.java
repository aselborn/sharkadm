/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

import se.smhi.sharkadm.utils.ConvUtils;

public class PEGObject {
	private String dyntaxaId;
	private String division = "";
	private String speciesClass = "";
	private String order = "";
	private String species = "";
	private String species_flag_code = "";
	private String stage = "";
	private String author = "";
	private String aphiaId = "";
	private String trophy = "";
	private String geometricShape = "";
	private String formula = "";
	private String sizeClassNo = "";
	private String unit = ""; 
	private String sizeRange = "";
	private String lengthL1 = "";
	private String lengthL2 = "";
	private String width = "";
	private String height = "";
	private String diameterD1 = "";
	private String diameterD2 = "";
	private String noOfCellsPerCountingUnit = "";
	private String calculatedVolume = "";
	private String comment = "";
	private String filament = "";
	private String calculatedCarbon = "";
	private String commentOnCarbonCalculation = "";

	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getCalculatedCarbon() {
		return this.calculatedCarbon;
	}
	public void setCalculatedCarbon(String calculatedCarbon) {
		this.calculatedCarbon = ConvUtils.convDecimal(calculatedCarbon);
	}
	public String getCalculatedVolume() {
		return calculatedVolume;
	}
	public void setCalculatedVolume(String calculatedVolume) {
		this.calculatedVolume = ConvUtils.convDecimal(calculatedVolume);
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getCommentOnCarbonCalculation() {
		return commentOnCarbonCalculation;
	}
	public void setCommentOnCarbonCalculation(String commentOnCarbonCalculation) {
		this.commentOnCarbonCalculation = commentOnCarbonCalculation;
	}
	public String getDiameterD1() {
		return diameterD1;
	}
	public void setDiameterD1(String diameterD1) {
		this.diameterD1 = ConvUtils.convDecimal(diameterD1);
	}
	public String getDiameterD2() {
		return diameterD2;
	}
	public void setDiameterD2(String diameterD2) {
		this.diameterD2 = ConvUtils.convDecimal(diameterD2);
	}
	public String getDivision() {
		return division;
	}
	public void setDivision(String division) {
		this.division = division;
	}
	public String getFilament() {
		return filament;
	}
	public void setFilament(String filament) {
		this.filament = filament;
	}
	public String getFormula() {
		return formula;
	}
	public void setFormula(String formula) {
		this.formula = formula;
	}
	public String getGeometricShape() {
		return geometricShape;
	}
	public void setGeometricShape(String geometricShape) {
		this.geometricShape = geometricShape;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = ConvUtils.convDecimal(height);
	}
	public String getLengthL1() {
		return lengthL1;
	}
	public void setLengthL1(String lengthL1) {
		this.lengthL1 = ConvUtils.convDecimal(lengthL1);
	}
	public String getLengthL2() {
		return lengthL2;
	}
	public void setLengthL2(String lengthL2) {
		this.lengthL2 = ConvUtils.convDecimal(lengthL2);
	}
	public String getNoOfCellsPerCountingUnit() {
		return noOfCellsPerCountingUnit;
	}
	public void setNoOfCellsPerCountingUnit(String noOfCellsPerCountingUnit) {
		this.noOfCellsPerCountingUnit = ConvUtils.convDecimal(noOfCellsPerCountingUnit);
	}
	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}
	public String getSpecies_flag_code() {
		return species_flag_code;
	}
	public void setSpecies_flag_code(String species_flag_code) {
		this.species_flag_code = species_flag_code;
	}
	public String getSizeClassNo() {
		return sizeClassNo;
	}
	public void setSizeClassNo(String sizeClassNo) {
		this.sizeClassNo = ConvUtils.convNoDecimal(sizeClassNo);
	}
	public String getSizeRange() {
		return sizeRange;
	}
	public void setSizeRange(String sizeRange) {
		this.sizeRange = sizeRange;
	}
	public String getSpecies() {
		return species;
	}
	public void setSpecies(String species) {
		this.species = species;
	}
	public String getSpeciesClass() {
		return speciesClass;
	}
	public void setSpeciesClass(String speciesClass) {
		this.speciesClass = speciesClass;
	}
	public String getStage() {
		return stage;
	}
	public void setStage(String stage) {
		this.stage = stage;
	}
	public String getDyntaxaId() {
		return dyntaxaId;
	}
	public void setDyntaxaId(String dyntaxaId) {
		this.dyntaxaId = dyntaxaId;
	}
	public String getTrophy() {
		return trophy;
	}
	public void setTrophy(String trophy) {
		this.trophy = trophy;
	}
	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getAphiaId() {
		return aphiaId;
	}
	public void setAphiaId(String aphiaId) {
		this.aphiaId = aphiaId;
	}

}
