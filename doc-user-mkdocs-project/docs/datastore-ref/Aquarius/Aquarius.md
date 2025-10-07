# TSTool / Datastore Reference / Aquarius Web Services #

*   [Overview](#overview)
*   [Outstanding Issues](#outstanding-issues)
*   [Standard Time Series Properties](#standard-time-series-properties)
*   [Other Specifications and Integration Considerations](#other-specifications-and-integration-considerations)
    +   [Timezone Handling](#timezone-handling)
*   [Limitations](#limitations)
*   [Datastore Configuration File](#datastore-configuration-file)
*   [See Also](#see-also)

--------------------

## Overview ##

Aquarius web services allow queries by software,
including web applications and analysis tools such as TSTool.
TSTool accesses Aquarius web services using the Aquarius plugin.
Aquarius data can be used for real-time operations archiving historical data.

See the following documentation:

*   Aquarius:
    +   [Aquarius Acquisition API Reference Guide](https://panama.aquaticinformatics.net/AQUARIUS/Acquisition/v2/docs/reference.html)
    +   [Aquarius Publish API Reference Guide](https://panama.aquaticinformatics.net/AQUARIUS/Publish/v2/docs/reference.html)
*   TSTool:
    +   [Install Aquarius Plugin appendix](../../appendix-install/install.md)
    +   [Aquarius TSID command](../../command-ref/TSID/TSID.md)
    +   [`ReadAquarius` command](../../command-ref/ReadAquarius/ReadAquarius.md)

The Aquarius API is organized by major functions.
Reading time series data involves using the "Acquisition" API.
Reading time series data involves using the "Publish" API.
TSTool primarily uses the following Aquarius Publish web services,
which are accessed via the Aquarius Java SDK.
The Java request methods have names that vary slightly from the "Swagger" services shown below.

**<p style="text-align: center;">
Aquarius Web Services Used by the TSTool Plugin
</p>**

| **API** | **Resource Type** | **Service**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | **Comments** |
| -- | -- | -- | -- |
| **Publish** | Location    | `GetLocationDescriptionList` | Read a list of location metadata. |
||             | `GetLocationData` | Read location data. |
|| Parameter   | `GetParameterList` | Read parameter metadata. |
|| Time Series | `GetTimeSeriesUniqueIdList` | Read time series unique identifiers. |
||             | `GetTimeSeriesDescriptionList` | Read time series description (metadata) list. |

To improve performance, Aquarius objects are read when TSTool starts and are cached.
Time series data are records are not cached.
If metadata are changed, restart TSTool.
This approach provides good performance on systems that have stable configurations.

The TSTool [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/)
command can be used to retrieve data from any web service and save to a file.
For example, a JSON format file can be saved and the resulting file can be read using commands such as
[`NewObject`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/NewObject/NewObject/).
These general commands provide flexibility to retrieve data in addition to the specific Aquarius datastore commands.
See the [`SetPropertyFromDataStore`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetPropertyFromDataStore/NSetPropertyFromDataStore/)
command (available in TSTool 14.7.0) to set a processor property from the datastore property
so that the token can be used in `WebGet` commands without hard-coding the token.

## Outstanding Issues ##

The following are outstanding issues with TSTool Aquarius plugin and the Aquarius API,
specific to the Panama Canal Authority system.

1.  **API feedback**;
    1.  The Swagger interface does not describe default values.
        For example, what is the default for `QueryFrom` and `QueryTo` for the `GetTimeSeriesData` service?
    2.  The format of parameters is not clear in some cases, for example `QueryFrom` and `QueryTo`.
        ISO?  Epoch seconds?
2.  **Unique time series identifier**:
    1.  TSTool uses a unique identifier from the location identifier, data type (parameter),
        and interval (computation period).  This is not sufficient and additional metadata needs to be used.
        TSTool can handle this by also using a scenario or other time series identifier part.
    2.  The time series list shows some time series that have the same TSTool identifier information.
        The only difference seems to be that the Aquarius identifier has, for example,
        `Precipitation.Historical.Monthly Telemetria@ACL` and `Precipitation.Monthly Telemetria@ACL`.
        Does `Historical` show up elsewhere so that it could be easily handled without parsing out of the identifier?
        The time series identifier used in TSTool software is like `ACL.Aquarius.Precipitation-Sum.Month`
        and can accomodate `historical` in a scenario or other field.
        The human-friendly Aquarius label may be approprirate to help uniquely identify a time series;
        however, it can be long, may not be specified and may not be clear.
3.  **Period of record**:
    1.  What is the default period of record for queries?
        It seems that using the raw or corrected API calls reads the full period.
        This is OK if fast but if slow or puts undue load on the web services server,
        then a shorter period default would be better, such as one year.
4.  **Data interval**:
    1.  The compuation period is used to determine the data interval.
    2.  What is the full list of supported periods?  Panama does not have 1Hour or other time-based periods.
    3.  Note that TSTool uses the term "interval" (period is used for the overall time window for a time series).
5.  **Difference between raw, corrected, etc.**:
    1.  Need a basic overview of this how it corresponds to unique time series, etc.
    2.  How are time series ingested from NovaStar handled as "raw" and "corrected"?
    3.  How do time series descriptions and other data in Aquarius indicate raw and corrected time series
        so that it is obvious which API requests can be used?
        Should the time series type, raw and corrected start/end be used to determine?
    4.  It seems like raw and corrected requests return the same response format in the Java API?
6.  **Data flags**:
    1.  TSTool has the ability to set a string flag with each data value,
        for example to show data quality.
    2.  Are there appropriate flags in Aquarius?
    3.  Is the NovaStar flag passed through?
7.  **Missing data**:
    1.  Does Aquarius store missing values, for example interval values that are missing or could not be computed?
    2.  If so, how are missing floating point values indicated?
8.  **Timezone**:
    1.  Need to confirm how it is used for locations.
9.  **Lifecycle history**:
    1.  TSTool has the ability to track the time series lifecycle history as comments.
        Is there a way to retrieve such information from Aquarius?
10. **NovaStar integration**
    1.  Data from the NovaStar system will be input to the Aquarius system using the API.
    2.  How are the current NovaStar time series handled in Aquarius?  What conventions?
        Steve will check the scheduled process that is being used to export data for Aquarius.
    3.  One goal is to be able to read the same time series out of NovaStar and Aquarius
        so that they can be compared.
        A predictable time series identifier mapping needs to be implemented.
    4.  Is it expected that time series are created in Aquarius using the UI or the API.
        It will probably be necessary to clean up NovaStar time series.
11. **Aquarius API version:**
    1.  How will the Aquarius software, web services API, and Java library be kept synchronized?
        For example, the documentation and Swagger page is is for "v2", not a more specific version.
    2.  Is the Java library robust to handle version changes?
12. **Aquarius tech support:**
    1.  How should the issue tracker and support be used going forward?
        Big issues?  Small issues?
        Trying to get as much of this answered up front will save time on iterations.

## Web Service to Time Series Mapping ##

Time series data objects in TSTool consist of various properties such as location identifier, data type, units,
and data arrays containing data values.
To convert Aquarius data to time series requires joining location, time series description, and parameter metadata,
and time series data points for time series values.

The TSTool main interface browsing tool displays joined information in the time series list to select time series.
A catalog of unique time series is created from metadata and is used to provide user choices.
The ***Data type*** and ***Time step*** are general filters implemented for all datastores and the
***Where*** input filters are specific to Aquarius.

**<p style="text-align: center;">
![tstool-where](tstool-where.png)
</p>**

**<p style="text-align: center;">
TSTool Where Filters
</p>**

## Standard Time Series Properties ##

The general form of time series identifier used by TSTool is:

```
LocationID.DataSource.DataType.Interval~DatastoreName
```

The standard time series identifier format for Aquarius web service time series is as follows.

```
ACL.Aquarius.Voltage.IrregSecond[.Scenario]~Aquarius-SomeSystem
```

The meaning of the TSID parts is as follows.
See the [Troubleshooting](../../troubleshooting/troubleshooting.md) documentation for information about dealing with duplicate time series identifiers.

*   The `LocationId` is set to:
    +   Aquarius time series description location identifier.
*   The `DataSource` is set to:
    +   `Aquarius` always, not necessary to query the time series.
*   The `DataType` is set to:
    +   Aquarius time series description parameter.
*   The `Interval` is set to standard TSTool intervals:
    +   `IrregSecond` for instantaneous time series (computation period ID is `Unknown` or empty).
    +   `Day` for Aquarius `Daily` interval data.
    +   `Month` for Aquarius `Monthly` interval data.
    +   `Year` for Aquarius `Annual` interval data.
    +   **TODO - are multiple allowed, what about N-hour and N-minute interval data?**
*   The `Scenario` is optional for TSTool but may be needed to uniquely identify time series:
    +   Why are variations of time series when location, data type (parameter) and interval are otherwise the same?
    +   Should the label be used for the scenario?
*   The `DatastoreName` is taken from the datastore configuration file `Name` property:
    +   The datastore name is listed in the TSTool main interface.
    +   Multiple datastores can be configured, each pointing to a different Aquarius web services
        (e.g., if multiple versions are available) and organization account.
        Therefore, datastore names should be assigned with enough detail to avoid confusion.
        The following are typical examples:
        -   `Aquarius` - general, if only one datasture will be configured
        -   `Aquarius-Org` - for a specific organization

Important standard time series properties include:

1.  **Time Series Description**:
    1.  The Aquarius time series description is used in graph legends.
    2.  If the Aquarius time series description label is available, use the location name, a dash, and the label.
    3.  If the Aquarius time series description label is not available, use the location name, a dash, and the parameter.
2.  **Data Units**:
    1.  The Aquarius time series description unit.
3.  **Missing Data Value**:
    1.  The special value `NaN` is used internally for the missing data value
        and is used when web service data values are reported as `null`.
    2.  It does not clear whether Aquarius allows null values.
4.  **Aquarius Properties:**
    1.  Other Aquarius properties are set as time series general properties.

See the next section for additional mapping of Aquarius data to TSTool time series.

## Other Specifications and Integration Considerations ##

The following are other specifications related to TSTool plugin integration with Aquarius web services.

1.  **Service URL**:
    1.  The configuration file `ServiceRootUrl` property includes only the server address.
        Because the Aquarius Java SDK is used, underlying URL path and query parameters are added by the SDK.
        See the [Datastore Configuration File](#datastore-configuration-file) section for an example.
2. **Data Caching:**
    1.  TSTool performance, in particular interactive features, is impacted by web service query times.
        Therefore, it is desirable to cache data in memory so that software does not need to requery web services.
        The trade-off is that when data are cached, changes in the Aquarius system will not be visible in the TSTool
        session unless TSTool rereads the data.
        There is a balance between performance and having access to the most recent data.
    2.  Currently, the TSTool plugin caches the location, parameter, and time series description objects
        and the time series catalog objects that join these objects.
        The catalog is used to determine unique lists of choices used in the user interface.
3.  **Response Limits (Data Throttling)**:
    1.  Aquarius web services may throttle requests.
        This will be evaluated as the plugin is implemented.
5.  **Timezone:**
    1.  Aquarius internally stores data in UTC and web service times and query parameters use UTC by default.
    2.  Is the station local timezone saved consistently in Aquarius?
    3.  TSTool uses the computer local time zone for the query start and end and time series data.
6.  **Timestamp and Data Interval:**
    1.  The time series computation period indicates the time series data interval,
        but the initial focus has been irregular interval time series.
    2.  Times are saved to millisecond precision.
    3.  Future enhancements will enable more options for handling regular interval time series.
7.  **Observations:**
    1.  Does Aquarius store a data flag for values?
    2.  If duplicate values are encountered at the same time, the last value encountered is saved in the time series.

### Timezone Handling ###

The following are features of Aquarius web services related to timezone:

*   Aquarius internally stores data in UTC time zone and web service times and query parameters use UTC by default.
*   Timezone for station?
*   Using an output timezone that uses [daylight saving time](https://en.wikipedia.org/wiki/Daylight_saving_time)
    will result in a one hour when no data exist (because time skips forward by an hour)
    and one hour each year when duplicate values exist (because time skips back by one hour).
    These discontinuties are not present in the UTC timeline, which does not use daylight saving,
    but will be present if local time are used during input and output.

TSTool is able to handle various timezone representations for the date/times used with the time series period of record
and individual time series values.
No (empty) timezone is also allowed,
which implies that the timezone is not important (e.g., for date) or is consistent (for date/time).
Constantly checking date/times for timezone compatibility in each command is a complication that is often ignored.
In most cases, the data for a workflow will have constant timezone because all data are local.
However, it is always best if the workflow clearly enforces consistent timezone.

The [`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command
and [`ReadAquarius`](../../command-ref/ReadAquarius/ReadAquarius.md)
`InputStart`, `InputEnd`, and `Timezone` command parameters set the input (read) period.

The timezone is handled as followed:

*   Timezones used in date/times in command parameters should use the timezone name (e.g., `America/Denver` or `UTC`),
    not UTC offset format (e.g., `+06:00`).
    This allows daylight saving time to be properly handled throughout the year and period of record.
    See the ["List of tz database time zones" on Wikipedia](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)
    "TZ Identifier" for a list of recognized timezones.
*   If the timezone is not specified in the input period,
    the Aquarius [`TSID`](../../command-ref/TSID/TSID.md) and
    [`ReadAquarius`](../../command-ref/ReadAquarius/ReadAquarius.md) commands
    will use the computer's timezone.
*   If a timezone is specified in the period or the
    [`ReadAquarius`](../../command-ref/ReadAquarius/ReadAquarius.md) command `Timezone` parameter,
    it will be used.
*   The `Timezone` parameter is provided as a convenience to override the input period time zone.
    It does not cause a shift in the date/time but simply overrides the timezone.
*   Each time series value's date/time is converted to the requested time zone for output.

## Limitations ##

The following limitations and design issues have been identified during development of the Aquarius plugin.
Additional software development is required to overcome these limitations.

1.  **Unique time series identifier:**
    1.  TSTool currently uses a combination of location ID, Aquarius parameter, and interval (from Aquarius computation period) to uniquely identify time series.
        It may be necessary to also consider other metadata for uniqueness.
3.  **Time period:**
    1.  The Aquarius API does not seem to restrict the period for queries.
    2.  Because queries of full historical period may result in slow performance,
        TSTool defaults the period to the most recent 30 days.
3.  **Regular interval time series:**
    1.  Regular interval time series are currently not handled by the TSTool Aquarius plugin.
4.  **Data flags:**
    1.  Data flags are not currently transferred to time series that are read.

## Datastore Configuration File ##

A datastore is configured by creating a datastore configuration file.

Create a user datastore configuration file `.tstool/NN/datastores/Aquarius.cfg` (or similar) in the user's files,
for example by copying and modifying the following example, or copying from another installation.
The `NN` should agree with the major TSTool version, for example `15` as shown by the ***Help / About TSTool*** menu.
TSTool will attempt to open datastores for all enabled configuration files.

The following illustrates the `AquariusDataStore` datastore configuration file format
and configures a datastore named `Aquarius-Panama`.
The `Name` property is the datastore name that will be used by the TSTool - the file name can be any name
but is often the same as the `Name` with extension `.cfg`.

```
# Configuration information for the Aquarius web service datastore.
# Properties are:
#
# Enabled - indicates if the datastore is enabled (active)
# ServiceApiDocumentationURL - URL for online API documentation
# Type - must be AquariusCloudDataStore to find proper software
#
# The user will see the following when interacting with the data store:
#
# Name - data store identifier used in applications, for example as the
#     input type information for time series identifiers (usually a short string)
# Description - data store description for reports and user interfaces (short phrase)
# ServiceRootURL - web service root URL, including the server name and root path

Enabled = True
Type = "AquariusDataStore"
Name = "Aquarius-Panama"
Description = "Aquarius web services for Panama Canal Authority (PCA)"
ServiceRootUrl = "https://panama.aquaticinformatics.net"
ServiceApiDocumentationUrl = "https://panama.aquaticinformatics.net/AQUARIUS/Publish/v2/docs/reference.html"
UserName = "XXXXXXXXX"
Password = "XXXXXXXXX"
Debug = True
```

**<p style="text-align: center;">
Aquarius Web Services Datastore Configuration File
</p>**

The following table describes configuration file properties.

**<p style="text-align: center;">
Aquarius Web Services Datastore Configuration File Properties
</p>**

| **Property**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; | **Description** | **Default** |
| -- | -- | -- |
| `Debug` | Used for troubleshooting. | `False` |
| `Description`<br>**required** | Description of the datastore, typically a short sentence, used in some displays. | None - must be specified. |
| `Enabled` | Indicates whether the datastore is enabled. | `True` |
| `Name`<br>**required** | Datastore name that is used in the TSTool software and Aquarius commands.  The name should be unique across all datastores. | None - must be specified. |
| `Password`<br>**required**</br> | The Aquarius web services account password. | None - must be specified. |
| `ServiceApiDocumentationUrl` | The URL for the web services API documentation, specific to the system.  This is used by software to display system-specific documentation. | Documentation will not be available from command editors. |
| `ServiceRootUrl`<br>**required** | The root URL for the web services.  This should only include the server name (or address). The API library will fill in the remaining parts of URLs for web service requests. | None - must be specified. |
| `Type`<br>**required** | Must be `AquariusDataStore`, which is used by TSTool to identify which plugin software to use for the datastore. | None - must be specified. |
| `UserName`<br>**required**</br> | The Aquarius web services account user name. | None - must be specified. |

## See Also 

*   [Aquarius TSID](../../command-ref/TSID/TSID.md) command
*   [`ReadDelimitedFile`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadDelimitedFile/ReadDelimitedFile/) command
*   [`ReadAquarius`](../../command-ref/ReadAquarius/ReadAquarius.md) command
*   [`ReadTableFromDelimitedFile`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTableFromDelimitedFile/ReadTableFromDelimitedFile/) command
*   [`ReadTableFromJSON`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTableFromJSON/ReadTableFromJSON/) command
*   [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/) command
