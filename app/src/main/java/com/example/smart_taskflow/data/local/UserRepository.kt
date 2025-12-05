package com.example.smart_taskflow.data.local

import com.example.smart_taskflow.data.model.User

class UserRepository(private val userDao: UserDao) {

    suspend fun registerUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun login(email: String, password: String): User? {
        return userDao.login(email, password)
    }

    suspend fun getUser(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}
