# TSTool / Command / ReadAquarius #

*   [Overview](#overview)
*   [Command Editor](#command-editor)
    +   [Match Single Time Series](#match-single-time-series)
    +   [Match 1+ Time Series](#match-1-time-series)
*   [Command Syntax](#command-syntax)
*   [Examples](#examples)
*   [Troubleshooting](#troubleshooting)
*   [See Also](#see-also)

-------------------------

## Overview ##

The `ReadAquarius` command reads one or more time series from Aquarius web services:

*   Read a single time series by matching a TSTool time series identifier (TSID).
*   Read 1+ time series using filters similar to the main TSTool window.

See the [Aquarius Web Services Appendix](../../datastore-ref/Aquarius/Aquarius.md)
for more information about Aquarius web service integration and limitations.
The command is designed to utilize web service query criteria to process large numbers of time series,
for example to produce real-time information products and perform historical data analysis and quality control.

See also the 
[TSID for Aquarius](../TSID/TSID.md) time series identifier command,
which reads time series for a single time series.

The ***Data type***, ***Data interval***, and ***Where*** command parameters and input fields
are similar to those in the main TSTool interface.
However, whereas the main TSTool interface first requires a query to find the
matching time series list and interactive select to copy specific time series identifiers into the ***Commands*** area,
the `ReadAquarius` command automates reading the time series list and the corresponding data for the time series.

Using the `ReadAquarius` command can greatly shorten command files and simplify command logic
when processing many time series.
However, because the command can process many time series and web services are impacted by network speed,
running the command can take a while to complete.
Checks are in place to constrain queries,
for example defaulting to 30 days if the period is not specified.

Data for the location and other time series metadata,
as shown in the main TSTool interface, are set as time series properties, using web service data values.
Right-click on a time series in the TSTool ***Results*** area and then use the
***Time Series Properties*** menu to view time series properties.
These properties can be transferred to a table with the
[`CopyTimeSeriesPropertiesToTable`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/CopyTimeSeriesPropertiesToTable/CopyTimeSeriesPropertiesToTable/)
command and processed further with other table commands.

The Aquarius time series computation period indicates whether time series values are instantaneous or interval time series.
TSTool represents time series with instantaneous measurements as irregular interval (`IrregSecond`) time series.
Regular interval time series use an
where the timestamp is the interval-ending date/time (e.g., `15Minute`, `1Hour`),
        
## Command Editor ##

The following dialog is used to edit the command and illustrates the syntax for the command.
Two options are available for matching time series.

### Match Single Time Series ###

The following example illustrates how to read a single time series by specifying the data type and interval (top)
and station identifier (***Match Single Time Series*** tab).
This approach is similar to using the general
[`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/)
command but offers parameters specific to Aquarius web services.

**<p style="text-align: center;">
![ReadAquarius-single](ReadAquarius-single.png)
</p>**

**<p style="text-align: center;">
`ReadAquarius` Command Editor to Read a Single Time Series (<a href="../ReadAquarius-single.png">see full-size image)</a>
</p>**

### Match 1+ Time Series ###

The following figure illustrates how to query multiple time series.
For example, this can be used to process all time series of a data type in the system
or all time series for a location.

**<p style="text-align: center;">
![ReadAquarius-multiple](ReadAquarius-multiple.png)
</p>**

**<p style="text-align: center;">
`ReadAquarius` Command Editor to Read Multiple Time Series (<a href="../ReadAquarius-multiple.png">see full-size image)</a>
</p>**

## Command Syntax ##

The command syntax is as follows:

```text
ReadAquarius(Parameter="Value",...)
```

**<p style="text-align: center;">
Command Parameters
</p>**

|**Tab**|**Parameter**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|**Description**|**Default**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|
|--------------|-----------------|-----------------|--|
|All|`DataStore`<br>**required**|The Aquarius datastore name to use for the web services connection, as per datastore configuration files (see the [Aquarius Web Services Datastore appendix](../../datastore-ref/Aquarius/Aquarius.md)). | None - must be specified. |
||`DataType`<br>**required**|The data type to be queried, corresponding to Aquarius parameter. | `*` to read all the time series. |
||`Interval`<br>**required**|The data interval for the time series, corresponding to Aquarius computation period, using TSTool naming conventions. | `*` - to read all the time series. |
|***Match Single Time Series***|`StationId`<br>**required**|The station identifier, corresponding to Aquarius location identifier. | None - must be specified to read a single time series. |
||`TSID`| A view-only value that indicates the time series identifier that will result from the input parameters when a single time series is queried. | |
|***Match 1+ Time Series***|`WhereN`|When reading 1+ time series, the “where” clauses to be applied.  The filters match the values in the Where fields in the command editor dialog and the TSTool main interface.  The parameters should be named `Where1`, `Where2`, etc., with a gap resulting in the remaining items being ignored.  The format of each value is:<br>`Item;Operator;Value`<br>Where `Item` indicates a data field to be filtered on, `Operator` is the type of constraint, and `Value` is the value to be checked when querying.|If not specified, the query will not be limited and large numbers of time series may be queried.|
|All|`DataApi`| The API to use when reading time series data points:<ul><li>`Corrected` - corrected values</li><li>`Raw` - raw values</li></ul>|`Corrected` (if appropriate). |
|All|`Alias`<br>|The alias to assign to the time series, as a literal string or using the special formatting characters listed by the command editor.  The alias is a short identifier used by other commands to locate time series for processing, as an alternative to the time series identifier (`TSID`).|None – alias not assigned.|
||`InputStart`|Start of the period to query, specified as a date/time with a precision that matches the requested data interval.  The timezone will default to the computer. |Read most recent 30 days of data.|
||`InputEnd`|End of the period to query, specified as a date/time with a precision that matches the requested data interval.  The timezone will default to the computer. | |Read most recent 30 days of data. |
||`IrregularInterval` | Used with regular interval time series in cases where an interval is not yet supported (e.g., month and year) or there are data complexities, such as daily interval time series that do not align with midnight.  The resulting time series will have irregular interval (spacing) and date/time precision will match the interval (e.g., `IrregHour` will use hourly time precision). The parameter can have one of the following values.  High precision time is typically only used in special cases. <ul><li>`IrregYear`</li><li>`IrregMonth`</li><li>`IrregDay`</li><li>`IrregHour`</li><li>`IrregMinute`</li><li>`IrregSecond`</li><li>`IrregHSecond`</li><li>`IrregMilliSecond`</li><li>`IrregMicroSecond`</li><li>`IrregNanoSecond`</li></ul>| `IrregSecond` |
||`Timezone`| Timezone for `InputStart` and `InputEnd` and output time series. The Aquarius UTC times will be converted to the requested timezone. | Output will use the local time zone for the computer. |
||`Debug`| Used for troubleshooting:  `False` or `True`. | `False` |

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-aquarius-plugin/tree/main/test/commands/ReadAquarius/).

## Troubleshooting ##

Refer to the TSTool log file.  Use the
[`SetDebugLevel`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetDebugLevel/SetDebugLevel/) command
to increase the amount of output for debugging.

## See Also ##

*   [`CopyTimeSeriesPropertiesToTable`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/CopyTimeSeriesPropertiesToTable/CopyTimeSeriesPropertiesToTable/) command
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command
*   [TSID for Aquarius](../TSID/TSID.md) command
*   [`WebGet`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/WebGet/WebGet/) command
