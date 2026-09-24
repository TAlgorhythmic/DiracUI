package se.dirac.acs.api

import android.database.Cursor
import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable
import me.algorhythmics.App
import java.util.EnumSet

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
	var filter: Filter = Filter.INTERNAL_FILTER

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

	constructor(c: Cursor) {
		val instance = App.getInstance()
		filter = instance.filters.getOrDefault(c.getInt(c.getColumnIndex("filter")).toLong(), Filter.NOTHING_FILTER)
		device = if (filter.id.toInt() == -1) Device.NOTHING_DEVICE
			else instance.devices.getOrDefault(c.getInt(c.getColumnIndex("device")).toLong(), Device.NOTHING_DEVICE)

		// If issues arise, disallow external usecases in internal and viceversa
		enabled = c.getInt(c.getColumnIndex("enabled")) != 0
		filterEnabled = c.getInt(c.getColumnIndex("filterEnabled")) != 0
		sfxEnabled = filter.sfxAvailable && c.getInt(c.getColumnIndex("sfxEnabled")) != 0
		eqEnabled = filter.eqAvailable && c.getInt(c.getColumnIndex("eqEnabled")) != 0
		eqBands[0] = c.getFloat(c.getColumnIndex("band0"))
		eqBands[1] = c.getFloat(c.getColumnIndex("band1"))
		eqBands[2] = c.getFloat(c.getColumnIndex("band2"))
		eqBands[3] = c.getFloat(c.getColumnIndex("band3"))
		eqBands[4] = c.getFloat(c.getColumnIndex("band4"))
		eqBands[5] = c.getFloat(c.getColumnIndex("band5"))
		eqBands[6] = c.getFloat(c.getColumnIndex("band6"))
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
