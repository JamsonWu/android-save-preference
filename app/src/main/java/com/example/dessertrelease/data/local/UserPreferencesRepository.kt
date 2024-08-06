package com.example.dessertrelease.data.local

import android.content.ContentValues.TAG
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
){
    // 通过 dataStore 读取 preference 值的方法
    // 从dataStore中读取值要注意读取不到值的场景
    // 如果要读取不同的状态值这种读取方法还是比较麻烦的，可以考虑封装一个函数
    // 注意 dataStore.data是个流，Flow
    // 仓库返回的就是流数据
    val isLinearLayout: Flow<Boolean> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                // 如果读取不到值，则赋空Preferences
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            // preferences根据KEY读取值
            preferences[IS_LINEAR_LAYOUT] ?: true
        }
    // 内联对象
    private companion object {
        // int,long,double,float,string,stringSet +PreferencesKey 只能存简单数据类型值
        // 注意这里只是读取一个KEY
        val IS_LINEAR_LAYOUT = booleanPreferencesKey("is_linear_layout")
    }

    // 仓库这里也要声明为异步方法
    suspend fun saveLayoutPreference(isLinearLayout: Boolean) {
        // edit 是异步方法
        dataStore.edit { preferences ->
            // 根据KEY更新值
            preferences[IS_LINEAR_LAYOUT] = isLinearLayout
        }
    }
}