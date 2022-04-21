import java.time.Instant
import java.util.Objects

class PrivateMessage(
    val senderID: Int,
    val receiverID: Int,
    var text: String,
    var attachment: Attachment? = null
) {

    val date = Instant.now().toEpochMilli()
    val id = Database.createNumber()
    var isRead: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other == null || other is PrivateMessage) return false
        return (this == other as PrivateMessage && this.senderID == other.senderID
                && this.receiverID == other.receiverID && this.text == other.text && this.attachment
                == other.attachment && this.id == other.id && this.date == other.date)
    }

    override fun hashCode(): Int {
        return Objects.hash(senderID, receiverID, date, id)
    }
}