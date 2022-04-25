import Database.storage

object Service {

    fun createNewChat(senderId: Int, receiverID: Int) {
        if (senderId != receiverID) storage[Pair(senderId, receiverID)] = mutableListOf()
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
        return if (storage[pair]!!.last().text == text && storage[pair]!!.last().attachment == attach) 0 else 1
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
        //storage[Pair(message.senderID, message.receiverID)]!!.removeIf{ it.id == message.id }
        //можно заменить весь метод одной строкой 68, но тогда я буду удалять напрямую из мапы, а вы говорили, что так
        //лучше не делать, не смогу выполнить проверки, которые делаю ниже, 3 раза буду создавать пару ключей для мапы,
        //поэтому не уверен, что тут нужно применять пайплайн

        val pairId = Pair(message.senderID, message.receiverID)
        val tempList: MutableList<PrivateMessage>? = storage[pairId]
        val tempMessage = storage[pairId]!!.find { it.id == message.id }
        tempList!!.removeIf { it.id == message.id }
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
        return storage
            .asSequence() //здесь он больше навредит, кмк, в мапе по ключу незатратно искать, но раз по заданию нужно...
            .filter { it.key.second == recipientId }
            .count { entry -> entry.value.any { !it.isRead } }
    }
    //Получить информацию о количестве непрочитанных чатов
    //это количество чатов, в каждом из которых есть хотя бы одно непрочитанное сообщение.

    fun getChats(recipientId: Int): Map<Pair<Int, Int>, List<PrivateMessage>> {

        return if (storage.filter { it.key.second == recipientId }.isNotEmpty()) {
            storage
                .filter { it.key.second == recipientId }
                .filter { entry -> entry.value.any { !it.isRead } }
        } else {
            throw MessageServiceException("нет сообщений")
        }
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