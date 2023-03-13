/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.database;

import se.smhi.sharkadm.model.Dataset;
import se.smhi.sharkadm.model.ModelVisitor;
import se.smhi.sharkadm.model.Sample;
import se.smhi.sharkadm.model.Variable;
import se.smhi.sharkadm.model.Visit;

public class SaveModelToDatabase extends ModelVisitor {

	@Override
	public void visitDataset(Dataset dataset) {

		int datasetDbId = SaveModel.instance().insertDataset(dataset);
		dataset.setDataset_oid(datasetDbId);

		for (Visit visit : dataset.getVisits()) {
			visit.Accept(this);
		}
	}

	@Override
	public void visitVisit(Visit visit) {

		int visitDbId = SaveModel.instance().insertVisit(visit);
		visit.setVisit_oid(visitDbId);

		for (Sample sample : visit.getSamples()) {
			sample.Accept(this);
		}
	}

	@Override
	public void visitSample(Sample sample) {

		int sampleDbId = SaveModel.instance().insertSample(sample);
		sample.setSample_oid(sampleDbId);

		for (Variable variable : sample.getVariables()) {
			variable.Accept(this);
		}
	}

	@Override
	public void visitVariable(Variable variable) {

		// If the variable doesn't contain param/value/unit, it should not be
		// stored in database. This reduction is done late which makes it 
		// possible to search for errors during import.
		if ((variable.getParameter().equals(""))
				&& (variable.getValue().equals(""))
				&& (variable.getUnit().equals(""))) {
			return;
		}

		int variableDbId = SaveModel.instance().insertVariable(variable);
		variable.setVariable_oid(variableDbId);
	}
}
