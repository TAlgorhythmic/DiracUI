package se.dirac.acs.api

import android.database.Cursor
import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable
import me.algorhythmics.App

class OutputSettings : Parcelable {
	companion object {
		@JvmField
		val CREATOR: Parcelable.Creator<OutputSettings> = OutputSettingsCreator()

		const val PARAM_STEREO_WIDTH = 2
		const val PARAM_TONAL_BALANCE = 3
		const val LOUDNESS = 4
	}

	var enabled: Boolean = false
	var filterEnabled: Boolean = false
	var sfxEnabled: Boolean = false
	var eqEnabled: Boolean = false
	var eqBands: FloatArray = FloatArray(7)
	var device: Device = Device.INTERNAL_DEVICE
	var filter: Filter = Filter.INTERNAL_FILTER

	// These two are not included in the parcel, just stored as a state
	// To update these you need bind.setParameter(PARAM_STEREO_WIDTH, value)
	var stereoWidth: Float = 0.0f
	var tonalBalance: Float = 0.0f
	var loudness: Float = 0.0f

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
		filter = instance.filters.getOrDefault(c.getInt(c.getColumnIndex("filter")).toLong(), Filter.INTERNAL_FILTER)
		device = if (filter.id.toInt() == -1) Device.INTERNAL_DEVICE
			else instance.devices.getOrDefault(c.getInt(c.getColumnIndex("device")).toLong(), Device.INTERNAL_DEVICE)

		// If issues arise, disallow external usecases in internal and viceversa
		enabled = c.getInt(c.getColumnIndex("enabled")) != 0
		filterEnabled = c.getInt(c.getColumnIndex("filterEnabled")) != 0
		sfxEnabled = filter.sfxAvailable && c.getInt(c.getColumnIndex("sfxEnabled")) != 0
		eqEnabled = filter.bandCount > 0 && c.getInt(c.getColumnIndex("eqEnabled")) != 0
		eqBands = c.getString(c.getColumnIndex("bands")).split(';').map { v: String -> v.toFloat() }.toFloatArray()
		stereoWidth = c.getFloat(c.getColumnIndex("stereoWidth"))
		tonalBalance = c.getFloat(c.getColumnIndex("tonalBalance"))
		loudness = c.getFloat(c.getColumnIndex("loudness"))
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
