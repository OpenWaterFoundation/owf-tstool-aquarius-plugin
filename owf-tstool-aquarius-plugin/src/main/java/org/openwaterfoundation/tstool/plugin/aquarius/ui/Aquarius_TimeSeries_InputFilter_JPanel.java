// Aquarius_TimeSeries_InputFilter_JPanel - panel to filter time series queries

/* NoticeStart

OWF TSTool Aquarius Plugin
Copyright (C) 2025 Open Water Foundation

OWF TSTool Aquarius Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool Aquarius Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool Aquarius Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.aquarius.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openwaterfoundation.tstool.plugin.aquarius.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.aquarius.datastore.AquariusDataStore;

import RTi.Util.GUI.InputFilter;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;

/**
This class is an input filter for querying Aquarius web services.
*/
@SuppressWarnings("serial")
public class Aquarius_TimeSeries_InputFilter_JPanel extends InputFilter_JPanel {

	/**
	Test datastore, for connection.
	*/
	private AquariusDataStore datastore = null;

	/**
	Constructor for case when no datastore is configured - default panel.
	@param label label for the panel
	*/
	public Aquarius_TimeSeries_InputFilter_JPanel( String label ) {
		super(label);
	}

	/**
	Constructor.
	@param dataStore the data store to use to connect to the test database.  Cannot be null.
	@param numFilterGroups the number of filter groups to display
	*/
	public Aquarius_TimeSeries_InputFilter_JPanel( AquariusDataStore dataStore, int numFilterGroups ) {
	    super();
	    this.datastore = dataStore;
	    if ( this.datastore != null ) {
	        setFilters ( numFilterGroups );
	    }
	}

	/**
	Set the filter data.  This method is called at setup and when refreshing the list with a new subject type.
	For all cases, use the InputFilter constructor "whereLabelPersistent" to ensure that the TSTool ReadAquarius command will show a nice.
	*/
	public void setFilters ( int numFilterGroups ) {
		String routine = getClass().getSimpleName() + ".setFilters";
		
		// Read the data to populate filter choices.

		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();
		try {
			// By default all time series are included in the catalog:
			// - this allows providing query filters that are found in the time series list
			// - use the saved global data rather than rereading to improve performance
			tscatalogList = datastore.getTimeSeriesCatalog (false);
		}
		catch ( Exception e ) {
			Message.printWarning(2, routine, "Exception reading the Aquarius time series list");
			Message.printWarning(2, routine, e);
		}
		
		// The internal names for filters match the /tscatalog web service query parameters.
		// TODO smalers 2020-01-24 add more filters for points, point type, etc. as long as the web service API supports.

	    List<InputFilter> filters = new ArrayList<>();

	    // Always add blank to top of filter
	    filters.add(new InputFilter("", "", StringUtil.TYPE_STRING, null, null, false)); // Blank.

	    // Loop through the time series catalog records and extract unique values for filters.
	    List<String> locIdChoices = new ArrayList<>();
	    List<String> stationNameChoices = new ArrayList<>();
	    String locId = null;
	    //String stationName = null;
	    boolean found = false;
	    for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
	    	// Get the values for lists.
	    	locId = "" + tscatalog.getLocId();
	    	//stationName = tscatalog.getStationName();

	    	// Only add if not already in the lists.
	    	found = false;
	    	for ( String locId0 : locIdChoices ) {
	    		if ( locId.equals(locId0) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		locIdChoices.add("" + locId);
	    	}

	    	/*
	    	found = false;
	    	for ( String stationName0 : stationNameChoices ) {
	    		if ( stationName.equals(stationName0) ) {
	    			found = true;
	    			break;
	    		}
	    	}
	    	if ( !found ) {
	    		stationNameChoices.add(stationName);
	    	}
	    	*/
	    }

	    Collections.sort(locIdChoices,String.CASE_INSENSITIVE_ORDER);
	    InputFilter filter = new InputFilter("Location - ID",
	        "loc_id", "locId", "loc_id",
	        StringUtil.TYPE_STRING, locIdChoices, locIdChoices, false);
	    filter.removeConstraint(InputFilter.INPUT_GREATER_THAN);
	    filter.removeConstraint(InputFilter.INPUT_GREATER_THAN_OR_EQUAL_TO);
	    filter.removeConstraint(InputFilter.INPUT_LESS_THAN);
	    filter.removeConstraint(InputFilter.INPUT_LESS_THAN_OR_EQUAL_TO);
	    filters.add(filter);

	    /*
	    Collections.sort(stationNameChoices,String.CASE_INSENSITIVE_ORDER);
	    filters.add(new InputFilter("Station - Name",
            "station_name", "stationName", "station_name",
            StringUtil.TYPE_STRING, stationNameChoices, stationNameChoices, true));
            */

	  	setToolTipText("<html>Specify one or more input filters to limit query, will be ANDed.</html>");
	    
	    int numVisible = 14;
	    setInputFilters(filters, numFilterGroups, numVisible);
	}

	/**
	Return the data store corresponding to this input filter panel.
	@return the data store corresponding to this input filter panel.
	*/
	public AquariusDataStore getDataStore ( ) {
	    return this.datastore;
	}
}