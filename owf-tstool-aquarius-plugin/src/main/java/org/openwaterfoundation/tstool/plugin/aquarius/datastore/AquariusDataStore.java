// AquariusDataStore - class that implements the AquariusDataStore plugin datastore

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

package org.openwaterfoundation.tstool.plugin.aquarius.datastore;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aquaticinformatics.aquarius.sdk.timeseries.AquariusClient;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.LocationDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.ParameterMetadata;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataCorrectedServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataRawServiceRequest;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDataServiceResponse;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesDescription;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesPoint;
import com.aquaticinformatics.aquarius.sdk.timeseries.servicemodels.Publish.TimeSeriesUniqueIds;

import org.openwaterfoundation.tstool.plugin.aquarius.PluginMeta;
import org.openwaterfoundation.tstool.plugin.aquarius.dao.TimeSeriesCatalog;
import org.openwaterfoundation.tstool.plugin.aquarius.dao.TimeSeriesDescriptionComparator;
import org.openwaterfoundation.tstool.plugin.aquarius.ui.Aquarius_TimeSeries_CellRenderer;
import org.openwaterfoundation.tstool.plugin.aquarius.ui.Aquarius_TimeSeries_InputFilter_JPanel;
import org.openwaterfoundation.tstool.plugin.aquarius.ui.Aquarius_TimeSeries_TableModel;
import org.openwaterfoundation.tstool.plugin.aquarius.util.TimeUtil;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.TS.TSUtil;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequirementCheck;
import RTi.Util.Message.Message;
import RTi.Util.String.StringUtil;
import RTi.Util.Time.DateTime;
import RTi.Util.Time.TimeInterval;
import riverside.datastore.AbstractWebServiceDataStore;
import riverside.datastore.DataStoreRequirementChecker;
import riverside.datastore.PluginDataStore;

public class AquariusDataStore extends AbstractWebServiceDataStore implements DataStoreRequirementChecker, PluginDataStore {

	/**
	 * Standard request parameters:
	 * - currently nothing
	 */
	//private final String COMMON_REQUEST_PARAMETERS = "?something=something"
	//private final String COMMON_REQUEST_PARAMETERS = "";
	
	/**
	 * The data source to use for time series.
	 */
	private final String TS_DATA_SOURCE = "Aquarius";

	/**
	 * The Aquarius client that will be used for API requests.
	 */
    private AquariusClient client = null;

	/**
	 * Properties for the plugin, used to help with application integration.
	 */
	private Map<String,Object> pluginProperties = new LinkedHashMap<>();

	/**
	 * Cached location description list.
	 */
	List<LocationDescription> locationDescriptionList = new ArrayList<>();

	/**
	 * Cached location data list.
	 */
	List<Publish.LocationDataServiceResponse> locationDataList = new ArrayList<>();

	/**
	 * Cached parameter metadata list.
	 */
	List<ParameterMetadata> parameterMetadataList = new ArrayList<>();

	/**
	 * Cached station list.
	 */
	//List<Station> stationList = new ArrayList<>();

	/**
	 * Cached time series catalog, used to streamline creating lists for UI choices.
	 */
	private List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();

	/**
	 * Cached time series description list.
	 */
	List<TimeSeriesDescription> timeSeriesDescriptionList = new ArrayList<>();

	/**
	 * Cached time series unique ID list.
	 */
	List<TimeSeriesUniqueIds> timeSeriesUniqueIdList = new ArrayList<>();

	/**
	 * Authentication token.
	 */
	//private Token token = null;

	/**
	 * Global debug option for datastore, used for development and troubleshooting.
	 */
	private boolean debug = false;

	/**
	Constructor for web service.
	@param name identifier for the data store (will be used in commands)
	@param description name for the data store
	@param serviceRootUrl the service root URL to which specific requests will be appended, must have a trailing slash
	@param props properties to configure the datastore:
	<ul>
	<li> `Description` - description, longer than name</li>
	<li> `Enabled` - standard datastore property, indicated whether it is enabled</li>
	<li> `Name` - name of the datastore, same as name</li>
	<li> `OrganizationId` - the Aquarius organization ID</li>
	<li> `Password` - Aquarius account password</li>
	<li> `ServiceApiDocumentationUrl` - URL for the API documentation landing page</li>
	<li> `ServiceRootUrl` - the URL for the web service API, for example "ServiceRootUrl = "https://panama.aquaticinformatics.net"</li>
	<li> `Type` - must be `AquariusDataStore`</li>
	<li> `UserName` - Aquarius account user name</li>
	</ul>
	*/
	public AquariusDataStore ( String name, String description, URI serviceRootUrl, PropList props ) {
		String routine = getClass().getSimpleName() + ".AquariusDataStore";

		String prop = props.getValue("Debug");
		if ( (prop != null) && prop.equalsIgnoreCase("true") ) {
			Message.printStatus(2, routine, "Datastore \"" + name + "\" - detected Debug=true");
			this.debug = true;
		}
	    setName ( name );
	    setDescription ( description );
	    setServiceRootURI ( serviceRootUrl );
	    setProperties ( props );

	    // Set standard plugin properties:
        // - plugin properties can be listed in the main TSTool interface
        // - version is used to create a versioned installer and documentation.
        this.pluginProperties.put("Name", "Open Water Foundation Aquarius web services plugin");
        this.pluginProperties.put("Description", "Plugin to integrate TSTool with Aquarius web services.");
        this.pluginProperties.put("Author", "Open Water Foundation, https://openwaterfoundation.org");
        this.pluginProperties.put("Version", PluginMeta.VERSION);

        // Create the client that will be used for the session.
        createClient ();

	    // Read global data used throughout the session:
	    // - in particular a cache of the TimeSeriesCatalog used for further queries

	    readGlobalData();
	}

	/**
 	* Check the database requirement for DataStoreRequirementChecker interface, for example one of:
 	* <pre>
 	* @require datastore Aquarius-WET version >= 1.5.5
 	* @require datastore Aquarius-WET ?configproperty propname? == Something
 	* @require datastore Aquarius-WET configuration system_id == CO-District-MHFD
 	*
 	* @enabledif datastore nsdataws-mhfd version >= 1.5.5
 	* </pre>
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		String routine = getClass().getSimpleName() + ".checkRequirement";
		// Parse the string into parts:
		// - calling code has already interpreted the first 3 parts to be able to do this call
		String requirement = check.getRequirementText();
		Message.printStatus(2, routine, "Checking requirement: " + requirement);
		// Get the annotation that is being checked, so messages are appropriate.
		String annotation = check.getAnnotation();
		String [] requireParts = requirement.split(" ");
		// Datastore name may be an original name but a substitute is used, via TSTool command line.
		String dsName = requireParts[2];
		String dsNameNote = ""; // Note to add on messages to help confirm how substitutions are being handled.
		String checkerName = "CloudFrontDataStore";
		if ( !dsName.equals(this.getName())) {
			// A substitute datastore name is being used, such as in testing.
			dsNameNote = "\nCommand file datastore name '" + dsName + "' substitute that is actually used is '" + this.getName() + "'";
		}
		if ( requireParts.length < 4 ) {
			check.setIsRequirementMet(checkerName, false, "Requirement does not contain check type as one of: version, configuration, "
				+ "for example: " + annotation + " datastore nsdataws-mhfd version...");
			return check.isRequirementMet();
		}
		String checkType = requireParts[3];
		if ( checkType.equalsIgnoreCase("configuration") ) {
			// Checking requirement of form:
			// 0        1         2             3             4         5  6
			// @require datastore nsdataws-mhfd configuration system_id == CO-District-MHFD
			String propertyName = requireParts[4];
			String operator = requireParts[5];
			String checkValue = requireParts[6];
			// Get the configuration table property of interest:
			// - currently only support checking system_id
			if ( propertyName.equals("system_id") ) {
				// Know how to handle "system_id" property.
				if ( (checkValue == null) || checkValue.isEmpty() ) {
					// Unable to do check.
					check.setIsRequirementMet ( checkerName, false, "'system_id' value to check is not specified in the requirement." + dsNameNote );
					return check.isRequirementMet();
				}
				else {
					// TODO smalers 2023-01-03 need to evaluate whether Aquarius has configuration properties.
					//String propertyValue = readConfigurationProperty(propertyName);
					String propertyValue = "";
					if ( (propertyValue == null) || propertyValue.isEmpty() ) {
						// Unable to do check.
						check.setIsRequirementMet ( checkerName, false, "Aquarius configuration 'system_id' value is not defined in the database." + dsNameNote );
						return check.isRequirementMet();
					}
					else {
						if ( StringUtil.compareUsingOperator(propertyValue, operator, checkValue) ) {
							check.setIsRequirementMet ( checkerName, true, "Aquarius configuration property '" + propertyName + "' value (" + propertyValue +
								") does meet the requirement: " + operator + " " + checkValue + dsNameNote );
						}
						else {
							check.setIsRequirementMet ( checkerName, false, "Aquarius configuration property '" + propertyName + "' value (" + propertyValue +
								") does not meet the requirement:" + operator + " " + checkValue + dsNameNote );
						}
						return check.isRequirementMet();
					}
				}
			}
			else {
				// Other properties may not be easy to compare.  Probably need to use "contains" and other operators.
				check.setIsRequirementMet ( checkerName, false, "Check type '" + checkType + "' configuration property '" + propertyName + "' is not supported.");
				return check.isRequirementMet();
			}
		}
		/* TODO smalers 2021-07-29 need to implement, maybe need to define the system ID in the configuration file as a cross check for testing.
		else if ( checkType.equalsIgnoreCase("configproperty") ) {
			if ( parts.length < 7 ) {
				// 'property' requires 7 parts
				throw new RuntimeException( "'configproperty' requirement does not contain at least 7 parts for: " + requirement);
			}
		}
		*/
		else if ( checkType.equalsIgnoreCase("version") ) {
			// Checking requirement of form:
			// 0        1         2             3       4  5
			// @require datastore nsdataws-mhfd version >= 1.5.5
			Message.printStatus(2, routine, "Checking web service version.");
			// Do a web service round trip to check version since it may change with software updates.
			String wsVersion = readVersion();
			if ( (wsVersion == null) || wsVersion.isEmpty() ) {
				// Unable to do check.
				check.setIsRequirementMet ( checkerName, false, "Web service version is unknown (services are down or software problem).");
				return check.isRequirementMet();
			}
			else {
				// Web service versions are strings of format A.B.C.D so can do semantic version comparison:
				// - only compare the first 3 parts
				//Message.printStatus(2, "checkRequirement", "Comparing " + wsVersion + " " + operator + " " + checkValue);
				String operator = requireParts[4];
				String checkValue = requireParts[5];
				boolean verCheck = StringUtil.compareSemanticVersions(wsVersion, operator, checkValue, 3);
				String message = "";
				if ( !verCheck ) {
					message = annotation + " web service version (" + wsVersion + ") does not meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				else {
					message = annotation + " web service version (" + wsVersion + ") does meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				return check.isRequirementMet();
			}
		}
		else {
			// Unknown check type.
			check.setIsRequirementMet ( checkerName, false, "Requirement check type '" + checkType + "' is unknown.");
			return check.isRequirementMet();
		}

	}

	/**
	 * Check whether the authentication token is expired if if so, refresh the token.
	 */
	private void checkTokenExpiration() {
		/*
		if ( this.token.isExpired() ) {
			// Create a new token
			createAuthenticationToken();
		}
		*/
	}

	/**
	 * Create the client for API requests.
	 */
    private void createClient () {
    	String routine = getClass().getSimpleName() + ".createClient";
    	
    	String server = getProperty("ServiceRootUrl");
        String username = getProperty("UserName");
        String password = getProperty("Password");
        // Uncomment this for development only.
        Message.printStatus(2,routine,"Creating Aquarius client for server=\"" + server + "\" username=\"" + username + "\" password=\"" + password + "\".");
        if ( (server == null) || server.isEmpty() ) {
        	Message.printWarning(2,routine,"The datastore configuration ServiceRootUrl property is not defined.");
        }
        else {
        	// Remove the trailing / from the server.
        	if ( server.endsWith("/") ) {
        		server = server.substring(0, (server.length() - 1));
        	}
        }
        if ( (username == null) || username.isEmpty() ) {
        	Message.printWarning(2,routine,"The datastore configuration UserName property is not defined.");
        }
        if ( (password == null) || password.isEmpty() ) {
        	Message.printWarning(2,routine,"The datastore configuration Password property is not defined.");
        }

        try {
        	this.client = AquariusClient.createConnectedClient(server, username, password);
        }
        catch ( Exception e ) {
        	Message.printWarning(2, routine, "There was an error creating the Aquarius client.");
        	Message.printWarning(2, routine, e );
        }

        	/*
            // Example: list all time series descriptions
            TimeSeriesDescriptionListServiceResponse descs = client.Get(new GetTimeSeriesDescriptionListServiceRequest());
            descs.getTimeSeriesDescriptions().forEach(ts ->
                System.out.println(ts.getIdentifier() + " : " + ts.getLabel())
            );

            // Example: write new points
            PostTimeSeriesDataServiceRequest req = new PostTimeSeriesDataServiceRequest();
            req.setTimeSeriesUniqueId("your-timeseries-guid");
            
            TimeSeriesPoint pt = new TimeSeriesPoint();
            pt.setTime("2025-10-01T12:00:00Z");
            pt.setValue(42.0);
            pt.setGradeCode(10);

            req.getPoints().add(pt);

            client.Post(req); // submit data
            */
    }

	/**
	 * Create a time series input filter, used to initialize user interfaces.
	 * @return a time series input filter for Aquarius time series catalog queries
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel () {
		Aquarius_TimeSeries_InputFilter_JPanel ifp = new Aquarius_TimeSeries_InputFilter_JPanel(this, 4);
		return ifp;
	}

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp ) {
		// First query the database for the specified input.
		List<TimeSeriesCatalog> tsmetaList = readTimeSeriesMeta ( dataType, timeStep, ifp );
		return getTimeSeriesListTableModel(tsmetaList);
	}

	/**
	 * Find a location data object given the location identifier.
	 * @param locationDataList the list of location data to search
	 * @param locationIdentifier location identifier to match.
	 * @return the location data that matches the requested location identifier, or null if not found
	 */
	private Publish.LocationDataServiceResponse findLocationDataForLocationIdentifier (
		List<Publish.LocationDataServiceResponse> locationDataList, String locationIdentifier ) {
		Publish.LocationDataServiceResponse foundLoc = null;
		if ( locationIdentifier == null ) {
			return null;
		}
		if ( locationDataList != null ) {
			for ( Publish.LocationDataServiceResponse locationData : locationDataList ) {
				if ( locationData.getIdentifier().equals(locationIdentifier) ) {
					foundLoc = locationData;
					break;
				}
			}
		}
		return foundLoc;
	}

	/**
	 * Find a location description object given the location identifier.
	 * @param locationDescriptions the list of location descriptions to search
	 * @param locationIdentifier location identifier to match.
	 * @return the location description that matches the requested location identifier, or null if not found
	 */
	public LocationDescription findLocationDescriptionForLocationIdentifier ( List<LocationDescription> locationDescriptions, String locationIdentifier ) {
		LocationDescription foundLoc = null;
		if ( locationIdentifier == null ) {
			return null;
		}
		if ( locationDescriptions != null ) {
			for ( LocationDescription locationDescription : locationDescriptions ) {
				if ( locationDescription.getIdentifier().equals(locationIdentifier) ) {
					foundLoc = locationDescription;
					break;
				}
			}
		}
		return foundLoc;
	}

	/**
	 * Find a parameter metadata object the parameter.
	 * @param parameterMetadataList the list of parameter metadata to search
	 * @param parameterid parameter to match.
	 * @return the parameter metadata object that matches the requested parameter , or null if not found
	 */
	private ParameterMetadata findParameterMetadataForParameter ( List<ParameterMetadata> parameterMetadataList, String parameter ) {
		ParameterMetadata foundParam = null;
		if ( parameter == null ) {
			return null;
		}
		if ( parameterMetadataList != null ) {
			for ( ParameterMetadata parameterMetadata: parameterMetadataList ) {
				//if ( parameterMetadata.getIdentifier().equals(parameterId) ) {
				if ( parameterMetadata.getIdentifier().equals(parameter) ) {
					foundParam = parameterMetadata;
					break;
				}
			}
		}
		return foundParam;
	}

	/**
	 * Find a parameter metadata object the parameter identifier.
	 * @param parameterMetadataList the list of parameter metadata to search
	 * @param parameterid parameter identifier to match.
	 * @return the parameter metadata object that matches the requested parameter identifier, or null if not found
	 */
	private ParameterMetadata findParameterMetadataForParameterId ( List<ParameterMetadata> parameterMetadataList, String parameterId ) {
		ParameterMetadata foundParam = null;
		if ( parameterId == null ) {
			return null;
		}
		if ( parameterMetadataList != null ) {
			for ( ParameterMetadata parameterMetadata: parameterMetadataList ) {
				//if ( parameterMetadata.getIdentifier().equals(parameterId) ) {
				if ( parameterMetadata.getDisplayName().equals(parameterId) ) {
					foundParam = parameterMetadata;
					break;
				}
			}
		}
		return foundParam;
	}

	/**
	 * Get the data interval for a computation period identifier.
	 * @param compuationPeriodId the computation period identifier for the time series
	 * @return the data interval for a computation period identifier, or an "IrregSecond" if instantaneous values).
	 */
	private String getDataIntervalForComputationPeriodIdentifier ( String computationPeriodId ) {
		if ( computationPeriodId.equals("Unknown") || computationPeriodId.isEmpty() ) {
			return "IrregSecond";
		}
		else if ( computationPeriodId.equals("Daily") ) {
			return "Day";
		}
		else if ( computationPeriodId.equals("Hourly") ) {
			return "Hour";
		}
		else if ( computationPeriodId.equals("Monthly") ) {
			return "Month";
		}
		else if ( computationPeriodId.equals("Annual") ) {
			return "Year";
		}
		else {
			// Should not happen.
			return "";
		}
	}
	
	/**
	 * Return the location description list.
	 * @return the location description list
	 */
	public List<LocationDescription> getLocationDescriptionList () {
		return this.locationDescriptionList;
	}

	/**
 	* Get the properties for the plugin.
 	* A copy of the properties map is returned so that calling code cannot change the properties for the plugin.
 	* @return plugin properties map.
 	*/
	public Map<String,Object> getPluginProperties () {
		Map<String,Object> pluginProperties = new LinkedHashMap<>();
		// For now the properties are all strings so it is easy to copy.
    	for (Map.Entry<String, Object> entry : this.pluginProperties.entrySet()) {
        	pluginProperties.put(entry.getKey(),
                    	entry.getValue());
    	}
		return pluginProperties;
	}

	/**
	 * Get the statistic for a computational period.
	 * @param compuatationalIdentifier the computational period identifier to evaluate
	 * @return the statistic for a computational period, or an empty string if not used (for instantaneous values).
	 */
	private String getStatisticForComputationIdentifier ( String computationIdentifier ) {
		if ( computationIdentifier.equals("Unknown") || computationIdentifier.isEmpty() ) {
			return "";
		}
		else {
			return computationIdentifier;
		}
	}

	/**
	 * Return the list of time series catalog.
	 * @param readData if false, return the global cached data, if true read the data and reset in the cache
	 */
	public List<TimeSeriesCatalog> getTimeSeriesCatalog(boolean readData) {
		if ( readData ) {
			String dataTypeReq = null;
			String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
			this.tscatalogList = readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp );
		}
		return this.tscatalogList;
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType) {
		boolean includeWildcards = true;
		return getTimeSeriesDataIntervalStrings(dataType, includeWildcards);
	}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 * @includeWildcards if true, include "*" wildcard.
	 */
	public List<String> getTimeSeriesDataIntervalStrings(String dataType, boolean includeWildcards ) {
		String routine = getClass().getSimpleName() + ".getTimeSeriesDataIntervalStrings";
		List<String> dataIntervals = new ArrayList<>();
		Message.printStatus(2, routine, "Getting interval strings for data type \"" + dataType + "\"");

		// Only check datatype if not a wildcard.
		boolean doCheckDataType = false;
		if ( (dataType != null) && !dataType.isEmpty() && !dataType.equals("*") ) {
			doCheckDataType = true;
			if ( dataType.contains(" - ") ) {
				// Remove the trailing count:
				//   Datatupe - Count
				int pos = dataType.indexOf(" - ");
				if ( pos > 0 ) {
					dataType = dataType.substring(0,pos);
				}
			}
		}

		// Use the cached time series catalog read at startup.
		List<TimeSeriesCatalog> tscatalogList = getTimeSeriesCatalog(false);
		Message.printStatus(2, routine, "  Have " + tscatalogList.size() + " cached time series from the catalog.");
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			if ( doCheckDataType ) {
				// Only check the first part of the data type, which is the 'stationparameter_no'.
				if ( !dataType.equals(tscatalog.getDataType())) {
					// Data type does not match 'stationparameter_no'.
					continue;
				}
			}
			// Only add the interval if not already in the list.
			if ( !StringUtil.isInList(dataIntervals, tscatalog.getDataInterval())) {
				dataIntervals.add(tscatalog.getDataInterval());
			}
		}

		// Sort the intervals:
		// - TODO smalers need to sort by time
		Collections.sort(dataIntervals,String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Always allow querying list of time series for all intervals:
			// - always add so that people can get a full list
			// - adding at top makes it easy to explore data without having to scroll to the end

			dataIntervals.add("*");
			if ( dataIntervals.size() > 1 ) {
				// Also add at the beginning to simplify selections:
				// - could check for a small number like 5 but there should always be a few
				dataIntervals.add(0,"*");
			}
		}

		return dataIntervals;
	}

	/**
	 * Return the list of time series data type strings.
	 * This is the version that is required by TSTool UI.
	 * These strings are the same as the datastream field.
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types (treat as if "*").
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval) {
		boolean includeWildcards = true;
		return getTimeSeriesDataTypeStrings(dataInterval, includeWildcards );
	}

	/**
	 * Return the list of time series data type strings.
	 * These strings are the same as the datastream field.
	 */
	public List<String> getTimeSeriesDataTypeStrings(String dataInterval, boolean includeWildcards ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesDataTypeStrings";
		// Map to count how many time series for each daa type.
		Map<String,Integer> countMap = new HashMap<>();

		List<String> dataTypes = new ArrayList<>();

		// Get the cached list of time series catalog objects.
		List<TimeSeriesCatalog> tscatalogList = getTimeSeriesCatalog(false);

		// Create the data type list:
		// - use the global TimeSeriesCatalog to get the data type.
		boolean found = false;
		if ( tscatalogList != null ) {
			for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
				String tscatalogDataType = tscatalog.getDataType();
				// Update the count.
				Integer count = countMap.get(tscatalogDataType);
				if ( count == null ) {
					count = Integer.valueOf(1);
				}
				else {
					// Increment the count.
					count = Integer.valueOf(count + 1);
				}
				countMap.put(tscatalogDataType, count);
				found = false;
				for ( String dataType : dataTypes ) {
					//if ( stationParameterName.equals(dataType) ) {
					if ( tscatalogDataType.equals(dataType) ) {
						found = true;
						break;
					}
				}
				if ( !found ) {
					//Message.printStatus(2, routine, "Adding data type \"" + tscatalogDataType + "\"");
					dataTypes.add(tscatalogDataType);
				}
			}
		}
		
		// Add the count to the data types.
		boolean includeCount = true;
		if ( includeCount ) {
			int i = -1;
			for ( String dataType : dataTypes ) {
				++i;
				Integer count = countMap.get(dataType);
				if ( count == null ) {
					dataType += " - 0";
				}
				else {
					dataType += " - " + count;
				}
				dataTypes.set(i, dataType);
			}
		}

		// Sort the names.
		Collections.sort(dataTypes, String.CASE_INSENSITIVE_ORDER);

		if ( includeWildcards ) {
			// Add wildcard at the front and end - allows querying all data types for the location:
			// - always add so that people can get a full list
			// - adding at the top makes it easy to explore data without having to scroll to the end

			dataTypes.add("*");
			dataTypes.add(0,"*");
		}

		return dataTypes;
	}

	/**
 	* Return the identifier for a time series in the table model.
 	* The TSIdent parts will be uses as TSID commands.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	*/
	public TSIdent getTimeSeriesIdentifierFromTableModel( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel,
		int row ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesIdentifierFromTableModel";
    	Aquarius_TimeSeries_TableModel tm = (Aquarius_TimeSeries_TableModel)tableModel;
    	// Should not have any nulls.
    	//String locId = (String)tableModel.getValueAt(row,tm.COL_LOCATION_ID);
    	String source = (String)tableModel.getValueAt(row,tm.COL_DATA_SOURCE); // Default, may add agency or organization later.
    	String dataType = (String)tableModel.getValueAt(row,tm.COL_DATA_TYPE);
   		if ( dataType.contains("-") ) {
   			// Need to surround the data type in single quotes to protect the dash used with the statistic:
   			// - do this even if no statistic to avoid ambiguity with the statistic
   			dataType = "'" + dataType + "'";
   		}
    	String statistic = (String)tableModel.getValueAt(row,tm.COL_STATISTIC);
    	if ( (statistic != null) && !statistic.isEmpty() ) {
    		// Append the statistic.
    		dataType = dataType + "-" + statistic;
    	}
    	String interval = (String)tableModel.getValueAt(row,tm.COL_DATA_INTERVAL);
    	// Currently extract the scenario from the identifier in special situations.
    	String timeSeriesDescriptionIdentifier = (String)tableModel.getValueAt(row, tm.COL_TS_DESCRIPTION_DESCRIPTION);
    	String scenario = (String)tableModel.getValueAt(row, tm.COL_SCENARIO);
    	String inputName = ""; // Only used for files.
    	TSIdent tsid = null;
    	boolean useTsid = false;
		String datastoreName = this.getName();
		String locId = "";
    	if ( useTsid ) {
    		// Use the LocType and ts_id.
   			locId = "ts_id:" + tableModel.getValueAt(row,tm.COL_TS_ID);
    	}
    	else {
    		// Use the Location ID for the location.
   			locId = "" + tableModel.getValueAt(row,tm.COL_LOC_ID);
    	}
    	try {
    		tsid = new TSIdent(locId, source, dataType, interval, scenario, datastoreName, inputName );
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	return tsid;
	}

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel) {
    	return new Aquarius_TimeSeries_CellRenderer ((Aquarius_TimeSeries_TableModel)tableModel);
    }

    /**
     * Get the TableModel used for displaying the time series.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data) {
    	return new Aquarius_TimeSeries_TableModel(this,(List<TimeSeriesCatalog>)data);
    }

	/**
	 * Indicate whether the datastore provides a time series input filter.
	 * This datastore does provide an input filter panel.
	 */
	public boolean providesTimeSeriesListInputFilterPanel () {
		return true;
	}

	/**
	 * Read global data that should be kept in memory to increase performance.
	 * This is called from the constructor.
	 * The following data are read and are available with get() methods:
	 * <ul>
	 * <li>TimeSeriesCatalog - cache used to find time series without re-requesting from the web service</li>
	 * </ul>
	 * If an error is detected, set on the datastore so that TSTool View / Datastores will show the error.
	 * This is usually an issue with a misconfigured datastore.
	 */
	public void readGlobalData () {
		String routine = getClass().getSimpleName() + ".readGlobalData";
		Message.printWarning ( 2, routine, "Reading global data for datastore \"" + getName() + "\"." );

		// Read the location description data.
		
		try {
			this.locationDescriptionList = readLocationDescriptionList();
			Message.printStatus(2, routine, "Read " + this.locationDescriptionList.size() + " location descriptions." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global location description list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the location data:
		// - this requires separate calls for each location
		
		try {
			this.locationDataList = readLocationDataList();
			Message.printStatus(2, routine, "Read " + this.locationDataList.size() + " location data." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global location data list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the parameter data.
		
		try {
			this.parameterMetadataList = readParameterMetadataList();
			Message.printStatus(2, routine, "Read " + this.parameterMetadataList.size() + " parameters." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global parameter metadata list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the time series unique ID data.
		
		try {
			this.timeSeriesUniqueIdList = readTimeSeriesUniqueIdList();
			Message.printStatus(2, routine, "Read " + this.timeSeriesUniqueIdList.size() + " time series unique IDs." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global time series unique ID list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// Read the time series description data:
		// - read this after other data
		
		try {
			this.timeSeriesDescriptionList = readTimeSeriesDescriptionList();
			Message.printStatus(2, routine, "Read " + this.timeSeriesDescriptionList.size() + " time series descriptions." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global time series description list (" + e + ")");
			Message.printWarning(3, routine, e );
		}

		// The time series catalog COULD be used more throughout TSTool, such as when reading time series.
		// However, the initial implementation of readTimeSeries reads the list each time.
		// The cached list is used to create choices for the UI in order to ensure fast performance.
		// Therefore the slowdown is only at TSTool startup.
		try {
    		String dataTypeReq = null;
    		String dataIntervalReq = null;
    		InputFilter_JPanel ifp = null;
    		// Read the catalog for all time series.
			this.tscatalogList = readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp );
			Message.printStatus(2, routine, "Read " + this.tscatalogList.size() + " time series catalog." );
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global time series catalog list (" + e + ")");
			Message.printWarning(3, routine, e );
		}
	}

	/**
 	* Read the location data for a single objects.
 	* @return a location data object
 	*/
	private List<Publish.LocationDataServiceResponse> readLocationDataList() throws IOException {
		String routine = getClass().getSimpleName() + ".readLocationDataList";
		
        // Create the request.
        Publish.LocationDataServiceRequest request = new Publish.LocationDataServiceRequest();

        List<Publish.LocationDataServiceResponse> locationDataList = new ArrayList<>();
        for ( LocationDescription locationDescription : this.locationDescriptionList ) {
        	// Request the data.
        	try {
        		request.setLocationIdentifier(locationDescription.getIdentifier());
        		Publish.LocationDataServiceResponse response = client.Publish.get(request);
        		locationDataList.add(response);
        
        		// An enclosing object is not returned:
        		// - instead, the response has methods corresponding to Location data
        		// - therefore, just cache the list of responses
        	}
        	catch ( Exception e ) {
        		Message.printWarning ( 3, routine, "Exception reading location data for identifier \"" + locationDescription.getIdentifier() );
        		Message.printWarning ( 3, routine, e );
        	}
        }

		return locationDataList;
	}

	/**
 	* Read the location description list objects.
 	* @return the list of location description objects
 	*/
	private List<LocationDescription> readLocationDescriptionList() throws IOException {
		//String routine = getClass().getSimpleName() + ".readLocationDescriptionList";
		
        // Create the request.
        Publish.LocationDescriptionListServiceRequest request = new Publish.LocationDescriptionListServiceRequest();

        // Request the data.
        Publish.LocationDescriptionListServiceResponse response = client.Publish.get(request);
        
        List<LocationDescription> locationDescriptionList = response.getLocationDescriptions();

		// Sort on the name.
		//Collections.sort(variableList, new VariableComparator());
		return locationDescriptionList;
	}

	/**
 	* Read the parameter metadata list objects.
 	* @return the list of Parameter metadata objects
 	*/
	private List<ParameterMetadata> readParameterMetadataList() throws IOException {
		String routine = getClass().getSimpleName() + ".readParameterMetadataList";
		
        // Create the request.
        Publish.ParameterListServiceRequest request = new Publish.ParameterListServiceRequest();

        // Request the data.
        Publish.ParameterListServiceResponse response = client.Publish.get(request);
        
        List<ParameterMetadata> parameterMetadataList = response.getParameters();
        
        for ( ParameterMetadata parameterMetadata : parameterMetadataList )  {
        	Message.printStatus(2, routine, " ParameterMetadata identifier=\"" + parameterMetadata.getIdentifier() +
        		"\" displayName=\"" + parameterMetadata.getDisplayName() + "\"");
        }

		// Sort on the name.
		//Collections.sort(variableList, new VariableComparator());
		return parameterMetadataList;
	}

    /**
     * Read a single time series given its time series identifier using default read properties.
     * @param tsid time series identifier.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd, boolean readData ) {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
    	try {
    		return readTimeSeries ( tsid, readStart, readEnd, readData, null );
    	}
    	catch ( Exception e ) {
    		// Throw a RuntimeException since the method interface does not include an exception type.
    		Message.printWarning(2, routine, e);
    		throw new RuntimeException ( e );
    	}
    }

    /**
     * Read a single time series given its time series identifier.
     * @param tsidReq requested time series identifier.
     * The output time series may be different depending on the requested properties.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @param readProperties additional properties to control the query:
     * <ul>
     * <li> "Debug" - if true, turn on debug for the query</li>
     * <li> "IrregularInterval" - irregular interval (e.g., "IrregHour" to use instead of TSID interval,
     *      where the TSID intervals corresponds to the web services.</li>
     * <li> "Read24HourAsDay" - string "false" (default) or "true" indicating whether 24Hour interval time series
     *      should be output as 1Day time series.</li>
     * <li> "ReadDayAs24Hour" - string "false" (default) or "true" indicating whether day interval time series
     *      should be output as 24Hour time series.</li>
     * <li> "Timezone" - timezone such as "America/Denver" to convert from Aquarius UTC
     * </ul>
     * @return the time series or null if not read
     */
    public TS readTimeSeries (
    	String tsidReq,
    	DateTime readStart,
    	DateTime readEnd,
    	boolean readData,
    	HashMap<String,Object> readProperties
    	) throws Exception {
    	String routine = getClass().getSimpleName() + ".readTimeSeries";
		checkTokenExpiration();
		
    	// Get the properties of interest:
    	// - corresponds to parameters in the ReadAquarius command
    	// - TSID command uses the defaults and may result in more exceptions because TSID can only handle general behavior
    	if ( readProperties == null ) {
    		// Create an empty hashmap if necessary to avoid checking for null below.
    		readProperties = new HashMap<>();
    	}
    	Object object = readProperties.get("DataApi");
    	String dataApi = "Corrected"; // Default.
    	if ( object != null ) {
    		// Have a data API to call.
    		dataApi = (String)object;
    	}
    	String IrregularInterval = null;
    	TimeInterval irregularInterval = null;
    	//boolean read24HourAsDay = false;
    	//boolean readDayAs24Hour = false;
    	object = readProperties.get("IrregularInterval");
    	if ( object != null ) {
    		IrregularInterval = (String)object;
    		irregularInterval = TimeInterval.parseInterval(IrregularInterval);
    	}
    	/*
    	object = readProperties.get("Read24HourAsDay");
    	if ( object != null ) {
    		String Read24HourAsDay = (String)object;
    		if ( Read24HourAsDay.equalsIgnoreCase("true") ) {
    			read24HourAsDay = true;
    		}
    	}
    	object = readProperties.get("ReadDayAs24Hour");
    	if ( object != null ) {
    		String ReadDayAs24Hour = (String)object;
    		if ( ReadDayAs24Hour.equalsIgnoreCase("true") ) {
    			readDayAs24Hour = true;
    		}
    	}
    	*/
    	// Default time zone is the computer local time:
    	// - will be overridden below if 
    	ZoneId zoneId = ZoneId.systemDefault();
    	String timezone = zoneId.toString();
    	object = readProperties.get("Timezone");
    	if ( object != null ) {
    		// Have a time zone to use for output.
    		timezone = (String)object;
    		//zoneOffset = ZoneOffset.of(timezone);
    	}
    	Message.printStatus(2, routine, "Default timezone for output is \"" + timezone +
    		"\" (will be used if requested input period does not specify time zone and TimeZone parameter is not specified).");

		// The default period if not specified is the last 30 days:
		// - if hourly data observations are measured, 24x30 = 720 values will result, which is within the 1500 limit data point requests
		// - maybe need to round, but can't really do with irregular time series?
		if ( readEnd == null ) {
			readEnd = new DateTime ( DateTime.DATE_CURRENT );
			readEnd.setTimeZone(timezone);
			Message.printStatus(2, routine, "Using default read end: " + readEnd );
		}
		else {
			// Make sure that the time zone is set in a copy of the DateTime.
			readEnd = new DateTime(readEnd);
			if ( (readEnd.getTimeZoneAbbreviation() == null) || readEnd.getTimeZoneAbbreviation().isEmpty() ) {
				// No time zone in the read end so use the override time zone.
				readEnd.setTimeZone(timezone);
				Message.printStatus(2, routine, "Using specified read end with default timezone: " + readEnd );
			}
			else {
				Message.printStatus(2, routine, "Using specified read end with specified timezone: " + readEnd );
			}
		}
		if ( readStart == null ) {
			readStart = new DateTime ( DateTime.DATE_CURRENT );
			readStart.setTimeZone(timezone);
			readStart.addDay(-30);
		}
		else {
			// Make sure that the time zone is set in a copy of the DateTime.
			readStart = new DateTime(readStart);
			if ( (readStart.getTimeZoneAbbreviation() == null) || readStart.getTimeZoneAbbreviation().isEmpty() ) {
				// No time zone in the read start so use the override time zone.
				readStart.setTimeZone(timezone);
				Message.printStatus(2, routine, "Using specified read start with default timezone: " + readStart );
			}
			else {
				Message.printStatus(2, routine, "Using specified read start with specified timezone: " + readStart );
			}
		}

		// Set the zone ID here after checks on the read period and Timezone property:
		// - the read start timezone should be OK by here
   		zoneId = ZoneId.of(readStart.getTimeZoneAbbreviation());
		Message.printStatus(2, routine, "Data points will be converted from UTC to " + zoneId );

    	TS ts = null;

    	// Create a time series identifier for the requested TSID:
    	// - the actual output may be set to a different identifier based on the above properties
    	// - also save interval base and multiplier for the original request
    	TSIdent tsidentReq = TSIdent.parseIdentifier(tsidReq);

    	// Time series catalog for the single matching time series.
 		TimeSeriesCatalog tscatalog = null;

   		// Use Aquarius time series identifier parts to match a time series in the catalog:
   		// - station_no.stationparamer_no-ts_shortname
   		// - if necessary: station_no.'stationparamer_no'-'ts_shortname'
   		List<TimeSeriesCatalog> tscatalogReqList = TimeSeriesCatalog.findForTSIdent ( this.tscatalogList, tsidentReq );
   		if ( tscatalogReqList.size() == 0 ) {
   			// Did not match any time series.
   			throw new RuntimeException ( "No time series catalog found matching TSID = \"" + tsidentReq + "\" in " + this.tscatalogList.size() + " tscatalog.");
   		}
   		else if ( tscatalogReqList.size() > 1 ) {
   			// Matched more than one time series so identifier information is not unique.
   			boolean dev = false;
   			if ( dev ) {
   				// For now use the first one.
   				Message.printStatus ( 2, routine, "Matched " + tscatalogReqList.size() + " time series catalog for TSID = " + tsidentReq + ", using the first.");
   				tscatalog = tscatalogReqList.get(0);
   			}
   			else {
   				// Production:
   				// - this should not happen and may require defining a new statio and re-adding the asset
   				throw new RuntimeException ( "Matched " + tscatalogReqList.size() + " time series catalog for TSID = " + tsidentReq + ", expecting 1.");
   			}
   		}
   		else {
   			// Matched a single time series so can continue:
   			// - ts_id is used below to read data
   			Message.printStatus(2, routine, "Matched a single time series catalog for TSID = \"" + tsidReq + "\"." );
   			tscatalog = tscatalogReqList.get(0);
   		}

    	// Create the time series and set properties:
    	// - above code used "req" (requested) variables based on the requested TSID
    	// - from this point forward the "out" variables are used,
    	//   in case IrregularInterval, Read24HourAsDay, or ReadDayAs24Hour properties were specified
    	
   		if ( (irregularInterval != null) && !IrregularInterval.isEmpty() ) {
   			// Reset the irregular interval if requested.
   			tsidentReq.setInputName(IrregularInterval);
   			tsidReq = tsidentReq.toString();
   		}
    	ts = TSUtil.newTimeSeries(tsidReq, true);

    	// Set the time series properties.
    	//int intervalBaseOut = tsidentOut.getIntervalBase();
    	//int intervalMultOut = tsidentOut.getIntervalMult();
    	try {
    		ts.setIdentifier(tsidReq);
    	}
    	catch ( Exception e ) {
    		throw new RuntimeException ( e );
    	}
    	// Set the period to bounding data records:
    	// - the period may be reset below depending on time series interval, interval end adjustments, etc.
    	// - TODO smalers 2023-01-17 may need to do more to handle the case of interval data timestamps being adjusted below
    	if ( readStart != null ) {
    		ts.setDate1Original(readStart);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the start down to include a full interval.
    			readStart.round(-1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate1(readStart);
    	}
    	if ( readEnd != null ) {
    		ts.setDate2Original(readEnd);
    		/*
    		if ( TimeInterval.isRegularInterval(tsident.getIntervalBase()) ) {
    			// Round the end up to include a full interval
    			readEnd.round(1, tsident.getIntervalBase(), tsident.getIntervalMult());
    		}
    		*/
    		ts.setDate2(readEnd);
    	}

    	// Set standard properties:
    	// - the description in the time series description does not seem to be clean and may be blank.
    	String label = tscatalog.getTimeSeriesDescriptionLabel();
    	if ( (label == null) || label.isEmpty() ) {
    		// Set the description to the the location name and parameter.
    		ts.setDescription(tscatalog.getLocationDescriptionName() + " - " + tscatalog.getDataType());
    	}
    	else {
    		// Set the description to the location name and label.
    		ts.setDescription(tscatalog.getLocationDescriptionName() + " - " + tscatalog.getTimeSeriesDescriptionLabel());
    	}
		ts.setDataUnits("");
		ts.setDataUnitsOriginal("");
		ts.setMissing(Double.NaN);

		// Set the time series properties:
		// - additional properties are set below to help understand adjusted timestamps and offset days
		setTimeSeriesProperties ( ts, tscatalog, dataApi );

    	if ( readData ) {
    		// Also read the time series values.
    		String timeSeriesUniqueId = tscatalog.getTimeSeriesDescriptionUniqueId();
    		// Read the time series object, which has methods to retreive data points and other ata.
    		TimeSeriesDataServiceResponse timeSeries = readTimeSeriesData ( dataApi, timeSeriesUniqueId, readStart, readEnd );
    		
    		// The data are ordered with oldest first.
    		
    		List<TimeSeriesPoint> pointList = timeSeries.getPoints();

    		if ( pointList.size() > 0 ) {
    			// Set the period based on data from the first and last values:
    			// - this values may be adjusted below
    			//ts.setDate1(dataList.get(0).getTimestampAsDateTime(zoneOffset));
    			Instant instant1 = pointList.get(0).getTimestamp().getDateTimeOffset();
    			ts.setDate1(TimeUtil.ofInstant(instant1, zoneId));
    			ts.setDate1Original(ts.getDate1());
    			//ts.setDate2(dataList.get(dataList.size() - 1).getTimestampAsDateTime(zoneOffset));
    			Instant instant2 = pointList.get(pointList.size() - 1).getTimestamp().getDateTimeOffset();
    			ts.setDate2(TimeUtil.ofInstant(instant2, zoneId));
    			ts.setDate2Original(ts.getDate2());

    			// Allocate the time series data array:
    			// - do this after adjusting the period for timestamps
    			// - irregular interval does not allocate an array up front
    			ts.allocateDataSpace();

    			// Loop through the data values and set the data.
    			for ( TimeSeriesPoint point : pointList ) {
    				//DateTime dt = data.getTimestampAsDateTime ( zoneOffset );
    				Instant instant = point.getTimestamp().getDateTimeOffset();
    				DateTime dt = TimeUtil.ofInstant(instant, zoneId);
    				ts.setDataValue(dt, point.getValue().getNumeric());
    			}

    			/*
    			if ( badDateTimeCount > 0 ) {
    				//problems.add("Time series had " + badDateTimeCount + " bad timestamps.  See the log file.");
    				String message = "  Time series had " + badDateTimeCount + " bad timestamps.  See the log file.";
    				Message.printWarning(3,routine,message);
    				throw new Exception (message);
    			}
    			if ( badValueCount > 0 ) {
    				//problems.add("Time series had " + badValueCount + " bad data values.  See the log file.");
    				String message = "  Time series had " + badValueCount + " bad data values.  See the log file.";
    				Message.printWarning(3,routine,message);
    				throw new Exception(message);
    			}
    			if ( badInterpolationTypeCount > 0 ) {
    				String message = "  Time series had " + badInterpolationTypeCount + " bad interpolation types.  See the log file.";
    				//problems.add("Time series had " + badInterpolationTypeCount + " bad interpolation types.  See the log file.");
    				Message.printWarning(3,routine,message);
    				throw new Exception (message);
    			}
    			if ( valueErrorCount > 0 ) {
    				String message = "  Time series had " + valueErrorCount + " errors setting values.  See the log file.";
    				//problems.add("Time series had " + badDateTimeCount + " bad timestamps.  See the log file.");
    				Message.printWarning(3,routine,message);
    			}
    			*/
    		}
    		else {
    			Message.printStatus(2, routine, "No datapoints read for time series \""
    				+ tsidReq + "\" for readStart=" + readStart + " to " + readEnd );
    		}

    	}

    	return ts;
    }

	/**
 	* Read a single Aquarius time series.
 	* @param dataApi which API to use, either 'Raw' or 'Corrected' (default).
 	* @param timeSeriesUniqueId the time series unique identifier to read
 	* @param readStart the starting date/time to read
 	* @param readEnd the ending date/time to read
 	* @return the requested time series response (call its methods to get necessary data)
 	*/
	private TimeSeriesDataServiceResponse readTimeSeriesData ( String dataApi, String timeSeriesUniqueId, DateTime readStart, DateTime readEnd ) throws IOException {
		String routine = getClass().getSimpleName() + ".readTimeSeries";
		
		// The response is apparently the same no matter what.
		TimeSeriesDataServiceResponse response = null;

		if ( (dataApi != null) && dataApi.equalsIgnoreCase("Raw") ) {
			// Read raw data.
			// Create the request.
			TimeSeriesDataRawServiceRequest request = new Publish.TimeSeriesDataRawServiceRequest();
			request.setTimeSeriesUniqueId(timeSeriesUniqueId);

			// Request the data.
			Message.printStatus(2, routine, "Reading time series data using the 'Raw' API service.");
			response = client.Publish.get(request);
		}
		else {
			// Default is Corrected.
			// Create the request.
			TimeSeriesDataCorrectedServiceRequest request = new Publish.TimeSeriesDataCorrectedServiceRequest();
			request.setTimeSeriesUniqueId(timeSeriesUniqueId);

			// Request the data.
			Message.printStatus(2, routine, "Reading time series data using the 'Corrected' API service.");
			response = client.Publish.get(request);
		}
        
        //response.get
		return response;
	}

	/**
	 * Read time series catalog, which uses the cached time series descriptions and other data.
	 * @param dataTypeReq Requested data type (e.g., "DischargeRiver") or "*" to read all data types,
	 *        or null to use default of "*".
	 * @param dataIntervalReq Requested data interval (e.g., "IrregSecond") or "*" to read all intervals,
	 *        or null to use default of "*".
	 * @param ifp input filter panel with "where" conditions
	 */
	public List<TimeSeriesCatalog> readTimeSeriesCatalog ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
		String routine = getClass().getSimpleName() + ".readTimeSeriesCatalog";

		// Loop through the time series descriptions and add an entry in the catalog.
		List<TimeSeriesCatalog> tscatalogList = new ArrayList<>();
		for ( TimeSeriesDescription timeSeriesDescription : this.timeSeriesDescriptionList ) {
			if ( (dataTypeReq != null) && !dataTypeReq.isEmpty() && !dataTypeReq.equals("*") ) {
				// Filter based on the data type.
				if ( !timeSeriesDescription.getParameter().equals(dataTypeReq) ) {
					// Data type did not match.
					continue;
				}
			}

			if ( (dataIntervalReq != null) && !dataIntervalReq.isEmpty() && !dataIntervalReq.equals("*") ) {
				// Filter based on the data interval:
				// - currently all are IrregSecond so no filter
			}

			/*
			// Add query parameters based on the input filter:
			// - this includes list type parameters and specific parameters to match database values
			int numFilterWheres = 0; // Number of filter where clauses that are added.
			if ( ifp != null ) {
	        	int nfg = ifp.getNumFilterGroups ();
	        	InputFilter filter;
	        	for ( int ifg = 0; ifg < nfg; ifg++ ) {
	            	filter = ifp.getInputFilter ( ifg );
	            	//Message.printStatus(2, routine, "IFP whereLabel =\"" + whereLabel + "\"");
	            	boolean special = false; // TODO smalers 2022-12-26 might add special filters.
	            	if ( special ) {
	            	}
	            	else {
	            		// Add the query parameter to the URL.
				    	filter = ifp.getInputFilter(ifg);
				    	String queryClause = WebUtil.getQueryClauseFromInputFilter(filter,ifp.getOperator(ifg));
				    	if ( Message.isDebugOn ) {
				    		Message.printStatus(2,routine,"Filter group " + ifg + " where is: \"" + queryClause + "\"");
				    }
				    	if ( queryClause != null ) {
				    	requestUrl.append("&" + queryClause);
				    		++numFilterWheres;
				    	}
	            	}
	        	}
			}
			*/

			TimeSeriesCatalog tscatalog = new TimeSeriesCatalog();
			Message.printStatus(2, routine, "Adding tscatalog for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");

			// Standard properties expected by TSTool:
			// - locId - set below from description metadata
			// - datatype - set below from description metadata
			// - interval is always IrregSecond pending other information
			// - units set beow from description metadata

			// Set time series description data.
			tscatalog.setTimeSeriesDescriptionIdentifier ( timeSeriesDescription.getIdentifier() );
			tscatalog.setTimeSeriesDescriptionUniqueId ( timeSeriesDescription.getUniqueId() );
			// The following is set as the 'locId'
			tscatalog.setLocId ( timeSeriesDescription.getLocationIdentifier() );
			tscatalog.setTimeSeriesDescriptionParameter ( timeSeriesDescription.getParameter() );
			// The following is set as the 'dataType'
			tscatalog.setDataType ( timeSeriesDescription.getParameter() );
			tscatalog.setDataUnits ( timeSeriesDescription.getUnit() );
			tscatalog.setTimeSeriesDescriptionUtcOffset ( timeSeriesDescription.getUtcOffset() );
			tscatalog.setTimeSeriesDescriptionUtcOffsetIsoDuration ( timeSeriesDescription.getUtcOffsetIsoDuration() );
			tscatalog.setTimeSeriesDescriptionParameter ( timeSeriesDescription.getParameter() );
			tscatalog.setTimeSeriesDescriptionParameterId ( timeSeriesDescription.getParameterId() );
			tscatalog.setTimeSeriesDescriptionLastModified ( timeSeriesDescription.getLastModified() );
			tscatalog.setTimeSeriesDescriptionRawStartTime ( timeSeriesDescription.getRawStartTime() );
			tscatalog.setTimeSeriesDescriptionRawEndTime ( timeSeriesDescription.getRawEndTime() );
			tscatalog.setTimeSeriesDescriptionCorrectedStartTime ( timeSeriesDescription.getCorrectedStartTime() );
			tscatalog.setTimeSeriesDescriptionCorrectedEndTime ( timeSeriesDescription.getCorrectedEndTime() );
			tscatalog.setTimeSeriesDescriptionTimeSeriesType ( timeSeriesDescription.getTimeSeriesType() );
			tscatalog.setTimeSeriesDescriptionLabel ( timeSeriesDescription.getLabel() );
			tscatalog.setTimeSeriesDescriptionComment ( timeSeriesDescription.getComment() );
			tscatalog.setTimeSeriesDescriptionDescription ( timeSeriesDescription.getDescription() );
			tscatalog.setTimeSeriesDescriptionComputationIdentifier ( timeSeriesDescription.getComputationIdentifier() );
			tscatalog.setStatistic ( getStatisticForComputationIdentifier(timeSeriesDescription.getComputationIdentifier()) );
			tscatalog.setTimeSeriesDescriptionComputationPeriodIdentifier ( timeSeriesDescription.getComputationPeriodIdentifier() );
			tscatalog.setDataInterval ( getDataIntervalForComputationPeriodIdentifier(timeSeriesDescription.getComputationPeriodIdentifier()) );
			tscatalog.setTimeSeriesDescriptionSubLocationIdentifier ( timeSeriesDescription.getSubLocationIdentifier() );
			
			// Set the scenario to "Historical" if ".Historical." is found in the identifier.
			String timeSeriesDescriptionIdentifier = timeSeriesDescription.getIdentifier();
			if ( timeSeriesDescriptionIdentifier.contains(".Historical.") ) {
				tscatalog.setScenario ( "Historical" );
			}

			// Set location data:
			// - this includes location "description" and location "data" objects
			tscatalog.setLocId ( timeSeriesDescription.getLocationIdentifier() );
			LocationDescription locationDescription = findLocationDescriptionForLocationIdentifier (
				this.locationDescriptionList, timeSeriesDescription.getLocationIdentifier() );
			if ( locationDescription != null ) {
				//Message.printStatus(2, routine, "Found location description \"" + locationDescription.getIdentifier()
				//+ "\" for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");

				// Set location metadata in the tscatalog:
				// - only include what seems useful
				//locationDescription.getIsExternalLocation();
				tscatalog.setLocationDescriptionName(locationDescription.getName());
				//locationDescription.getTags();
				tscatalog.setLocationDescriptionUniqueId(locationDescription.getUniqueId());
				tscatalog.setLocationDescriptionUtcOffset(locationDescription.getUtcOffset());
			}
			else {
				Message.printStatus(2, routine, "Did not location description \"" + timeSeriesDescription.getLocationIdentifier()
					+ "\" for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");
			}

			Publish.LocationDataServiceResponse locationData = findLocationDataForLocationIdentifier (
				this.locationDataList, timeSeriesDescription.getLocationIdentifier() );
			if ( locationData != null ) {
				//Message.printStatus(2, routine, "Found location description \"" + locationDescription.getIdentifier()
				//+ "\" for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");

				// Set location data in the tscatalog:
				// - only include what seems useful
				tscatalog.setLocationDataElevation(locationData.getElevation());
				tscatalog.setLocationDataElevationUnits(locationData.getElevationUnits());
				tscatalog.setLocationDataLatitude(locationData.getLatitude());
				tscatalog.setLocationDataLongitude(locationData.getLongitude());
			}
			else {
				Message.printStatus(2, routine, "Did not location data \"" + timeSeriesDescription.getLocationIdentifier()
					+ "\" for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");
			}

			// Set parameter data.
			ParameterMetadata parameterMetadata = findParameterMetadataForParameter (
				this.parameterMetadataList, timeSeriesDescription.getParameter() );
			if ( parameterMetadata != null ) {
				Message.printStatus(2, routine, "Found parameter metadata \"" + timeSeriesDescription.getParameterId()
					+ "\" for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");
				// Set parameter metadata in the tscatalog:
				// - only include what seems useful
				tscatalog.setParameterMetadataIdentifier(parameterMetadata.getIdentifier());
				tscatalog.setParameterMetadataDisplayName(parameterMetadata.getDisplayName());
			}
			else {
				Message.printStatus(2, routine, "Did not parameter metadata \"" + timeSeriesDescription.getParameterId()
					+ "\" for time series unique ID \"" + timeSeriesDescription.getUniqueId() + "\".");
			}

			// Have enough data to set the time series identifier:
			// - this should be the same as the table model getTimeSeriesIdentifierFromTableModel method,
			//   but the table model is not active here
			String statistic = tscatalog.getStatistic();
			String scenarioPart = tscatalog.getScenario();
			if ( ! tscatalog.getScenario().isEmpty() ) {
				scenarioPart = "." + tscatalog.getScenario();
			}
			if ( (statistic != null) && !statistic.isEmpty() ) {
				tscatalog.setTsId(tscatalog.getLocId() + "." + TS_DATA_SOURCE + "." + tscatalog.getDataType()
					+ "-" + statistic + "." + tscatalog.getDataInterval() + scenarioPart );
			}
			else {
				tscatalog.setTsId(tscatalog.getLocId() + "." + TS_DATA_SOURCE + "." + tscatalog.getDataType()
					+ "." + tscatalog.getDataInterval() + scenarioPart );
			}

			// Save the catalog in the list.
			tscatalogList.add(tscatalog);
		}
		
		// Check the catalog list for problems.
		for ( TimeSeriesCatalog tscatalog : tscatalogList ) {
			// Make sure that the time series identifier is unique.
			boolean duplicateTsid = false;
			for ( TimeSeriesCatalog tscatalog2 : tscatalogList ) {
				if ( tscatalog == tscatalog2 ) {
					// Don't compare with itself.
					continue;
				}
				if ( tscatalog.getTsId().equals(tscatalog2.getTsId()) ) {
					// The time series identifier is not unique.
					duplicateTsid = true;
					break;
				}
			}
			if ( duplicateTsid ) {
				tscatalog.addProblem("TSTool TSID is not unique.");
			}
		}

		return tscatalogList;
	}

    /**
     * Read time series metadata, which results in a query that joins station, station_type, point, point_class, and point_type.
     */
    List<TimeSeriesCatalog> readTimeSeriesMeta ( String dataTypeReq, String dataIntervalReq, InputFilter_JPanel ifp ) {
    	// Remove note from data type.
	   	int pos = dataTypeReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataTypeReq = dataTypeReq.substring(0, pos);
	   	}
	   	pos = dataIntervalReq.indexOf(" - ");
	   	if ( pos > 0 ) {
		   	dataIntervalReq = dataIntervalReq.substring(0, pos).trim();
	   	}
	   	// By default all time series are included in the catalog:
	   	// - the filter panel options can be used to constrain
	    return readTimeSeriesCatalog ( dataTypeReq, dataIntervalReq, ifp );
	}

	/**
 	* Read the time series description objects.
 	* @return the list of time series description objects
 	*/
	private List<TimeSeriesDescription> readTimeSeriesDescriptionList() throws IOException {
		String routine = getClass().getSimpleName() + ".readTimeSeriesDescriptionList";
		
		// Indicate the request method:
		// - GET has a limit of about 60 unique IDs, POST has no limit
		boolean doGet = false;
        List<TimeSeriesDescription> timeSeriesDescriptionList = new ArrayList<>();
		if ( doGet ) {
	        // Create the request.
	        Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest request = new Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest();
	        // It appears that reading all may have a limit:
	        // - therefore test reading a smaller number
	        int readFlag = 2;
	       	ArrayList<String> uniqueIds = null;
	        if ( readFlag == -1 ) {
	        	// Empty list:
	        	// - see if that causes all to be read
	        }
	        else if ( readFlag == 0 ) {
	        	// Request all the available time series.
	        	uniqueIds = new ArrayList<>();
	        	for ( TimeSeriesUniqueIds id : this.timeSeriesUniqueIdList ) {
	        		uniqueIds.add(id.getUniqueId());
	        	}
	        }
	        else if ( readFlag == 1 ) {
	        	// Request a single time series.
	        	uniqueIds = new ArrayList<>();
	        	uniqueIds.add(this.timeSeriesUniqueIdList.get(0).getUniqueId());
	        }
	        else {
	        	// Request multiple time series:
	        	// - set readFlag >= 2 for this case
	        	// - 50 seems to work, documentation says the limit is about 60
	        	uniqueIds = new ArrayList<>();
	        	for ( int i = 0; i < 50; i++ ) {
	        		TimeSeriesUniqueIds id = this.timeSeriesUniqueIdList.get(i);
	        		uniqueIds.add(id.getUniqueId());
	        	}
	        }
	       	if ( uniqueIds != null ) {
	       		Message.printStatus(2, routine, "Reading time series descriptions with unique ID list length of " + uniqueIds.size() );
	       		request.setTimeSeriesUniqueIds(uniqueIds);
	       	}
	       	else {
	       		Message.printStatus(2, routine, "Reading all time series descriptions." );
	       	}
	
	        Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse response = client.Publish.get(request);
	        
	        timeSeriesDescriptionList = response.getTimeSeriesDescriptions();
		}
		else {
	        Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest request = new Publish.TimeSeriesDescriptionListByUniqueIdServiceRequest();
        	// Request all the available time series.
	       	ArrayList<String> uniqueIds = null;
        	uniqueIds = new ArrayList<>();
        	for ( TimeSeriesUniqueIds id : this.timeSeriesUniqueIdList ) {
        		uniqueIds.add(id.getUniqueId());
        	}
       		request.setTimeSeriesUniqueIds(uniqueIds);
	        Publish.TimeSeriesDescriptionListByUniqueIdServiceResponse response = client.Publish.post(request);
	        timeSeriesDescriptionList = response.getTimeSeriesDescriptions();
		}

		// Sort on the location ID and the data type.
		Collections.sort(timeSeriesDescriptionList, new TimeSeriesDescriptionComparator());
		return timeSeriesDescriptionList;
	}

	/**
 	* Read the time series unique ID objects.
 	* @return the list of time series unique ID objects
 	*/
	private List<TimeSeriesUniqueIds> readTimeSeriesUniqueIdList() throws IOException {
		//String routine = getClass().getSimpleName() + ".readParameterMetadataList";
		
        // Create the request.
        Publish.TimeSeriesUniqueIdListServiceRequest request = new Publish.TimeSeriesUniqueIdListServiceRequest();

        Publish.TimeSeriesUniqueIdListServiceResponse response = client.Publish.get(request);
        
        List<TimeSeriesUniqueIds> timeSeriesUniqueIdList = response.getTimeSeriesUniqueIds();

		// Sort on the name.
		//Collections.sort(variableList, new VariableComparator());
		return timeSeriesUniqueIdList;
	}

    /**
     * Read the version from the web service, used when processing #@require commands in TSTool.
     * TODO smalers 2023-01-03 need to figure out if a version is available.
     */
    private String readVersion () {
		checkTokenExpiration();
    	return "";
    }

    /**
     * Set the time series properties from the TimeSeriesCatalog.
     * @param ts the time series to update
     * @param tscatalog the time series catalog matching the time series
     * @param dataApi the API service used to request time series data, 'Raw', or 'Corrected'
     */
    private void setTimeSeriesProperties ( TS ts, TimeSeriesCatalog tscatalog, String dataApi ) {
    	// Set all the Aquarius properties that are known for the time series:
    	// - use names that match the Aquarius API to allow using the API documentation

    	ts.setProperty("timeseries.identifier", tscatalog.getTimeSeriesDescriptionIdentifier());
    	ts.setProperty("timeseries.uniqueId", tscatalog.getTimeSeriesDescriptionUniqueId());
    	ts.setProperty("dataapi", dataApi );

    	ts.setProperty("timeseries.parameter", tscatalog.getTimeSeriesDescriptionParameter());
    	ts.setProperty("timeseries.parameterId", tscatalog.getTimeSeriesDescriptionParameterId());
	   	// The following is set as the 'dataType'
	   	//ts.setProperty("timeseries.xxx", timeSeriesDescriptionParameterId = "";
	   	// The following is set as 'dataUnit'
	   	//ts.setProperty("timeseries.xxx", timeSeriesDescriptionUnit = "";
	   	ts.setProperty("timeseries.utcOffset", tscatalog.getTimeSeriesDescriptionUtcOffset());
	   	ts.setProperty("timeseries.utcOffsetIsoDuration", tscatalog.getTimeSeriesDescriptionUtcOffsetIsoDuration());
	   	ts.setProperty("timeseries.lastModified", tscatalog.getTimeSeriesDescriptionLastModified());
	   	ts.setProperty("timeseries.rawStartTime", tscatalog.getTimeSeriesDescriptionRawStartTime());
	   	ts.setProperty("timeseries.rawEndTime", tscatalog.getTimeSeriesDescriptionRawEndTime());
	   	ts.setProperty("timeseries.correctedStartTime", tscatalog.getTimeSeriesDescriptionCorrectedStartTime());
	   	ts.setProperty("timeseries.correctedEndTime", tscatalog.getTimeSeriesDescriptionCorrectedEndTime());
	   	ts.setProperty("timeseries.type", tscatalog.getTimeSeriesDescriptionTimeSeriesType());
	   	ts.setProperty("timeseries.label", tscatalog.getTimeSeriesDescriptionLabel());
	   	ts.setProperty("timeseries.comment", tscatalog.getTimeSeriesDescriptionComment());
	   	ts.setProperty("timeseries.description", tscatalog.getTimeSeriesDescriptionDescription());
	   	ts.setProperty("timeseries.compuationIdentifier", tscatalog.getTimeSeriesDescriptionComputationIdentifier());
	   	ts.setProperty("timeseries.compuationPeriodIdentifier", tscatalog.getTimeSeriesDescriptionComputationPeriodIdentifier());
	   	ts.setProperty("timeseries.subLocationIdentifier", tscatalog.getTimeSeriesDescriptionSubLocationIdentifier());

	   	// ExtendedAttributes - not handled
	   	// Thresholds - not handled

	   	// Location description, listed alphabetically.
	   	ts.setProperty("location.uniqueId", tscatalog.getLocationDescriptionUniqueId());
	   	ts.setProperty("location.utcOffset", tscatalog.getLocationDescriptionUtcOffset());
	   	ts.setProperty("location.name", tscatalog.getLocationDescriptionName());

	   	// Location data, listed alphabetically.
	   	ts.setProperty("location.elevation", tscatalog.getLocationDataElevation());
	   	ts.setProperty("location.elevationunits", tscatalog.getLocationDataElevationUnits());
	   	ts.setProperty("location.latitude", tscatalog.getLocationDataLatitude());
	   	ts.setProperty("location.longitude", tscatalog.getLocationDataLongitude());
	
	   	// Parameter data, listed alphabetically.
	   	ts.setProperty("parameter.displayName", tscatalog.getParameterMetadataDisplayName());
	   	ts.setProperty("parameter.identifier", tscatalog.getParameterMetadataIdentifier());
    }
    
}