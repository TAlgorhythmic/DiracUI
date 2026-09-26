package se.dirac.acs.api

import java.util.Arrays

enum class Usecase(val value: Int) {
	INTERNAL_POWERSOUND(1),
	EXTERNAL_HEADSET(3),
	INTERNAL_POWERSOUND_GAME(10),
	EXTERNAL_HEADSET_GAME(13);

	companion object {
		fun fromInt(i: Int): Usecase {
			if (i in 1..<5) return Usecase.entries[i - 1]

			throw IllegalArgumentException("unsupported value: $i")
		}
	}
}
