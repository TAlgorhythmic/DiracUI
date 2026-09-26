package se.dirac.acs.api;

import android.os.Bundle;

import se.dirac.acs.api.Device;
import se.dirac.acs.api.Output;
import se.dirac.acs.api.OutputSettings;
import se.dirac.acs.api.UsecaseItem;
import se.dirac.acs.api.Parameter;
import se.dirac.acs.api.IAudioControlServiceCallback;

/**
* WARNING: This is essentially the protocol, so even if most of the stuff here is unused
* it needs to be here.
* Modifying it could break the protocol.
*/
interface IAudioControlService {

	/** Fetch a device name and optional tech types (e.g. eq, sfx, etc) from a hardcoded
	* `diracvdd.bin` file that I guess gets shipped with a stock rom.
	* The diracvdd.bin i got from oppo has only 1 entry, probably worth using
	*
	* @param locale Device name can be translated, it fallbacks to "en" so just pass "en" to avoid having to try something that doesnt exist
	*/
	Device getDevice(long id, String locale);

	/**
	* @param locale just pass "en"
	*/
	Device getDeviceByProductId(String productId, String locale);

	/**
	* Lists all devices from a specific output
	*
	* @param locale just pass "en".
	* @param output either INTERNAL or EXTERNAL. INTERNAL will likely return a singleton with the speaker, EXTERNAL will actually fetch the list of devices in the database (provided by diracvdd.bin)
	*/
	List<Device> listDevices(String locale, in Output output);

	/**
	* Returns additional arbitrary data vendors likely put relevant data for them?
	* Not sure, oppo's single device profile is empty on this end anyways so probably not important.
	*/
	byte[] getDeviceVendorData(long id);

	/**
	* Change the output settings.
	* This is the main route of changing audio settings, since most of them live in OutputSettings
	*
	* @return whether it was applied or not
	*/
	boolean setOutput(in OutputSettings output);

	/**
	* Disable output type sort of? Easier to just use setOutput for everything.
	*/
	void setDisabled(in Output output);

	/**
	* Delete a device.
	* This creates a bug on the service, dont call it.
	*/
	boolean deleteDevice(long id);

	OutputSettings getCurrentOutputSettings(in Output output);

	/**
	* Check whether a user has a licence, even though there is no user system in this app
	*/
	boolean userHasLicence(String user);

	/**
	* More user stuff, syncs licences. Unused.
	*/
	int requestSync();

	String getUser();

	boolean setUser(String str, String str1);

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

	/**
	* These are basically the same as above but they account for safe mode,
	* To avoid unknown errors, probably worth using
	* You pass new Bundle(), and after executing it will be filled with errors if any
	*/
	Device getDevice2(long id, String locale, out Bundle status);
	Device getDeviceByProductId2(String productId, String locale, out Bundle status);
	List<Device> listDevices2(String locale, in Output output, out Bundle status);
	byte[] getDeviceVendorData2(long id, out Bundle status);
	boolean setOutput2(in OutputSettings output, out Bundle status);
	void setDisabled2(in Output output, out Bundle status);
	boolean deleteDevice2(long id, out Bundle status);
	OutputSettings getCurrentOutputSettings2(in Output output, out Bundle status);
	boolean userHasLicence2(String user, out Bundle status);
	int requestSync2(out Bundle status);
	String getUser2(out Bundle status);
	boolean setUser2(String str, String str1, out Bundle status);
	int getCurrentRouting2(out Bundle status);
	void registerCallback2(IAudioControlServiceCallback callback, out Bundle status);
	void unregisterCallback2(IAudioControlServiceCallback callback, out Bundle status);
	String getInstallationId2(out Bundle status);

	boolean isSafeMode();
	void killServer();

	/**
	* Sets one individual DSP parameter to a value. This is not part of OutputSettings,
	* so it's the only way to set stereo width and tonal balance.
	*
	* Known ids:
	*   STEREO_WIDTH_ID = 2
	*   TONAL_BALANCE_ID = 3
	*   LOUDNESS_ID = 4
	*
	* @param item addressing only; in practice just its Output
	*/
	boolean setParameter(in UsecaseItem item, int paramId, float value, out Bundle status);

	float getParameter(in UsecaseItem item, int i, out Bundle status);

	List<Parameter> listParameters(in UsecaseItem item, out Bundle status);
	boolean setDiracEnabled(boolean enabled, out Bundle status);

	/**
	* This likely only sets INTERNAL_POWERSOUND to INTERNAL_POWERSOUND_GAME and same with external.
	* Unused, you can set game mode by changing filter usecase and using setOutput
	*/
	boolean setGameModeEnabled(boolean enabled, out Bundle status);
	boolean isGameModeEnabled();
	boolean hasAlternativeTuningInternal();
	boolean hasAlternativeTuningExternal();
	OutputSettings getOutputSettings(in UsecaseItem item, long id, out Bundle status);
	List<UsecaseItem> listUsecases(in Output output, out Bundle status);
}
