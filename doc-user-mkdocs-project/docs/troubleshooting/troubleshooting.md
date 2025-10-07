# TSTool / Troubleshooting #

Troubleshooting TSTool for Aquarius involves confirming that the core product and plugin are performing as expected.
Issues may also be related to Aquarius data.

*   [Troubleshooting Core TSTool Product](#troubleshooting-core-tstool-product)
*   [Troubleshooting Aquarius TSTool Integration](#troubleshooting-aquarius-tstool-integration)
    +   [***Commands(Plugin)*** Menu Contains Duplicate Commands](#commandsplugin-menu-contains-duplicate-commands)
    +   [Web Service Datastore Returns no Data - Authentication Problem](#web-service-datastore-returns-no-data-authentication-problem)
    +   [Web Service Datastore Returns no Data](#web-service-datastore-returns-no-data)
    +   [Duplicate Time Series are Listed in TSTool](#duplicate-time-series-are-listed-in-tstool)

------------------

## Troubleshooting Core TSTool Product ##

See the main [TSTool Troubleshooting documentation](https://opencdss.state.co.us/tstool/latest/doc-user/troubleshooting/troubleshooting/).

## Troubleshooting Aquarius TSTool Integration ##

The following are typical issues that are encountered when using TSTool with Aquarius.
The ***View / Datastores*** menu item will display the status of datastores.
The ***Tools / Diagnostics - View Log File...*** menu item will display the log file.

### ***Commands(Plugin)*** Menu Contains Duplicate Commands ###

This problem should not occur because Aquarius plugins have versions that are recognized by TSTool.
It is possible that a plugin jar file was not packaged correctly,
but this would typically only occur with development versions.

If the ***Commands(Plugin)*** menu contains duplicate commands,
TSTool is finding multiple plugin `jar` files.
Make sure that the TSTool version is at least 15.0.0.
If the problem remains, check the `plugins` folder and subfolders for the software installation folder
and the user's `.tstool/NN/plugins` folder.
Remove extra jar files, leaving only the version that is desired (typically the most recent version).

### Web Service Datastore Returns no Data - Authentication Problem ###

The Aquarius web service API requires an API token for all requests.
The Aquarius SDK API library used by TSTool reads authentication properties from the plugin datastore configuration file.

Define the authentication properties in the [datastore configuration file](../datastore-ref/Aquarius/Aquarius.md#datastore-configuration-file)
to allow the TSTool Aquarius plugin to determine the token value to use for queries.
See also the [`SetPropertyFromDataStore`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetPropertyFromDataStore/SetPropertyFromDataStore/)
command (available in TSTool 14.7.0 and later), which can be used to define a processor property that can be passed to TSTool commands
such as [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/).

If the authentication information is inaccurate, no data will be returned.

### Web Service Datastore Returns no Data ###

If the web service datastore returns no data, check the following:

1.  Confirm that the input parameters are correct:
    1.  Confirm that the station and time series are active.
    2.  Confirm that the period of record for the time series in the ***Time Series List*** area overlaps the requested period.
        Note that the API and Aquarius use UTC whereas local time is used by default for TSTool commands.
    3.  If no [`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command is specified,
        a default period of the last 30 days is used,
        in order to minimize load on Aquarius web services.
        Some stations may be temporarily offline, for example due to winter conditions or maintenance.
        If necessary, use a longer perod to confirm that some data are available.
    4.  Use the time series table view to view data and confirm that values are not missing
        (e.g., `NaN` or `null` data values will be indicated as missing).
2.  Review the TSTool log file for errors.
    Typically a message will indicate an HTTP error code for the URL that was requested.
3.  Use another tool such as `curl` or `Postman` to test the request,
    which will require appropriate authentication information.
    Using a URL request will not directly match the API library but can help with troubleshooting.
4.  See Aquarius API documentation:
    *   [AQUARIUS Acquisition API Reference Guide](https://panama.aquaticinformatics.net/AQUARIUS/Acquisition/v2/docs/reference.html).
    *   [AQUARIUS Publish API Reference Guide](https://panama.aquaticinformatics.net/AQUARIUS/Publish/v2/docs/reference.html).

If the issue cannot be resolved, contact the [Open Water Foundation](https://openwaterfoundation.org/about-owf/staff/).

### Duplicate Time Series are Listed in TSTool ###

TSTool requires unique time series identifiers to properly read and write data.
Duplicate Aquarius time series (and corresponding TSTool time series identifiers) may be shown in the TSTool time series list.
This may be due to the TSTool plugin not adequately determining unique identifiers.
The ***Problems*** column of the list will indicate when the TSTool time series identifier is not unique.
Report the issue to the Open Water Foundation.
