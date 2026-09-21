package se.dirac.acs.api

import java.util.Arrays

enum class Usecase(val value: Int) {
	INTERNAL_POWERSOUND(1),
	INTERNAL_PANORAMA(2),
	EXTERNAL_HEADSET(3),
	EXTERNAL_MRC(4);

	companion object {
		fun fromInt(i: Int): Usecase {
			if (i in 1..<5) return Usecase.entries[i - 1]

			throw IllegalArgumentException("unsupported value: $i")
		}
	}
}
