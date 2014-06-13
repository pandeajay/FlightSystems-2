package datamodel;

import java.util.List;
import java.util.Map;

public class Flight {
	String flightId;
	AirLineCompany cpid;
	String details;	
	String typeId;
	AirLineCompany airLine;
	Map<ClassAvailable, Integer> seatAvailability;
	List<FromToCity> fromToCityList;

}
