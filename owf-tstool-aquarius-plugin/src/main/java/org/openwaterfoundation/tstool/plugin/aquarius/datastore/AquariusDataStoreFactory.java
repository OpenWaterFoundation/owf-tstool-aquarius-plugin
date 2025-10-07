// AquariusDataStoreFactory - class to create an AquariusDataStore instance

/* NoticeStart

OWF TSTool Aquarius Plugin
Copyright (C) 2025 Open Water Foundation

OWF TSTool Aquarius is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool Aquarius is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool Aquarius Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.aquarius.datastore;

import java.net.URI;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;

import riverside.datastore.DataStore;
import riverside.datastore.DataStoreFactory;

public class AquariusDataStoreFactory implements DataStoreFactory {

	/**
	Create an AquariusDataStore instance.
	@param props datastore configuration properties, such as read from the configuration file
	*/
	public DataStore create ( PropList props ) {  
	    String name = props.getValue ( "Name" );
	    String description = props.getValue ( "Description" );
	    if ( description == null ) {
	        description = "";
	    }
	    String serviceRootUrl = props.getValue ( "ServiceRootUrl" );
	    if ( serviceRootUrl == null ) {
	    	throw new RuntimeException("AquariusDataStore ServiceRootUrl is not defined.");
	    }
	    try {
	    	Message.printStatus(2, "", "ServiceRootUrl=\"" + serviceRootUrl + "\"");
	    	if ( ! serviceRootUrl.endsWith("/") ) {
	    		// Append a trailing slash:
	    		// - also reset in the original properties
	    		serviceRootUrl += "/";
	    		props.set("ServiceRootUrl", serviceRootUrl);
	    	}
	        DataStore ds = new AquariusDataStore ( name, description, new URI(serviceRootUrl), props );
	        return ds;
	    }
	    catch ( Exception e ) {
	        Message.printWarning(3,"",e);
	        throw new RuntimeException ( e );
	    }
	}
}