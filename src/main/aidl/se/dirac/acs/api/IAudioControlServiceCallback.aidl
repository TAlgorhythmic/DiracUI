package se.dirac.acs.api;

import se.dirac.acs.api.Output;
import se.dirac.acs.api.OutputSettings;

interface IAudioControlServiceCallback {
	/** Unused */
    void onFilterAdd(long j, int[] iArr);

	/** Unused */
    void onSyncDone();

	/** Unused */
    void onSetUser(String str);

    void onSettingsChanged(Output output, OutputSettings outputSettings);

	/** Unused */
    void onRoutingChanged(int i);
}
