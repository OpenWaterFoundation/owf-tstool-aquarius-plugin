package org.openwaterfoundation.tstool.plugin.aquarius.dao;

import java.util.Comparator;

import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;

/**
 * Comparator for Collections.sort to sort TimeSeriesDescription, currently by location identifier and then data type.
 */
public class TimeSeriesDescriptionComparator implements Comparator<TimeSeriesDescription> {

	/**
	 * Constructor.
	 */
	public TimeSeriesDescriptionComparator () {
	}
	
	/**
	 * If timeSeriesDescriptionA is < timeSeriesDescriptionB, return -1.
	 * If timeSeriesDescriptionA = timeSeriesDescriptionB, return 0.
	 * If timeSeriesDescriptionA is > timeSeriesDescriptionB, return 1
	 */
	public int compare(TimeSeriesDescription timeSeriesDescriptionA, TimeSeriesDescription timeSeriesDescriptionB) {
		String identifierA = timeSeriesDescriptionA.getLocationIdentifier();
		String identifierB = timeSeriesDescriptionB.getLocationIdentifier();

		int compare = identifierA.compareTo(identifierB);
		if ( compare == 0 ) {
			// Need to compare the data type.
			String parameterIdA = timeSeriesDescriptionA.getParameterId();
			String parameterIdB = timeSeriesDescriptionB.getParameterId();
			return parameterIdA.compareTo(parameterIdB);
		}
		else {
			// Done with the comparison.
			return compare;
		}
	}
}