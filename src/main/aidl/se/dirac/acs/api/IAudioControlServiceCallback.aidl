package se.dirac.acs.api;

import se.dirac.acs.api.Output;
import se.dirac.acs.api.OutputSettings;

interface IAudioControlServiceCallback {
	/** Unused */
    void onFilterAdd(long j, in int[] iArr);

	/** Unused */
    void onSyncDone();

	/** Unused */
    void onSetUser(String str);

    void onSettingsChanged(in Output output, in OutputSettings outputSettings);

	/** Unused */
    void onRoutingChanged(int i);
}
