# TSTool / Command / TSID for Aquarius #

*   [Overview](#overview)
*   [Command Editor](#command-editor)
*   [Command Syntax](#command-syntax)
*   [Examples](#examples)
*   [Troubleshooting](#troubleshooting)
*   [See Also](#see-also)

-------------------------

## Overview ##

The TSID command for Aquarius causes a single time series to be read from Aquarius web services using default parameters.
A TSID command is created by copying a time series from the ***Time Series List*** in the main TSTool interface
to the ***Commands*** area.
TSID commands can also be created by editing the command file with a text editor.

See the [Aquarius Web Services Datastore Appendix](../../datastore-ref/Aquarius/Aquarius.md) for information about TSID syntax.

See also the [`ReadAquarius`](../ReadAquarius/ReadAquarius.md) command,
which reads one or more time series and provides parameters for control over how data are read.

If the input period is not specified with the
[`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command,
the default is to read the last 30 days.
If a period is specified but the time zone is not specified, the time zone for the computer is used.

The TSTool Aquarius plugin automatically manipulates time series timestamps to be consistent
with TSTool, as follows:

*   Irregular interval time series:
    +   use timestamps from Aquarius web services without changing
*   Regular interval time series:
    +   Aquarius web service timestamps correspond to the interval-end
        
## Command Editor ##

All TSID commands are edited using the general
[`TSID`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/TSID/TSID/)
command editor.

## Command Syntax ##

See the [Aquarius Web Services Datastore Appendix](../../datastore-ref/Aquarius/Aquarius.md) for information about TSID syntax.

## Examples ##

See the [automated tests](https://github.com/OpenWaterFoundation/owf-tstool-aquarius-plugin/tree/main/test/commands/TSID/).

## Troubleshooting ##

*   See the [`ReadAquarius` command troubleshooting](../ReadAquarius/ReadAquarius.md#troubleshooting) documentation.

## See Also ##

*   [`ReadAquarius`](../ReadAquarius/ReadAquarius.md) command for full control reading Aquariustime series
*   [`ReadTimeSeries`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/ReadTimeSeries/ReadTimeSeries/) command - provides more flexibility than a TSID
*   [`SetInputPeriod`](https://opencdss.state.co.us/tstool/latest/doc-user/command-ref/SetInputPeriod/SetInputPeriod/) command - to set the period to read
