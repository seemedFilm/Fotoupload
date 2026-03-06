package com.patrickl.fotoupload_android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.patrickl.fotoupload_android.data.EnrollmentRepository

class EnrollmentViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = EnrollmentRepository(context)
        @Suppress("UNCHECKED_CAST")
        return EnrollmentViewModel(repository) as T
    }
}