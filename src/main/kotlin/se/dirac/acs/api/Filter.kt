package se.dirac.acs.api

import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable

class Filter : Parcelable {
	companion object {
		@JvmField
		val CREATOR: Parcelable.Creator<Filter> = FilterCreator()
		var INTERNAL_FILTER: Filter = Filter(
			id = -1, // Arbitrary id
			name = "Nothing",
			vendor = "Nobody",
            UsecaseItem(0, "Something"),
			sfxAvailable = true,
			licence = 3,
			productId = "NO_ID",
			bandCount = 10
		)

		fun filterFromParcel(parcel: Parcel): Filter {
			try {
				val id = parcel.readLong()
				val name = parcel.readString()
				val vendor = parcel.readString()
				val usecase = parcel.readParcelable(UsecaseItem::class.java.classLoader, UsecaseItem::class.java)
					?: throw BadParcelableException("No valid usecase in parcel")
				val sfxAvailable = parcel.readByte() != 0.toByte()
				val bandCount = parcel.readInt()
				val licence = parcel.readInt()
				val productID = parcel.readString()
				return Filter(
					id, 
					requireNotNull(name),
					requireNotNull(vendor),
					usecase,
					sfxAvailable,
					bandCount,
					licence,
					requireNotNull(productID),
				)
			} catch (e: BadParcelableException) {
				throw e
			} catch (e2: Exception) {
				throw BadParcelableException(e2)
			}
		}
	}
    val id: Long
	var usecase: UsecaseItem
    val sfxAvailable: Boolean
    val vendor: String
    val license: Int
    val productID: String
    val name: String
	val bandCount: Int

	constructor(
		id: Long,
		name: String,
		vendor: String,
		usecase: UsecaseItem,
		sfxAvailable: Boolean,
		bandCount: Int,
		licence: Int,
		productId: String
	) {
        this.id = id
        this.name = name
        this.vendor = vendor
        this.usecase = usecase
        this.sfxAvailable = sfxAvailable
		this.bandCount = bandCount
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
        parcel.writeParcelable(this.usecase, i)
        parcel.writeByte(if (this.sfxAvailable) 1.toByte() else 0.toByte())
        parcel.writeInt(this.bandCount)
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
