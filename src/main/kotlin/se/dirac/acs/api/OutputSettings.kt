package se.dirac.acs.api

import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable

class OutputSettings : Parcelable {
	companion object {
		val CREATOR: Parcelable.Creator<OutputSettings> = OutputSettingsCreator()
	}

	var enabled: Boolean = false
	var filterEnabled: Boolean = false
	var sfxEnabled: Boolean = false
	var eqEnabled: Boolean = false
	var eqBands: FloatArray = FloatArray(7)
	var device: Device = Device.NOTHING_DEVICE
	var filter: Filter = Filter.NOTHING_FILTER

	constructor(device: Device, filter: Filter) {
		this.device = device
		this.filter = filter

		if (!device.filters.contains(filter)) {
			throw IllegalArgumentException("invalid filter")
		}
	}

	constructor(parcel: Parcel) {
		this.eqBands = FloatArray(7)
		try {
			this.device = parcel.readParcelable(Device::class.java.classLoader, Device::class.java)
			?: throw BadParcelableException("No valid device in parcel")
			val flags = BooleanArray(4)
			parcel.readBooleanArray(flags)
			this.enabled = flags[0]
			this.filterEnabled = flags[1]
			this.sfxEnabled = flags[2]
			this.eqEnabled = flags[3]
			parcel.readFloatArray(this.eqBands)
			this.filter = parcel.readParcelable(Filter::class.java.classLoader, Filter::class.java)
			?: throw BadParcelableException("No valid filter")
		} catch (e: BadParcelableException) {
			throw e
		} catch (e2: Exception) {
			throw BadParcelableException(e2)
		}
	}

	override fun describeContents(): Int {
		return 1
	}

	override fun writeToParcel(parcel: Parcel, i: Int) {
		parcel.writeParcelable(this.device, 0)
		parcel.writeBooleanArray(booleanArrayOf(this.enabled, this.filterEnabled, this.sfxEnabled, this.eqEnabled))
		parcel.writeFloatArray(this.eqBands)
		parcel.writeParcelable(this.filter, 0)
	}

	class OutputSettingsCreator : Parcelable.Creator<OutputSettings> {
		override fun createFromParcel(p: Parcel): OutputSettings {
			return OutputSettings(p)
		}

		override fun newArray(length: Int): Array<out OutputSettings?> {
			return Array(length) {null}
		}
	}
}
