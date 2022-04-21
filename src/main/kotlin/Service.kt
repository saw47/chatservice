import Database.storage

object Service {

    fun createNewChat(senderId: Int, receiverID: Int) {
        if (senderId != receiverID) {
            storage[Pair(senderId, receiverID)] = mutableListOf<PrivateMessage>()
        }
    }

    fun createMessage(senderId: Int, receiverID: Int, text: String, attach: Attachment? = null): Int {

        val message = PrivateMessage(
            receiverID = receiverID,
            senderID = senderId,
            text = text,
            attachment = attach
        )

        val pair = Pair(senderId, receiverID)
        if (storage.keys.contains(pair)) {
            storage[pair]!!.add(message)
        } else {
            createNewChat(senderId, receiverID)
            storage[pair]!!.add(message)
        }
        return if (storage[pair]!!.last().text == text) 0 else 1
    }

    fun editMessage(message: PrivateMessage, newText: String? = null, addAttach: Attachment? = null): Int {

        val pairId = Pair(message.senderID, message.receiverID)
        if (storage[pairId]?.find { it.id == message.id } == null) throw MessageServiceException("Message not found")
        val modifiedMessageList = storage[pairId]

        when {
            newText == null && addAttach == null -> return 1
            newText != null -> modifiedMessageList?.find { it.id == message.id }?.text = newText
            addAttach != null -> modifiedMessageList?.find { it.id == message.id }?.attachment = addAttach
        }

        return if (modifiedMessageList != null) {
            storage[pairId]?.addAll(modifiedMessageList)
            0
        } else 1
    }

    fun readMessage(message: PrivateMessage): Int {
        return if (!message.isRead) {
            message.isRead = true
            0
        } else {
            1
        }
    }

    fun tagChatLikeARead(treadPair: Pair<Int, Int>): Int {

        return if (storage[treadPair]?.isNotEmpty() == true) {
            val tempStorage = storage[treadPair]
            tempStorage?.forEach { it.isRead = true }
            storage[treadPair] = tempStorage!!
            0
        } else {
            1
        }
    }

    fun deleteMessage(message: PrivateMessage): Int {

        val pairId = Pair(message.senderID, message.receiverID)
        val tempList: MutableList<PrivateMessage>? = storage[pairId]

        val tempMessage = tempList!!.find { it.id == message.id }
        tempList.removeIf { it.id == message.id }
        storage[pairId] = tempList
        if (storage[pairId]!!.isEmpty()) deleteChat(pairId.first, pairId.second)
        return if (!storage[pairId]!!.contains(tempMessage)) 0 else 1
    }
    //валидация удаляющего сообщения (отправитель == удаляющий) пусть реализуется на стороне клиента)

    fun deleteChat(senderId: Int, receiverID: Int): Int {
        storage.remove(Pair(senderId, receiverID))
        return if (storage[Pair(senderId, receiverID)].isNullOrEmpty()) 0 else 1
    }
    //логика - удаляет исходящие сообщения этого пользователя другому определенному пользователю. Доругой пользователь
    //перестает видеть в этом чате входящие от того, кто их удалил, но видит свои исходящие сообщения.
    //валидация удаляющего сообщения (отправитель == удаляющий) пусть реализуется на стороне клиента


    fun getUnreadChatsCount(recipientId: Int): Int {
        var counter: Int = 0
        val recipientChats = storage.filter { it.key.second == recipientId }
        for (list in recipientChats) {
            if(list.value.any { !it.isRead }) {
                counter++
            }
        }
        return counter
    }
    //Получить информацию о количестве непрочитанных чатов
    //это количество чатов, в каждом из которых есть хотя бы одно непрочитанное сообщение.

    fun getChats(recipientId: Int): Map<Pair<Int, Int>, List<PrivateMessage>> {
        val recipientChats = storage.filter { it.key.second == recipientId }
        val recipientUnreadChats: MutableMap<Pair<Int, Int>, List<PrivateMessage>> = mutableMapOf()
        for (chat in recipientChats) {
            val chatIterator = chat.value.iterator()
            while (chatIterator.hasNext()) {
                if (!chatIterator.next().isRead) {
                    recipientUnreadChats[chat.key] = chat.value
                    break
                }
            }
        }
        if (recipientChats.isNotEmpty()) return recipientUnreadChats else throw MessageServiceException("нет сообщений")
    }
    //Получить список чатов-
    // где в каждом чате есть последнее (может непрочитанное?) сообщение (если нет, то пишется "нет сообщений").

    fun getMessageList(senderID: Int, receiverID: Int): List<PrivateMessage> {
        val senderAsSender = storage[Pair(senderID, receiverID)]
        val senderAsRecipient = storage[Pair(receiverID, senderID)]
        val merge = mutableListOf<PrivateMessage>()
        if (senderAsSender != null && senderAsRecipient != null) {
            merge.addAll(senderAsSender)
            merge.addAll(senderAsRecipient)
            merge.sortBy { it.date }
        }
        return merge
    }
    //возвращает чат в привычном виде -- последовательный по дате список сообщений между 2-мя пользователями
}