/*******************************************************************************
 * Copyright (c) 2014 Open Door Logistics (www.opendoorlogistics.com)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at http://www.gnu.org/licenses/lgpl.txt
 ******************************************************************************/
package com.opendoorlogistics.core.gis.map.tiled;

import gnu.trove.map.hash.TLongObjectHashMap;
import gnu.trove.procedure.TLongObjectProcedure;
import gnu.trove.procedure.TLongProcedure;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.List;

import com.opendoorlogistics.core.gis.map.data.DrawableObject;
import com.opendoorlogistics.core.gis.map.data.DrawableObjectImpl;
import com.opendoorlogistics.core.utils.Colours;
import com.opendoorlogistics.core.utils.strings.Strings;

public class CalculateChangedObjectsV2 {
	private TLongObjectHashMap<List<DrawableObject>> drawables = new TLongObjectHashMap<>();
	private TLongHashSet selected = new TLongHashSet();

	/**
	 * Get the list of objects whose selection state has changed
	 * 
	 * @param newSelected
	 * @return
	 */
	synchronized List<DrawableObject> updateSelected(final TLongHashSet newSelected) {

		final TLongHashSet changeset = new TLongHashSet();

		// get ids added to the selection
		newSelected.forEach(new TLongProcedure() {

			@Override
			public boolean execute(long id) {
				if (selected.contains(id) == false) {
					changeset.add(id);
				}
				return true;
			}
		});

		// get ids removed from the selection
		this.selected.forEach(new TLongProcedure() {

			@Override
			public boolean execute(long id) {
				if (newSelected.contains(id) == false) {
					changeset.add(id);
				}
				return true;
			}
		});

		// update internal list
		selected = new TLongHashSet(newSelected);

		// translate to a change set of objects
		final ArrayList<DrawableObject> ret = new ArrayList<>(changeset.size());
		changeset.forEach(new TLongProcedure() {

			@Override
			public boolean execute(long id) {
				List<DrawableObject> objs = drawables.get(id);
				if (objs != null) {
					ret.addAll(objs);
				}
				return true;
			}
		});

		return ret;
	}

	/**
	 * Get the list of object that has changed
	 * 
	 * @param objs
	 */
	synchronized List<DrawableObject> updateObjects(Iterable<? extends DrawableObject> objs) {

		final ArrayList<DrawableObject> ret = new ArrayList<>();

		// Take copy of input in a hashmap to compare with and replace our internal list.
		// Also check for any added objects.
		final TLongObjectHashMap<List<DrawableObject>> newMap = new TLongObjectHashMap<>();
		for (DrawableObject drawable : objs) {
			DrawableObject copy = new DrawableObjectImpl(drawable);
			List<DrawableObject> list = newMap.get(drawable.getGlobalRowId());
			if(list==null){
				list = new ArrayList<>(1);
				newMap.put(drawable.getGlobalRowId(), list);
			}
			list.add(copy);

			// is this new?
			if (drawables.containsKey(drawable.getGlobalRowId()) == false) {
				ret.add(copy);
			}
		}

		// Check for any removed or modified objects from our current objects
		drawables.forEachEntry(new TLongObjectProcedure<List<DrawableObject>>() {

			@Override
			public boolean execute(long id, List<DrawableObject> oldList) {
				List<DrawableObject> newList = newMap.get(id);
				
				// has this been deleted?
				if (newList == null) {
					ret.addAll(oldList);
				}
				else {
					boolean addBoth=false;
					
					if(oldList.size()!=newList.size()){
						// different sized lists.. must have changed
						addBoth = true;
					}
					
					if(!addBoth){
						if(oldList.size()== 1 && newList.size()==1){
							// special case where only one object in each.. easy to check
							addBoth = isDifferent(oldList.get(0), newList.get(0));
						}else{
							// match lists
							ArrayList<DrawableObject> newCopy = new ArrayList<>(newList);
							int n = oldList.size();
							for(int i =0 ; i<n && addBoth==false;i++){
								int n2 = newCopy.size();
								int match=-1;
								for(int j=0;j<n2;j++){
									if(isDifferent(oldList.get(i), newCopy.get(j))){
										match = j;
										break;
									}
								}
								
								if(match!=-1){
									// found match
									newCopy.remove(match);
								}else{
									addBoth = true;
								}
							}
						}
						
					}
					
					if(addBoth){
						ret.addAll(oldList);
						ret.addAll(newList);			
					}
				}


				return true;
			}
		});

		// replace internal list
		drawables = newMap;

		return ret;
	}

	private static boolean isDifferent(DrawableObject oldObj, DrawableObject newObj) {
		// has it been modified?
		boolean modified = false;

		// check position
		if (!modified) {
			modified = oldObj.getLatitude() != newObj.getLatitude() || oldObj.getLongitude() != newObj.getLongitude();
		}

		// check geometry (geometry is immutable so just check its the same object)
		if (!modified) {
			modified = oldObj.getGeometry() != newObj.getGeometry();
		}

		// check colour
		if (!modified) {
			modified = Colours.compare(oldObj.getColour(), newObj.getColour()) != 0;
		}

		// check colour key
		if (!modified) {
			modified = !Strings.equals(oldObj.getColourKey(), newObj.getColourKey());
		}

		// check draw outline
		if (!modified) {
			modified = oldObj.getDrawOutline() != newObj.getDrawOutline();
		}

		// image formula key
		if (!modified) {
			modified = !Strings.equals(oldObj.getImageFormulaKey(), newObj.getImageFormulaKey());
		}

		// legend key
		if (!modified) {
			modified = !Strings.equals(oldObj.getLegendKey(), newObj.getLegendKey());
		}

		// label
		if (!modified) {
			modified = !Strings.equals(oldObj.getLabel(), newObj.getLabel());
		}

		// font size
		if (!modified) {
			modified = oldObj.getFontSize() != newObj.getFontSize();
		}

		// pixel width
		if (!modified) {
			modified = oldObj.getPixelWidth() != newObj.getPixelWidth();
		}

		// opaque
		if (!modified) {
			modified = oldObj.getOpaque() != newObj.getOpaque();
		}
		return modified;
	}

}
