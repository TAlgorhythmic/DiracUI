package se.dirac.acs.api

import android.os.Parcel
import android.os.Parcelable

enum class Output : Parcelable {
    INTERNAL,
    EXTERNAL;

	companion object {
		@JvmField
		val CREATOR: Parcelable.Creator<Output> = object : Parcelable.Creator<Output> {
            override fun createFromParcel(p: Parcel): Output {
				return Output.entries[p.readInt()]
            }

            override fun newArray(len: Int): Array<out Output?> {
				return Array(len) { null }
            }
        }
	}

	override fun writeToParcel(parcel: Parcel, i: Int) {
		parcel.writeInt(this.ordinal)
    }

    override fun describeContents(): Int {
		return 0
    }
}
