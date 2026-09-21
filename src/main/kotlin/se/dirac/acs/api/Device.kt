package se.dirac.acs.api

import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable

class Device : Parcelable {
	companion object {
		val CREATOR: Parcelable.Creator<Device> = DeviceCreator()
		val NOTHING_DEVICE: Device = Device(
			100, // Arbitrary id that doesnt conflict with anything else (probably)
			"Nothing", // Name
			System.currentTimeMillis(),
			List(1) {Filter.NOTHING_FILTER}
		)

		fun deviceFromParcel(parcel: Parcel): Device {
			try {
				val id = parcel.readLong()
				val name = parcel.readString()
				val timeAdded = parcel.readLong()
				val filters: List<Filter> = ArrayList()
				parcel.readTypedList(filters, Filter.CREATOR)
				return Device(id, name as String, timeAdded, filters)
			} catch (e: BadParcelableException) {
				throw e
			} catch (e2: Exception) {
				throw BadParcelableException(e2)
			}
		}
	}

    val id: Long
    val timeAdded: Long
    val filterAvailable: Boolean
    val filters: List<Filter>
    val name: String

    constructor(id: Long, name: String, timeAdded: Long, filters: List<Filter>) {
        this.id = id
        this.name = name
        this.timeAdded = timeAdded
        this.filters = filters.toList()
        this.filterAvailable = filters.isNotEmpty()
    }


    override fun describeContents(): Int {
        return 3
    }

    override fun toString(): String {
        val sb = StringBuilder("Device\n")
        sb.append("\tID: ").append(this.id).append("\n\tName: ").append(this.name).append("\n")

		for (filter in filters) {
			sb.append("\tFilter: ").append(filter).append("\n")
		}

        return sb.toString()
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeLong(this.id)
        parcel.writeString(this.name)
        parcel.writeLong(this.timeAdded)
        parcel.writeTypedList(this.filters)
    }

	class DeviceCreator : Parcelable.Creator<Device> {
		constructor()

        override fun createFromParcel(p: Parcel): Device {
			return Device.deviceFromParcel(p)
        }

        override fun newArray(length: Int): Array<out Device> {
			return Array(length) { Device.NOTHING_DEVICE }
        }
	}
}
