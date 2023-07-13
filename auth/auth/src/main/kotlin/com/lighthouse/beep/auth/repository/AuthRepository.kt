package com.lighthouse.beep.auth.repository

import com.lighthouse.beep.auth.model.OAuthRequest
import com.lighthouse.beep.model.deviceconfig.AuthInfo
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val authInfo: Flow<AuthInfo>

    suspend fun signIn(request: OAuthRequest)

    suspend fun signOut()

    suspend fun withdrawal()
}
