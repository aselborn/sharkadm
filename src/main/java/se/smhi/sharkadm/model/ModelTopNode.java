/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.model;

import java.util.ArrayList;
import java.util.List;

import se.smhi.sharkadm.facades.ModelFacade;

/**
 * <p>Holds the list of imported file info objects. This is the top node in the
 * model element tree. 
 * </p>
 * <p>Singleton.
 * </p>
 */
public class ModelTopNode {

	private List<Dataset> datasetList = new ArrayList<Dataset>();

	private static ModelTopNode instance = new ModelTopNode(); // Singleton.

	private ModelTopNode() { // Singleton.
	}

	public static ModelTopNode instance() { // Singleton.
		return instance;
	}

	public void resetModel() {
		datasetList.clear();
		ModelFacade.instance().modelChanged();
	}

	public List<Dataset> getDatasetList() {
		return datasetList;
	}

	public void addDataset(Dataset dataset) {
		datasetList.add(dataset);
		ModelFacade.instance().modelChanged();
	}

	public void setImportComment() {
		// TODO Auto-generated method stub
	}
}
