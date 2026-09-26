package se.dirac.acs.api

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import me.algorhythmics.diracui.toast

class UsecaseItem : Parcelable {
	val id: Int
	val name: String

	companion object {
		@JvmField
		val CREATOR: Parcelable.Creator<UsecaseItem> = UsecaseItemCreator()
	}

	constructor(id: Int, name: String) {
		this.id = id
		this.name = name
	}

	fun getOutput(): Output? {
		val up = name.uppercase()
		if (up.contains("HEADSET")) return Output.EXTERNAL
		else if (up.contains("SPEAKER") || up.contains("INTERNAL")) return Output.INTERNAL

		return null
	}

    override fun describeContents(): Int {
		return 0
    }

	override fun writeToParcel(parcel: Parcel, p1: Int) {
		parcel.writeString(this.name)
		parcel.writeInt(this.id)
	}

	class UsecaseItemCreator : Parcelable.Creator<UsecaseItem> {
        override fun createFromParcel(p: Parcel): UsecaseItem? {
			try {
				val name = p.readString() as String
				val id = p.readInt()
				return UsecaseItem(id, name)
			} catch (e: Exception) {
                Log.e("UsecaseItem", "Failed to decode UsecaseItem")
                toast("Failed to decode UsecaseItem")
				e.printStackTrace()
			}

			return null
        }

        override fun newArray(len: Int): Array<out UsecaseItem?> {
			return Array(len) {null}
        }
	}
}
