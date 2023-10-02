package ru.netology.nework.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.dao.UserDao
import ru.netology.nework.entity.EventsEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity
import ru.netology.nework.entity.UserEntity

@Database(
    entities = [PostEntity::class, EventsEntity::class, UserEntity::class, PostRemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
}