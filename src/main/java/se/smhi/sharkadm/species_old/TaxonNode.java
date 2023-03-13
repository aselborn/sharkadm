/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se 
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute. 
 */

package se.smhi.sharkadm.species_old;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaxonNode {
	// Taxonomic children list.
	private List<TaxonNode> children = new ArrayList<TaxonNode>(); 
	// Reference back to parent.
	TaxonNode parent = null;
	// Reference to the taxonomic object.
	TaxonObject taxonObject = null;
//	// Used to detect if the tree contains species or if it only contains higher levels.
//	Boolean containsSpecies = false;
	// PEG list.
	private List<PEGObject> pegList = new ArrayList<PEGObject>();
	
	public TaxonNode() { // Constructor.
	}
	
	public TaxonNode(TaxonNode parent) { // Constructor.
		this.parent = parent;
	}
	
	public List<TaxonNode> getChildren() {
		return children;
	}
	public void addChild(TaxonNode taxonNode) {
		this.children.add(taxonNode);
		taxonNode.setParent(this);
	}
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	public TaxonNode getParent() {
		return parent;
	}
	public void setParent(TaxonNode parent) {
		this.parent = parent;
	}
	public TaxonObject getTaxonObject() {
		return taxonObject;
	}
	public void setTaxonObject(TaxonObject taxonObject) {
		this.taxonObject = taxonObject;
		taxonObject.setTaxonNode(this);
	}
//	public Boolean getContainsSpecies() {
//		return containsSpecies;
//	}
//	public void setContainsSpecies() {
//		this.containsSpecies = true;
//		// Propagate this info to parents.
//		if (parent != null) {
//			parent.setContainsSpecies();
//		}
//	}
	
	// Converts the tree to a list.
	// Called from TaxonManager.getTaxonList().
	public void addChildrenToList(List<TaxonNode> taxonList) {
		for (TaxonNode child : children) {
			taxonList.add(child);
			child.addChildrenToList(taxonList);
		}
	}
	
	public void sortChildrenLists() {
		Collections.sort(children, new TaxonNameComparator());
		for (TaxonNode child : children) {
			child.sortChildrenLists();
		}
	}
	
	public void addPegObject(PEGObject pegObject) {
		this.pegList.add(pegObject);
	}

	public List<PEGObject> getPegList() {
		return pegList;
	}

	public PEGObject getPegObject(String sizeClass) {
		for (PEGObject pegObject : pegList) { // TODO This is not fast...
				if (pegObject.getSizeClassNo().equals(sizeClass)) {
					return pegObject;
			}
		}
		return null;
	}

}
