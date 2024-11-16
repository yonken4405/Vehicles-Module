import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AddOn(
    val name: String,
    val price: Double,
    val estimatedTime: Int // Estimated time in hours
) : Parcelable
