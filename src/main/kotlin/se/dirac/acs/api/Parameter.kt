package se.dirac.acs.api

import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable

class Parameter : Parcelable {
	companion object {
		@JvmField
		val CREATOR: Parcelable.Creator<Parameter> = ParameterCreator()

		const val STEREO_WIDTH_ID = 2
		const val TONAL_BALANCE_ID = 3
		const val LOUDNESS_ID = 4
	}

	var parameterId: Int = 0
	var parameterName: String? = null

	override fun describeContents(): Int {
		return 0
	}

	override fun writeToParcel(parcel: Parcel, i: Int) {
		parcel.writeInt(this.parameterId)
		parcel.writeString(this.parameterName)
	}

	class ParameterCreator : Parcelable.Creator<Parameter> {
		override fun createFromParcel(p: Parcel): Parameter? {
			try {
				return Parameter().apply {
					parameterId = p.readInt()
					parameterName = p.readString()
				}
			} catch (e: Exception) {
				return null
			}
		}

		override fun newArray(length: Int): Array<out Parameter?> {
			return Array(length) {null}
		}
	}
}
