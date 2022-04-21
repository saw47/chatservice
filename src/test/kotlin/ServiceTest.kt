package ru.netology

import Database
import Service
import org.junit.Test
import org.junit.Assert.*

internal class ServiceTest {

    @Test
    fun createNewChat() {
        clear()
        Service.createNewChat(1, 2)
        assertEquals(Database.storage.containsKey(Pair(1, 2)), true)
    }

    @Test
    fun createMessage() {
        clear()
        createStorage()

        var counter = 0
        Database.storage.forEach { counter += it.value.size }
        assertEquals(counter, 100)
    }

    @Test
    fun editMessage() {
        clear()
        createStorage()

        Service.editMessage(Database.storage[Pair(1, 2)]!![3], "new text TEST")

        assertEquals(Database.storage[Pair(1, 2)]!![3].text, "new text TEST")
        assertEquals(Database.storage[Pair(3, 2)]!![1].text, "origin message text")
    }

    @Test
    fun readMessage() {
        clear()
        createStorage()

        assertEquals(Database.storage[Pair(0, 1)]!![2].isRead, false)
        Service.readMessage(Database.storage[Pair(0, 1)]!![2])
        assertEquals(Database.storage[Pair(0, 1)]!![2].isRead, true)
    }

    @Test
    fun tagChatLikeARead() {
        clear()
        createStorage()


        Service.tagChatLikeARead(Pair(0, 1))
        assertEquals(Database.storage[Pair(0, 1)]!!.filter { it.isRead }.size, Database.storage[Pair(0, 1)]!!.size)
    }

    @Test
    fun deleteMessage() {
        clear()
        createStorage()

        val checkedId = Database.storage[Pair(0, 1)]!![2].id
        val checkedIdBeforeDelete = Database.storage[Pair(0, 1)]!![3].id

        assertEquals(Database.storage[Pair(0, 1)]?.get(2)?.id, checkedId)
        Service.deleteMessage(Database.storage[Pair(0, 1)]!![2])
        assertEquals(Database.storage[Pair(0, 1)]?.get(2)?.id, checkedIdBeforeDelete)


    }

    @Test
    fun deleteChat() {
        clear()
        createStorage()

        assertEquals(Database.storage[Pair(0, 1)]?.isEmpty(), false)
        Service.deleteChat(0, 1)
        assertEquals(Database.storage[Pair(0, 1)]?.isEmpty(), null)

    }

    @Test
    fun getUnreadChatsCount() {
        clear()
        createStorage()

        Service.getUnreadChatsCount(0)
        assertEquals(Service.getUnreadChatsCount(0), 4)
        Service.tagChatLikeARead(Pair(1, 0))
        assertEquals(Service.getUnreadChatsCount(0), 3)
    }

    @Test
    fun getChats() {
        clear()
        createStorage()

        var counter = 0
        var unreadStorage = Service.getChats(0)
        unreadStorage.forEach { counter += it.value.filter { !it.isRead }.size }
        println(counter)
        assertEquals(counter, 20)

        counter = 0
        Service.tagChatLikeARead(Pair(1, 0))
        unreadStorage = Service.getChats(0)
        unreadStorage.forEach { counter += it.value.filter { !it.isRead }.size }
        assertEquals(counter, 15)

    }

    @Test
    fun getMessageList() {
        clear()
        createStorage()

        val list = Service.getMessageList(0, 1)
        assertEquals(list.filter { it.senderID == 0 }.size == list.filter { it.senderID == 1 }.size, true)
        assertEquals(list.filter { it.senderID == 0 }.size + list.filter { it.senderID == 1 }.size
                == list.size, true
        )
    }

    companion object {
        fun createStorage() {
            for (i in 0..4) {
                for (j in 0..4) {
                    Service.createNewChat(j, i)
                }
            }

            for (i in 0..4) {
                for (j in 0..4) {
                    if (i != j) {
                        val key = Pair(j, i)
                        while (Database.storage[key]!!.size < 5) {
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
        }

        fun clear() {
            Database.storage.clear()
        }
    }
}