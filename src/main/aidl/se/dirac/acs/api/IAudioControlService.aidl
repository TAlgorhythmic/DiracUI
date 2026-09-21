package se.dirac.acs.api;

import se.dirac.acs.api.Device;
import se.dirac.acs.api.Output;
import se.dirac.acs.api.OutputSettings;
import se.dirac.acs.api.IAudioControlServiceCallback;

/**
* WARNING: This is essentially the protocol, so even if most of the stuff here is unused
* it needs to be here.
* Modifying it could break the protocol.
*/
interface IAudioControlService {

	/** Fetch a device name and optional tech types (e.g. eq, sfx, etc) from a hardcoded
	* `diracvdd.bin` file that I guess gets shipped with a stock rom.
	* The diracvdd.bin i from oppo has only 1 entry, probably worth using
	*
	* @param locale Device name can be translated, it fallbacks to "en" so just pass "en" to avoid having to try something that doesnt exist
	*/
	Device getDevice(long id, String locale);

	/**
	* @param locale just pass "en"
	*/
	Device getDeviceByProductId(String productId, String locale)

	/**
	* Lists all devices from a specific output
	*
	* @param locale just pass "en".
	* @param output either INTERNAL or EXTERNAL. INTERNAL will likely return a singleton with the speaker, EXTERNAL will actually fetch the list of devices in the database
	*/
	List<Device> listDevices(String locale, Output output);

	/**
	* Returns additional arbitrary data vendors likely put relevant data for them?
	* Not sure, oppo's single device profile is empty on this end anyways so probably not important.
	*/
	byte[] getDeviceVendorData(long id);

	/**
	* Change the output profile settings,
	* useful for when the user plugs headphones or connects to bluetooth.
	*
	* @return whether it was applied or not
	*/
	boolean setOutput(OutputSettings output);

	/**
	* Disable output type sort of? Easier to just use setOutput for everything.
	*/
	void setDisabled(Output output);

	/**
	* Delete a device.
	* Not sure why you would do that since dirac servers are long dead but there it is.
	*/
	boolean deleteDevice(long id);

	OutputSettings getCurrentOutputSettings(Output output);

	/**
	* Check whether a user has a licence, even though there is no user system in this app
	*/
	boolean userHasLicence(String user);

	/**
	* More user stuff, syncs licences. Unused.
	*/
	int requestSync();

	String getUser();

	/**
	* WARNING! Never call this, it's unimplemented in the service.
	* Calling it just throws an exception on the control service.
	*/
	int getCurrentRouting();

	/**
	* Listen for events the service sends (the only event ever sent is onSettingsChange)
	*/
	void registerCallback(IAudioControlServiceCallback callback);

	void unregisterCallback(IAudioControlServiceCallback callback);

	/**
	* Unused.
	*/
	String getInstallationId();
}
