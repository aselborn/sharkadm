/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

import se.smhi.sharkadm.utils.ConvUtils;

public class Variable extends ModelElement {

	private Sample parent;

	// Note: CamelCase not used to make it easier to cut-n-paste.	
	private int variable_oid; // Used in database as primary key.

	// The subclass Community is removed. Replaced by this flag.
	private boolean isCommunity = false;
	
	public Variable(boolean isCommunity) {
		this.isCommunity = isCommunity;
	}

	public void copyData(Variable variable) {
		this.parent = variable.parent;
		for (String key : variable.getFieldKeys()) {
			addField(key, variable.getField(key));
		}
		for (String key : variable.getTempFieldKeys()) {
			addTempField(key, variable.getTempField(key));
		}
	}

	public Variable copyVariableAndData() {
		Variable newVariable;
		if (this.isCommunity()) {
			newVariable = new Variable(true); // Community data.
			newVariable.copyData(this);
		} else {
			newVariable = new Variable(false);
			newVariable.copyData(this);
		}
		return newVariable;
	}

	public void setParent(Sample sample) {
		this.parent = sample;
	}

	public Sample getParent() {
		return parent;
	}

	// Design pattern: Visitor.
	public void Accept(ModelVisitor visitor) {
		visitor.visitVariable(this);
	}

	public int getVariable_oid() {
		return variable_oid;
	}

	public void setVariable_oid(int variable_oid) {
		this.variable_oid = variable_oid;
	}

	public String getParameter() {
		return this.getField("variable.parameter");
	}

	public void setParameter(String parameter) {
		this.addField("variable.parameter", parameter);
	}

	public String getValue() {
		return this.getField("variable.value");
	}

	public String getValueAsDecimalPoint() {
		if (!this.containsField("variable.value")) {
			return "";
		}
		return this.getField("variable.value").replace(",", ".").replace(" ", "");
	}
	public Double getValueAsDouble() {
		Double valueDouble;
		String value = this.getField("variable.value");
		try {
			valueDouble = ConvUtils.convStringToDouble(value);
		} catch (NumberFormatException e) {
			return null;
		}
		return valueDouble;
	}

	public void setValue(String value) {
		// Check if value is numeric.
		try {
			String tmpValue = value.replace(",", ".");
			tmpValue = tmpValue.replace(" ", ""); // Remove space as 1000-delimiter.
			Double.parseDouble(tmpValue);
			this.addField("variable.value", tmpValue);
			return;
		} catch (Exception e) {
			// Pass.
		}  
		this.addField("variable.value", value);
	}

	public String getUnit() {
		return this.getField("variable.unit");
	}

	public void setUnit(String unit) {
		this.addField("variable.unit", unit);
	}


	public boolean containsTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			return true;
		}
		else if (parent.tempFieldMap.containsKey(key)) {
			return true;			
		}
		else if (parent.getParent().tempFieldMap.containsKey(key)) {
			return true;			
		}
		return false;
	}

	public String getTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			return tempFieldMap.get(key);
		}
		else if (parent.tempFieldMap.containsKey(key)) {
			return parent.tempFieldMap.get(key);			
		}
		else if (parent.getParent().tempFieldMap.containsKey(key)) {
			return parent.getParent().tempFieldMap.get(key);			
		}
		return "";
	}

	public void removeTempField(String key) {
		if (tempFieldMap.containsKey(key)) {
			tempFieldMap.remove(key);
		}
		else if (parent.tempFieldMap.containsKey(key)) {
			parent.tempFieldMap.remove(key);			
		}
		else if (parent.getParent().tempFieldMap.containsKey(key)) {
			parent.getParent().tempFieldMap.remove(key);			
		}
		
	}

	public String getDyntaxaId() {
		String dyntaxaId = this.getField("variable.dyntaxa_id");
		if (dyntaxaId.equals("")) {
			return "0";
		}
		return dyntaxaId;
	}

	public boolean isCommunity() {
		return isCommunity;
	}

	public void setCommunity(boolean isCommunity) {
		this.isCommunity = isCommunity;
	}

}
