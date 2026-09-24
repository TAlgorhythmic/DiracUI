package se.dirac.acs.api

import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable

class Filter : Parcelable {
	companion object {
		val CREATOR: Parcelable.Creator<Filter> = FilterCreator()
		var INTERNAL_FILTER: Filter = Filter(
			id = -1, // Arbitrary id
			name = "Nothing",
			vendor = "Nobody",
			Usecase.INTERNAL_POWERSOUND,
			sfxAvailable = true,
			eqAvailable = true,
			licence = 3,
			productId = "NO_ID"
		)

		fun filterFromParcel(parcel: Parcel): Filter {
			try {
				return Filter(
					parcel.readLong(),
					requireNotNull(parcel.readString()),
					requireNotNull(parcel.readString()),
					Usecase.fromInt(parcel.readInt()),
					parcel.readByte() != 0.toByte(),
					parcel.readByte() != 0.toByte(),
					parcel.readInt(),
					requireNotNull(parcel.readString()),
				)
			} catch (e: BadParcelableException) {
				throw e
			} catch (e2: Exception) {
				throw BadParcelableException(e2)
			}
		}
	}
    val id: Long
	val usecase: Usecase
    val sfxAvailable: Boolean
    val eqAvailable: Boolean
    val vendor: String
    val license: Int
    val productID: String
    val name: String

	constructor(
		id: Long,
		name: String,
		vendor: String,
		usecase: Usecase,
		sfxAvailable: Boolean,
		eqAvailable: Boolean,
		licence: Int,
		productId: String
	) {
        this.id = id
        this.name = name
        this.vendor = vendor
        this.usecase = usecase
        this.sfxAvailable = sfxAvailable
        this.eqAvailable = eqAvailable
        this.license = licence
        this.productID = productId
    }

    override fun describeContents(): Int {
        return 2
    }

    override fun toString(): String {
        return this.name
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeLong(this.id)
        parcel.writeString(this.name)
        parcel.writeString(this.vendor)
        parcel.writeInt(this.usecase.value)
        parcel.writeByte(if (this.sfxAvailable) 1 else 0)
        parcel.writeByte(if (this.eqAvailable) 1 else 0)
        parcel.writeInt(this.license)
        parcel.writeString(this.productID)
    }

	class FilterCreator : Parcelable.Creator<Filter> {
		constructor()

        override fun createFromParcel(p: Parcel): Filter {
            return Filter.filterFromParcel(p)
        }

        override fun newArray(length: Int): Array<out Filter> {
			return Array(length) {Filter.INTERNAL_FILTER}
        }
	}
}
