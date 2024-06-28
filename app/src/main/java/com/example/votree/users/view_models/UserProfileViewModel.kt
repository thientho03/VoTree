package com.example.votree.users.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.votree.users.models.User
import com.example.votree.users.repositories.UserRepository

class UserProfileViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadUserProfile(userId: String) {
        _isLoading.value = true
        userRepository.getUserWithCallback(userId) { user ->
            _user.value = user
            _isLoading.value = false
        }
    }
}