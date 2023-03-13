/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

public abstract class ModelVisitor {

	public abstract void visitDataset(Dataset dataset);

	public abstract void visitVisit(Visit visit);

	public abstract void visitSample(Sample sample);

	public abstract void visitVariable(Variable variable);
	
}
