package org.openwaterfoundation.tstool.plugin.aquarius.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import RTi.Util.Time.DateTime;

/**
 * Time utilities used with Aquarius datastore.
 */
public class TimeUtil {
	
	/**
	 * Shared UTC time zone offset since commonly used:
	 * - "UTC" is not recognized by "Z" is
	 */
	//private static ZoneOffset ZONE_OFFSET_UTC = ZoneOffset.of("Z");

	/**
	 * Shared UTC time zone offset since commonly used:
	 * - "UTC" is not recognized by "Z" is
	 */
	private static ZoneId ZONE_ID_UTC = ZoneOffset.of("Z");

	/**
	 * Convert a UTC Epoch milliseconds to DateTime
	 * ZonedDateTime is used since a ZoneId is provided.
	 * @param instant timestamp as Instant in Utc (Aquarius internal time)
	 * @param zoneId the zone ID for the output DateTime, or null to use default Aquarius UTC
	 * @return DateTime object with in the requested timezone
	 */
	public static DateTime ofInstant ( Instant instant, ZoneId zoneId ) {
		// If necessary, convert the time zone.
		String tz = "UTC";
		if ( (zoneId != null) && zoneId.getRules().getOffset(instant).equals(ZoneOffset.UTC) ) {
			// Not UTC.
			//instant = Instant.of()
		}
		// Convert to the requested zone.
		ZonedDateTime zdt = null;
		if ( zoneId == null ) {
			// Convert the instant to ZonedDateTime in UTC.
			zdt = instant.atZone(ZONE_ID_UTC);
		}
		else {
			// Convert the instant to ZonedDateTime in the requested timezone.
			zdt = instant.atZone(zoneId);
			tz = zoneId.getId();
		}
		// Convert to a DateTime object, still in UTC.
		DateTime dt = new DateTime(DateTime.PRECISION_MILLISECOND);
		dt.setYear(zdt.getYear());
		dt.setMonth(zdt.getMonthValue());
		dt.setDay(zdt.getDayOfMonth());
		dt.setHour(zdt.getHour());
		dt.setMinute(zdt.getMinute());
		dt.setSecond(zdt.getSecond());
		dt.setNanoSecond(zdt.getNano());
		// The ID will be "UTC", "America/Denver", etc.
		dt.setTimeZone(tz);
		return dt;
	}

	/**
	 * Convert a UTC Epoch milliseconds to DateTime
	 * ZonedDateTime is used since a ZoneId is provided.
	 * @param timestamp UTC Epoch milliseconds
	 * @param zoneId the zone ID for the output DateTime
	 * @return DateTime object with in the requested timezone
	 */
	public static DateTime epochMilliToDateTime ( Long timestamp, ZoneId zoneId ) {
		// First get the OffsetDateTime for the requested offset.
		ZonedDateTime zdt = Instant.ofEpochMilli(timestamp).atZone(zoneId);
		// Convert to a DateTime object, still in UTC.
		DateTime dt = new DateTime(DateTime.PRECISION_MILLISECOND);
		dt.setYear(zdt.getYear());
		dt.setMonth(zdt.getMonthValue());
		dt.setDay(zdt.getDayOfMonth());
		dt.setHour(zdt.getHour());
		dt.setMinute(zdt.getMinute());
		dt.setSecond(zdt.getSecond());
		dt.setNanoSecond(zdt.getNano());
		// The ID will be "UTC", "America/Denver", etc.
		dt.setTimeZone(zoneId.getId());
		return dt;
	}

	/**
	 * Convert a UTC Epoch milliseconds to DateTime.
	 * OffsetDateTime is used since a ZoneOffeset is provided.
	 * @param timestamp UTC Epoch milliseconds
	 * @param zoneOffset the zone offset for the output DateTime
	 * @return DateTime object with in the requested timezone
	 */
	public static DateTime epochMilliToDateTime ( Long timestamp, ZoneOffset zoneOffset ) {
		// First get the OffsetDateTime for the requested offset.
		OffsetDateTime odt = Instant.ofEpochMilli(timestamp).atOffset(zoneOffset);
		// Convert to a DateTime object, now in the time zone of interest.
		DateTime dt = new DateTime(DateTime.PRECISION_MILLISECOND);
		dt.setYear(odt.getYear());
		dt.setMonth(odt.getMonthValue());
		dt.setDay(odt.getDayOfMonth());
		dt.setHour(odt.getHour());
		dt.setMinute(odt.getMinute());
		dt.setSecond(odt.getSecond());
		dt.setNanoSecond(odt.getNano());
		// The ID will be "UTC", "America/Denver", etc.
		dt.setTimeZone(zoneOffset.getId());
		return dt;
	}

    /**
     * Convert a DateTime to a Unix Epoch ms in UTC, which Aquarius API uses for period start and end.
     * @param dt DateTime instance to convert
     * @return an Unix Epoch number of ms
     */
    public static long toEpochMsUTC ( DateTime dt ) {
    	return toEpochSecondsUTC(dt)*1000;
    }

    /**
     * Convert a DateTime to a Unix Epoch seconds in UTC, which Aquarius API uses for period start and end.
     * @param dt DateTime instance to convert.  The time zone will be used or computer time if not specified.
     * @return an Unix Epoch number of seconds
     */
    public static long toEpochSecondsUTC ( DateTime dt ) {
    	// If the time zone is not specified, assume the local computer's time zone:
    	// - calling code should enforce a time zone
    	String dtTimeZone = dt.getTimeZoneAbbreviation();
    	//ZoneOffset zoneOffset = ZONE_OFFSET_UTC;
    	//ZoneId zoneId = x;
    	ZoneId zoneId = null;
    	if ( (dtTimeZone == null) || dtTimeZone.isEmpty() ) {
    		// Use the computer time by default.
    		zoneId = ZoneId.systemDefault();
    	}
    	else {
    		// Create a zone ID from the date/time zone:
    		// - TODO smalers 2025-09-29 might be able to cache this if it is a performance issue
    		zoneId = ZoneId.of(dtTimeZone);
    	}
    	// Create a ZonedDateTime.
    	ZonedDateTime zdt = ZonedDateTime.of (
    		dt.getYear(),
    		dt.getMonth(),
    		dt.getDay(),
    		dt.getHour(),
    		dt.getMinute(),
    		dt.getSecond(),
    		dt.getNanoSecond(),
    		zoneId
   		);
    	// Return the Epoch seconds, which will be in UTC.
    	return zdt.toEpochSecond();
    }

}