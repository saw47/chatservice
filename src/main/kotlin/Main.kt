fun main() {

    println("_______________________ create user pull________________________________________")

    val members = mutableListOf<Member>()
    while (members.size < 5) {
        members.add(Member(members.size))
    }

    members.forEach { println("index ${it.id}") }

    println("________________________list of keys___________________________________________")
    for (i in 0..6) {
        for (j in 0..6) {
            Service.createNewChat(j, i)
        }
        println(Database.storage.keys.toString())
    }

    for (i in 0..6) {
        for (j in 0..6) {
            if (i != j) {
                val key = Pair(j, i)
                while (Database.storage[key]!!.size < 10) {
                    Service.createMessage(
                        senderId = j,
                        receiverID = i,
                        text = "origin message text"
                    )
                    Thread.sleep(1)
                }
            }
        }
    }

    println("________________________list of messages_________________________________________")
    for (key in Database.storage.keys) {
        println("|${key.first} - ${key.second}|")
    }

    println("________________________list of ID message_________________________________________")
    for (key in Database.storage.keys) {
        Database.storage[key]?.forEach { print("${it.id} || ") }
        println()
    }


    val editedMessageId: Long = Database.storage[Pair(2, 3)]?.get(3)?.id ?: throw Exception("Thread not found")
    println("________________________old message_________________________________________")
    println(
        "old text: ${Database.storage[Pair(2, 3)]?.get(3)?.text} " +
                "|| old attach: ${Database.storage[Pair(2, 3)]?.get(3)?.attachment}" +
                " || message ID: ${Database.storage[Pair(2, 3)]?.get(3)?.id}"
    )

    println(
        "new text: ${Database.storage[Pair(2, 3)]?.get(3)?.text} ||" +
                " old attach: ${Database.storage[Pair(2, 3)]?.get(3)?.attachment}" +
                "|| message ID: ${Database.storage[Pair(2, 3)]?.get(3)?.id}"
    )

    println("________________________read message_________________________________________")
    println("________________________before read_________________________________________")
    println("${Database.storage[Pair(1, 2)]?.get(3)?.isRead}")
    Database.storage[Pair(1, 2)]?.get(3)!!.let { Service.readMessage(it) }
    println("________________________after read_________________________________________")
    println("${Database.storage[Pair(1, 2)]?.get(3)?.isRead}")

    println("________________________mark all as read_________________________________________")
    Database.storage[Pair(1, 2)]?.forEach { print("_${it.isRead}") }
    println()

    Service.tagChatLikeARead(Pair(1, 2))
    println("________________________after mark all as read_________________________________________")
    Database.storage[Pair(1, 2)]?.forEach { print("_${it.isRead}") }
    println()

    println("________________________before delete_________________________________________")
    Database.storage[Pair(0, 3)]?.forEach { print("${it.id};") }
    println()

    println("________________________after delete_________________________________________")
    val result = Service.deleteMessage(Database.storage[Pair(0, 3)]!![0])
    Database.storage[Pair(0, 3)]?.forEach { print("${it.id};") }
    println()
    println("________________________________________________________________________________")
    println("result delete - $result")

    println("________________________before chat delete_________________________________________")
    println(Database.storage[Pair(3, 4)].isNullOrEmpty())

    println("________________________after delete chat_________________________________________")
    val deleteChat = Service.deleteChat(3,4)
    println(Database.storage[Pair(3, 4)].isNullOrEmpty())
    println(deleteChat)
    println(Database.storage[Pair(3, 4)])

    println("________________________before read counter start_________________________________________")
    println(Service.getUnreadChatsCount(4))
    Database.storage[Pair(0, 4)]!!.forEach { it.isRead = true }
    Database.storage[Pair(1, 4)]!!.forEach { it.isRead = true }
    println("________________________after read counter start_________________________________________")
    println(Service.getUnreadChatsCount(4))

    println("________________________print unread chats_________________________________________")
    println(Service.getUnreadChatsCount(3))
    Service.getChats(3)
    Service.tagChatLikeARead(Pair(1, 3))
    Service.tagChatLikeARead(Pair(2, 3))
    Service.tagChatLikeARead(Pair(0, 3))
    println(Service.getUnreadChatsCount(3))
    Service.getChats(3).forEach { print("_${it.key}_") }
    println()

    println("________________________print one chat_________________________________________")
    val chat = Service.getMessageList(5,6)
    for (message in chat) {
        println("date - ${message.date}; sender - ${message.senderID}; recipient - ${message.receiverID}; id - ${message.id}")
    }
}




