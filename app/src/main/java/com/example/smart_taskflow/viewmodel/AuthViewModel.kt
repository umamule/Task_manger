package com.example.smart_taskflow.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_taskflow.data.local.AppDatabase
import com.example.smart_taskflow.data.local.UserRepository
import com.example.smart_taskflow.data.model.User
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = AppDatabase.getDB(application).userDao()
    private val repo = UserRepository(userDao)

    var loggedInUserId: Int? = null

    fun register(fullName: String, email: String, password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (repo.getUser(email) != null) {
                callback(false)
                return@launch
            }

            repo.registerUser(User(fullName = fullName, email = email, password = password))
            callback(true)
        }
    }

    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val user = repo.login(email, password)
            if (user != null) {
                loggedInUserId = user.id
                callback(true)
            } else {
                callback(false)
            }
        }
    }
}
