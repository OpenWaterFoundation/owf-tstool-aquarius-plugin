# TSTool / Install Aquarius Plugin #

This appendix describes how to install and configure the TSTool Aquarius Plugin.

*   [Install TSTool](#install-tstool)
*   [Install and Configure the TSTool Aquarius Web Services Plugin](#install-and-configure-the-tstool-aquarius-web-services-plugin)

-------

## Install TSTool ##

TSTool must be installed before installing the Aquarius plugin.
Typically the latest stable release should be used, although a development version can be installed to use new features.
Multiple versions of TSTool can be installed on the same computer.

1.  Download TSTool:
    *   Download the Windows version from the
        [State of Colorado's TSTool Software Downloads](https://opencdss.state.co.us/tstool/) page.
    *   Download the Linux version from the
        [Open Water Foundation TSTool download page](https://software.openwaterfoundation.org/tstool/).
2.  Run the installer and accept defaults.
3.  Run TSTool once by using the ***Start / CDSS / TSTool-Version*** menu on Windows
    (or run the `tstool` program on Linux).
    This will automatically create folders needed to install the plugin.

## Install and Configure the TSTool Aquarius Web Services Plugin ##

The plugin installation folder structure is as follows and is explained below.
The convention of using a version folder (e.g., `1.0.0`) was introduced in TSTool 15.0.0.

```
C:\Users\user\.tstool\NN\    (Windows)
/home/user/.tstool/NN/       (Linux)
  plugins/
    owf-tstool-aquarius-plugin/
      1.0.0/
        owf-tstool-aquarius-plugin-1.0.0.jar
        dep/
```

To install the plugin:

1.  TSTool must have been previously installed and run at least once.
    This will ensure that folders are properly created and, if appropriate,
    a previous version's files will be copied to a new major version run for the first time.
2.  Download the `tstool-aquarius-plugin` software installer file from the
    [TSTool Aquarius Plugin Download page](https://software.openwaterfoundation.org/tstool-aquarius-plugin/).
    For example with a name similar to `tstool-aquarius-plugin-1.0.0-win-202509251628.zip`.
3.  The plugin installation folders are as shown above.
    If installing the plugin in system files on Linux, install in the following folder:
    `/opt/tstool-version/plugins/`
4.  Copy files from the `zip` file to the `owf-tstool-aquarius-plugin` folder as shown in the above example:
    *   Windows:  Use File Explorer, 7-Zip, or other software to extract files.
    *   Linux:  Unzip the `zip` file to a temporary folder and copy the files.
5.  Configure one or more datastore configuration files:
    *   See the
        [Aquarius Web Services Datastore](../datastore-ref/Aquarius/Aquarius.md#datastore-configuration-file)
        documentation for information about the datastore configuration file format.
6.  Restart TSTool.
7.  Test web services access using TSTool by selecting the datastore name that was configured and selecting time series.
8.  If there are issues, use the ***View / Datastores*** menu item to list enabled datastores.
9.  If necessary, see the [Troubleshooting](../troubleshooting/troubleshooting.md) documentation.
10. For TSTool 15.0.0 and later, use the TSTool ***Tools / Plugin Manager*** menu to review installed plugins.
