// Aquarius_TimeSeries_TableModel - table model for the time series catalog

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

import java.util.List;

import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import org.openwaterfoundation.tstool.plugin.aquarius.datastore.AquariusDataStore;
import org.openwaterfoundation.tstool.plugin.aquarius.dao.TimeSeriesCatalog;

/**
This class is a table model for time series header information for Aquarius web services time series.
By default the sheet will contain row and column numbers.
*/
@SuppressWarnings({ "serial", "rawtypes" })
public class Aquarius_TimeSeries_TableModel extends JWorksheet_AbstractRowTableModel {
	
	/**
	Number of columns in the table model.
	*/
	private final int COLUMNS = 38;

	public final int COL_LOC_ID = 0;
	public final int COL_LOC_NAME = 1;
	public final int COL_DATA_SOURCE = 2;
	public final int COL_DATA_TYPE = 3;
	public final int COL_STATISTIC = 4;
	public final int COL_DATA_INTERVAL = 5;
	public final int COL_SCENARIO = 6;
	public final int COL_DATA_UNITS = 7;
	// Time series description data.
	public final int COL_TS_DESCRIPTION_IDENTIFIER = 8;
	public final int COL_TS_DESCRIPTION_UNIQUE_ID = 9;
	public final int COL_TS_DESCRIPTION_PARAMETER = 10;
	public final int COL_TS_DESCRIPTION_PARAMETER_ID = 11;
	public final int COL_TS_DESCRIPTION_UTC_OFFSET = 12;
	public final int COL_TS_DESCRIPTION_UTC_OFFSET_ISO_DURATION = 13;
	public final int COL_TS_DESCRIPTION_LAST_MODIFIED = 14;
	public final int COL_TS_DESCRIPTION_LAST_RAW_START_TIME = 15;
	public final int COL_TS_DESCRIPTION_LAST_RAW_END_TIME = 16;
	public final int COL_TS_DESCRIPTION_LAST_CORRECTED_START_TIME = 17;
	public final int COL_TS_DESCRIPTION_LAST_CORRECTED_END_TIME = 18;
	public final int COL_TS_DESCRIPTION_TIME_SERIES_TYPE = 19;
	public final int COL_TS_DESCRIPTION_LABEL = 20;
	public final int COL_TS_DESCRIPTION_COMMENT = 21;
	public final int COL_TS_DESCRIPTION_DESCRIPTION = 22;
	public final int COL_TS_DESCRIPTION_COMPUTATION_IDENTIFIER = 23;
	public final int COL_TS_DESCRIPTION_COMPUTATION_PERIOD_IDENTIFIER = 24;
	public final int COL_TS_DESCRIPTION_SUBLOCATION_IDENTIFIER = 25;
	// Location description data.
	public final int COL_LOC_DESCRIPTION_UNIQUE_ID = 26;
	public final int COL_LOC_DESCRIPTION_NAME = 27;
	public final int COL_LOC_DESCRIPTION_UTC_OFFSET = 28;
	public final int COL_LOC_LONGITUDE = 29;
	public final int COL_LOC_LATITUDE = 30;
	public final int COL_LOC_ELEVATION = 31;
	public final int COL_LOC_ELEVATION_UNITS = 32;
	// Parameter.
	public final int COL_PARAMETER_DISPLAY_NAME = 33;
	public final int COL_PARAMETER_ID = 34;
	//
	public final int COL_TS_ID = 35;
	public final int COL_PROBLEMS = 36;
	public final int COL_DATASTORE = 37;
	
	/**
	Datastore corresponding to datastore used to retrieve the data.
	*/
	AquariusDataStore datastore = null;

	/**
	Data are a list of TimeSeriesCatalog.
	*/
	private List<TimeSeriesCatalog> timeSeriesCatalogList = null;

	/**
	Constructor.  This builds the model for displaying the given Aquarius time series data.
	@param dataStore the data store for the data
	@param data the list of Aquarius TimeSeriesCatalog that will be displayed in the table.
	@throws Exception if an invalid results passed in.
	*/
	@SuppressWarnings("unchecked")
	public Aquarius_TimeSeries_TableModel ( AquariusDataStore dataStore, List<? extends Object> data ) {
		if ( data == null ) {
			_rows = 0;
		}
		else {
		    _rows = data.size();
		}
	    this.datastore = dataStore;
		_data = data; // Generic
		// TODO SAM 2016-04-17 Need to use instanceof here to check.
		this.timeSeriesCatalogList = (List<TimeSeriesCatalog>)data;
	}

	/**
	From AbstractTableModel.  Returns the class of the data stored in a given column.
	@param columnIndex the column for which to return the data class.
	*/
	@SuppressWarnings({ "unchecked" })
	public Class getColumnClass (int columnIndex) {
		switch (columnIndex) {
			// List in the same order as top of the class.
			case COL_LOC_LATITUDE: return Double.class;
			case COL_LOC_LONGITUDE: return Double.class;
			case COL_LOC_ELEVATION: return Double.class;
			default: return String.class; // All others.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of columns of data.
	@return the number of columns of data.
	*/
	public int getColumnCount() {
		return this.COLUMNS;
	}

	/**
	From AbstractTableMode.  Returns the name of the column at the given position.
	@return the name of the column at the given position.
	*/
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case COL_LOC_ID: return "Location ID";
			case COL_LOC_NAME: return "Location Name";
			case COL_DATA_SOURCE: return "Data Source";
			case COL_DATA_TYPE: return "Data Type";
			case COL_STATISTIC: return "Statistic";
			case COL_DATA_INTERVAL: return "Interval";
			case COL_SCENARIO: return "Scenario";
			case COL_DATA_UNITS: return "Units";
			// Time series description data.
			case COL_TS_DESCRIPTION_IDENTIFIER: return "TS Identifier";
			case COL_TS_DESCRIPTION_UNIQUE_ID: return "TS Unique ID";
			case COL_TS_DESCRIPTION_PARAMETER: return "TS Parameter";
			case COL_TS_DESCRIPTION_PARAMETER_ID: return "TS Parameter ID";
			case COL_TS_DESCRIPTION_UTC_OFFSET: return "TS UTC Offset";
			case COL_TS_DESCRIPTION_UTC_OFFSET_ISO_DURATION: return "TS Offset ISO Duration";
			case COL_TS_DESCRIPTION_LAST_MODIFIED: return "TS Last Modified";
			case COL_TS_DESCRIPTION_LAST_RAW_START_TIME: return "TS Raw Start";
			case COL_TS_DESCRIPTION_LAST_RAW_END_TIME: return "TS Raw End";
			case COL_TS_DESCRIPTION_LAST_CORRECTED_START_TIME: return "TS Corrected Start";
			case COL_TS_DESCRIPTION_LAST_CORRECTED_END_TIME: return "TS Corrected End";
			case COL_TS_DESCRIPTION_TIME_SERIES_TYPE: return "TS Type";
			case COL_TS_DESCRIPTION_LABEL: return "TS Label";
			case COL_TS_DESCRIPTION_COMMENT: return "TS Comment";
			case COL_TS_DESCRIPTION_DESCRIPTION: return "TS Description";
			case COL_TS_DESCRIPTION_COMPUTATION_IDENTIFIER: return "TS Computation ID";
			case COL_TS_DESCRIPTION_COMPUTATION_PERIOD_IDENTIFIER: return "TS Computational Period ID";
			case COL_TS_DESCRIPTION_SUBLOCATION_IDENTIFIER: return "TS Sublocation ID";
			// Location data.
			case COL_LOC_DESCRIPTION_UNIQUE_ID: return "Location Unique ID";
			case COL_LOC_DESCRIPTION_NAME: return "Location Name";
			case COL_LOC_DESCRIPTION_UTC_OFFSET: return "Location UTC Offset";
			case COL_LOC_LONGITUDE: return "Longitude";
			case COL_LOC_LATITUDE: return "Latitude";
			case COL_LOC_ELEVATION: return "Elevation";
			case COL_LOC_ELEVATION_UNITS: return "Elevation Units";
			// Paramea.
			case COL_PARAMETER_DISPLAY_NAME: return "Parameter Display name";
			case COL_PARAMETER_ID: return "Parameter ID";
			// Other general data.
			case COL_TS_ID: return "TSTool TSID";
			case COL_PROBLEMS: return "Problems";
			case COL_DATASTORE: return "Datastore";

			default: return "";
		}
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public String[] getColumnToolTips() {
	    String[] toolTips = new String[this.COLUMNS];
	    toolTips[COL_LOC_ID] = "Location identifier - Aquarius location identifier";
	    toolTips[COL_LOC_NAME] = "Location name";
	    toolTips[COL_DATA_SOURCE] = "Data source";
	    toolTips[COL_DATA_TYPE] = "Time series data type (parameter ID)";
	    toolTips[COL_STATISTIC] = "Time series data type statistic for interval data (from computational)";
	    toolTips[COL_DATA_INTERVAL] = "Time series data interval (from computation period)";
	    toolTips[COL_SCENARIO] = "Scneario to help uniquely identify time series";
	    toolTips[COL_DATA_UNITS] = "Time series data value units abbreviation";
	    // Time series description data.
		toolTips[COL_TS_DESCRIPTION_IDENTIFIER] = "Time series identifier";
		toolTips[COL_TS_DESCRIPTION_UNIQUE_ID] = "Time series unique ID";
		toolTips[COL_TS_DESCRIPTION_PARAMETER] = "Time series parameter (also used for data type)";
		toolTips[COL_TS_DESCRIPTION_PARAMETER_ID] = "Time series parameter ID";
		toolTips[COL_TS_DESCRIPTION_UTC_OFFSET] = "Time series UTC offset";
		toolTips[COL_TS_DESCRIPTION_UTC_OFFSET_ISO_DURATION] = "Time series Offset ISO duration";
		toolTips[COL_TS_DESCRIPTION_LAST_MODIFIED] = "Time series last modified";
		toolTips[COL_TS_DESCRIPTION_LAST_RAW_START_TIME] = "Time series raw start";
		toolTips[COL_TS_DESCRIPTION_LAST_RAW_END_TIME] = "Time series raw end";
		toolTips[COL_TS_DESCRIPTION_LAST_CORRECTED_START_TIME] = "Time series corrected start";
		toolTips[COL_TS_DESCRIPTION_LAST_CORRECTED_END_TIME] = "Time series corrected end";
		toolTips[COL_TS_DESCRIPTION_TIME_SERIES_TYPE] = "Time series type";
		toolTips[COL_TS_DESCRIPTION_LABEL] = "Time series label";
		toolTips[COL_TS_DESCRIPTION_COMMENT] = "Time series comment";
		toolTips[COL_TS_DESCRIPTION_DESCRIPTION] = "Time series description";
		toolTips[COL_TS_DESCRIPTION_COMPUTATION_IDENTIFIER] = "Time series computation ID (used to determine the statistic)";
		toolTips[COL_TS_DESCRIPTION_COMPUTATION_PERIOD_IDENTIFIER] = "Time series computational period ID (used to determine the data interval)";
		toolTips[COL_TS_DESCRIPTION_SUBLOCATION_IDENTIFIER] = "Time series sublocation ID";
		// Location data.
		toolTips[COL_LOC_DESCRIPTION_UNIQUE_ID] = "Location Unique ID";
		toolTips[COL_LOC_DESCRIPTION_NAME] = "Location Name";
		toolTips[COL_LOC_DESCRIPTION_UTC_OFFSET] = "Location UTC Offset";
	    toolTips[COL_LOC_LONGITUDE] = "Location longitude, decimal degrees";
	    toolTips[COL_LOC_LATITUDE] = "Location latitude, decimal degrees";
	    toolTips[COL_LOC_ELEVATION] = "Location elevation";
		toolTips[COL_LOC_ELEVATION_UNITS] = "Location elevation units";
	    // Parameter data.
		toolTips[COL_PARAMETER_DISPLAY_NAME ] = "Parameter display name";
		toolTips[COL_PARAMETER_ID ] = "Parameter ID";
		// Other general data.
		toolTips[COL_TS_ID] = "Time series identifier";
		toolTips[COL_PROBLEMS] = "Problems";
		toolTips[COL_DATASTORE] = "Datastore name";
	    return toolTips;
	}

	/**
	Returns an array containing the column widths (in number of characters).
	@return an integer array containing the widths for each field.
	*/
	public int[] getColumnWidths() {
		int[] widths = new int[this.COLUMNS];
	    widths[COL_LOC_ID] = 25;
	    widths[COL_LOC_NAME] = 30;
	    widths[COL_DATA_SOURCE] = 10;
	    widths[COL_DATA_TYPE] = 20;
	    widths[COL_STATISTIC] = 8;
	    widths[COL_DATA_INTERVAL] = 8;
	    widths[COL_SCENARIO] = 10;
	    widths[COL_DATA_UNITS] = 6;
	    // Time series description data.
		widths[COL_TS_DESCRIPTION_IDENTIFIER] = 30;
		widths[COL_TS_DESCRIPTION_UNIQUE_ID] = 25;
		widths[COL_TS_DESCRIPTION_PARAMETER] = 11;
		widths[COL_TS_DESCRIPTION_PARAMETER_ID] = 11;
		widths[COL_TS_DESCRIPTION_UTC_OFFSET] = 10;
		widths[COL_TS_DESCRIPTION_UTC_OFFSET_ISO_DURATION] = 15;
		widths[COL_TS_DESCRIPTION_LAST_MODIFIED] = 20;
		widths[COL_TS_DESCRIPTION_LAST_RAW_START_TIME] = 15;
		widths[COL_TS_DESCRIPTION_LAST_RAW_END_TIME] = 15;
		widths[COL_TS_DESCRIPTION_LAST_CORRECTED_START_TIME] = 15;
		widths[COL_TS_DESCRIPTION_LAST_CORRECTED_END_TIME] = 15;
		widths[COL_TS_DESCRIPTION_TIME_SERIES_TYPE] = 10;
		widths[COL_TS_DESCRIPTION_LABEL] = 20;
		widths[COL_TS_DESCRIPTION_COMMENT] = 30;
		widths[COL_TS_DESCRIPTION_DESCRIPTION] = 40;
		widths[COL_TS_DESCRIPTION_COMPUTATION_IDENTIFIER] = 15;
		widths[COL_TS_DESCRIPTION_COMPUTATION_PERIOD_IDENTIFIER] = 20;
		widths[COL_TS_DESCRIPTION_SUBLOCATION_IDENTIFIER] = 15;
		// Location data.
		widths[COL_LOC_DESCRIPTION_UNIQUE_ID] = 25;
		widths[COL_LOC_DESCRIPTION_NAME] = 20;
		widths[COL_LOC_DESCRIPTION_UTC_OFFSET] = 13;
	    widths[COL_LOC_LONGITUDE] = 7;
	    widths[COL_LOC_LATITUDE] = 6;
	    widths[COL_LOC_ELEVATION] = 6;
	    widths[COL_LOC_ELEVATION_UNITS] = 10;
		// Parameter data.
		widths[COL_PARAMETER_DISPLAY_NAME ] = 20;
		widths[COL_PARAMETER_ID ] = 10;
		// Other general data.
	    widths[COL_TS_ID] = 50;
		widths[COL_PROBLEMS] = 30;
		widths[COL_DATASTORE] = 20;
		return widths;
	}

	/**
	Returns the format to display the specified column.
	@param column column for which to return the format.
	@return the format (as used by StringUtil.formatString()).
	*/
	public String getFormat ( int column ) {
		switch (column) {
			case COL_LOC_LONGITUDE: return "%.6f";
			case COL_LOC_LATITUDE: return "%.6f";
			case COL_LOC_ELEVATION: return "%.2f";
			default: return "%s"; // All else are strings.
		}
	}

	/**
	From AbstractTableMode.  Returns the number of rows of data in the table.
	*/
	public int getRowCount() {
		return _rows;
	}

	/**
	From AbstractTableMode.  Returns the data that should be placed in the JTable at the given row and column.
	@param row the row for which to return data.
	@param col the column for which to return data.
	@return the data that should be placed in the JTable at the given row and column.
	*/
	public Object getValueAt(int row, int col) {
		// Make sure the row numbers are never sorted.
		if (_sortOrder != null) {
			row = _sortOrder[row];
		}

		TimeSeriesCatalog timeSeriesCatalog = this.timeSeriesCatalogList.get(row);
		switch (col) {
			// OK to allow null because will be displayed as blank.
			case COL_LOC_ID: return timeSeriesCatalog.getLocId();
			case COL_LOC_NAME: return timeSeriesCatalog.getLocationDescriptionName();
			case COL_DATA_SOURCE: return timeSeriesCatalog.getDataSource();
			case COL_DATA_TYPE: return timeSeriesCatalog.getDataType();
			case COL_STATISTIC: return timeSeriesCatalog.getStatistic();
			case COL_DATA_INTERVAL: return timeSeriesCatalog.getDataInterval();
			case COL_SCENARIO: return timeSeriesCatalog.getScenario();
			case COL_DATA_UNITS: return timeSeriesCatalog.getDataUnits();
			// Time series description data.
			case COL_TS_DESCRIPTION_IDENTIFIER: return timeSeriesCatalog.getTimeSeriesDescriptionIdentifier();
			case COL_TS_DESCRIPTION_UNIQUE_ID: return timeSeriesCatalog.getTimeSeriesDescriptionUniqueId();
			case COL_TS_DESCRIPTION_PARAMETER: return timeSeriesCatalog.getTimeSeriesDescriptionParameter();
			case COL_TS_DESCRIPTION_PARAMETER_ID: return timeSeriesCatalog.getTimeSeriesDescriptionParameterId();
			case COL_TS_DESCRIPTION_UTC_OFFSET: return timeSeriesCatalog.getTimeSeriesDescriptionUtcOffset();
			case COL_TS_DESCRIPTION_UTC_OFFSET_ISO_DURATION: return timeSeriesCatalog.getTimeSeriesDescriptionUtcOffsetIsoDuration();
			case COL_TS_DESCRIPTION_LAST_MODIFIED:
				return timeSeriesCatalog.getTimeSeriesDescriptionLastModified() == null ? null : timeSeriesCatalog.getTimeSeriesDescriptionLastModified().toString();
			case COL_TS_DESCRIPTION_LAST_RAW_START_TIME:
				return timeSeriesCatalog.getTimeSeriesDescriptionRawStartTime() == null ? null : timeSeriesCatalog.getTimeSeriesDescriptionRawStartTime().toString();
			case COL_TS_DESCRIPTION_LAST_RAW_END_TIME:
				return timeSeriesCatalog.getTimeSeriesDescriptionRawEndTime() == null ? null : timeSeriesCatalog.getTimeSeriesDescriptionRawEndTime().toString();
			case COL_TS_DESCRIPTION_LAST_CORRECTED_START_TIME:
				return timeSeriesCatalog.getTimeSeriesDescriptionCorrectedStartTime() == null ? null : timeSeriesCatalog.getTimeSeriesDescriptionCorrectedStartTime().toString();
			case COL_TS_DESCRIPTION_LAST_CORRECTED_END_TIME:
				return timeSeriesCatalog.getTimeSeriesDescriptionCorrectedEndTime() == null ? null : timeSeriesCatalog.getTimeSeriesDescriptionCorrectedEndTime().toString();
			case COL_TS_DESCRIPTION_TIME_SERIES_TYPE: return timeSeriesCatalog.getTimeSeriesDescriptionTimeSeriesType();
			case COL_TS_DESCRIPTION_LABEL: return timeSeriesCatalog.getTimeSeriesDescriptionLabel();
			case COL_TS_DESCRIPTION_COMMENT: return timeSeriesCatalog.getTimeSeriesDescriptionComment();
			case COL_TS_DESCRIPTION_DESCRIPTION: return timeSeriesCatalog.getTimeSeriesDescriptionDescription();
			case COL_TS_DESCRIPTION_COMPUTATION_IDENTIFIER: return timeSeriesCatalog.getTimeSeriesDescriptionComputationIdentifier();
			case COL_TS_DESCRIPTION_COMPUTATION_PERIOD_IDENTIFIER: return timeSeriesCatalog.getTimeSeriesDescriptionComputationPeriodIdentifier();
			case COL_TS_DESCRIPTION_SUBLOCATION_IDENTIFIER: return timeSeriesCatalog.getTimeSeriesDescriptionSubLocationIdentifier();
			// Location data.
			case COL_LOC_DESCRIPTION_UNIQUE_ID: return timeSeriesCatalog.getLocationDescriptionUniqueId();
			case COL_LOC_DESCRIPTION_NAME: return timeSeriesCatalog.getLocationDescriptionName();
			case COL_LOC_DESCRIPTION_UTC_OFFSET: return timeSeriesCatalog.getLocationDescriptionUtcOffset();
			case COL_LOC_LONGITUDE: return timeSeriesCatalog.getLocationDataLongitude();
			case COL_LOC_LATITUDE: return timeSeriesCatalog.getLocationDataLatitude();
			case COL_LOC_ELEVATION: return timeSeriesCatalog.getLocationDataElevation();
			case COL_LOC_ELEVATION_UNITS: return timeSeriesCatalog.getLocationDataElevationUnits();
			// Parameter data.
			case COL_PARAMETER_DISPLAY_NAME: return timeSeriesCatalog.getParameterMetadataDisplayName();
			case COL_PARAMETER_ID: return timeSeriesCatalog.getParameterMetadataIdentifier();
			// Other general data.
			case COL_TS_ID: return timeSeriesCatalog.getTsId();
			case COL_PROBLEMS: return timeSeriesCatalog.formatProblems();			
			case COL_DATASTORE: return this.datastore.getName();			
			default: return "";
		}
	}

}