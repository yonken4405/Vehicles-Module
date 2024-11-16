import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TimeSlot(
    val time: Int, // Time in 24-hour format (6-16)
    val isAvailable: Boolean
) : Parcelable
