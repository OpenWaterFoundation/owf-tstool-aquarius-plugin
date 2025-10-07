package org.openwaterfoundation.tstool.plugin.aquarius.dao;

import java.util.Comparator;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;

/**
 * Comparator for Collections.sort to sort LocationDescription, currently by location identifier
 */
public class LocationDescriptionComparator implements Comparator<LocationDescription> {

	/**
	 * Constructor.
	 */
	public LocationDescriptionComparator () {
	}
	
	/**
	 * If locationDescriptionA is < locationDescriptionB, return -1.
	 * If locationDescriptionA = locationDescriptionB, return 0.
	 * If locationDescriptionA is > locationDescriptionB, return 1
	 * @param locationDescriptionA first LocationDescription to compare
	 * @param locationDescriptionB second LocationDescription to compare
	 */
	public int compare(LocationDescription locationDescriptionA, LocationDescription locationDescriptionB) {
		String identifierA = locationDescriptionA.getIdentifier();
		String identifierB = locationDescriptionB.getIdentifier();

		return identifierA.compareTo(identifierB);
	}
}