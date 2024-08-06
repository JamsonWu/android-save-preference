/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.dessertrelease.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.dessertrelease.DessertReleaseApplication
import com.example.dessertrelease.R
import com.example.dessertrelease.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * View model of Dessert Release components
 */
class DessertReleaseViewModel(private val userPreferencesRepository: UserPreferencesRepository) : ViewModel() {

    // 使用MutableStateFlow维护状态
    private val _uiState = MutableStateFlow(DessertReleaseUiState())

    // UI states access for various [DessertReleaseUiState]
    // UI可以访问的状态
    val uiState: StateFlow<DessertReleaseUiState> =
        // 将仓库返回的Flow数据转为StateFlow
        // Flow<Boolean>读取需要用到 map 映射，返回一个新Flow
        userPreferencesRepository.isLinearLayout.map { isLinearLayout ->
            DessertReleaseUiState(isLinearLayout)
        }
            // 在协程上下文将Cold Flow转为Hot StateFlow，让UI可观察状态变化
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DessertReleaseUiState()
            )

    /*
     * [selectLayout] change the layout and icons accordingly and
     * save the selection in DataStore through [userPreferencesRepository]
     * 通过仓库更新状态值
     */
    fun selectLayout(isLinearLayout: Boolean) {
        viewModelScope.launch {
            // 值更新交给仓库来处理，由于保存是异步方法，所以要通过协程来调用
            userPreferencesRepository.saveLayoutPreference(isLinearLayout)
        }
    }

    // 通过内联单例对象提供viewModelFactory
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // 通过 factory给ViewModel传递参数
                // this是ViewModel上下文，APPLICATION_KEY是上下文的一个map字段
                val application = (this[APPLICATION_KEY] as DessertReleaseApplication)
                // 将操作dataStore的仓库传给ViewModel
                DessertReleaseViewModel(application.userPreferencesRepository)
            }
        }
    }
}

/*
 * Data class containing various UI States for Dessert Release screens
 */
data class DessertReleaseUiState(
    // 是否线性布局
    val isLinearLayout: Boolean = true,
    // 切换内容描述
    val toggleContentDescription: Int =
        if (isLinearLayout) R.string.grid_layout_toggle else R.string.linear_layout_toggle,
    // 切换图标
    val toggleIcon: Int =
        if (isLinearLayout) R.drawable.ic_grid_layout else R.drawable.ic_linear_layout
)
