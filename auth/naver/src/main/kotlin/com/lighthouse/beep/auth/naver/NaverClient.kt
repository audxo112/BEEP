package com.lighthouse.beep.auth.naver

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import com.lighthouse.beep.auth.model.OAuthTokenResult
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLoginState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NaverClient @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    init {
        NaverIdLoginSDK.initialize(
            context,
            BuildConfig.NAVER_LOGIN_CLIENT_ID,
            BuildConfig.NAVER_LOGIN_CLIENT_SECRET,
            context.resources.getString(R.string.app_name),
        )
    }

    fun requestAccessToken(launcher: ActivityResultLauncher<Intent>, block: (String) -> Unit) {
        if (NaverIdLoginSDK.getState() == NidOAuthLoginState.OK) {
            val token = NaverIdLoginSDK.getAccessToken()
            if (token != null) {
                block(token)
            } else {
                NaverIdLoginSDK.authenticate(context, launcher)
            }
        } else {
            NaverIdLoginSDK.authenticate(context, launcher)
        }
    }

    fun getAccessToken(result: ActivityResult): OAuthTokenResult {
        return when (result.resultCode) {
            Activity.RESULT_OK -> {
                val token = NaverIdLoginSDK.getAccessToken()
                if (token != null) {
                    OAuthTokenResult.Success(token)
                } else {
                    OAuthTokenResult.Failed(NullPointerException("Token is Null!"))
                }
            }

            Activity.RESULT_CANCELED -> {
                OAuthTokenResult.Canceled(
                    NaverIdLoginSDK.getLastErrorCode().code,
                    NaverIdLoginSDK.getLastErrorDescription() ?: "",
                )
            }

            else -> {
                OAuthTokenResult.Failed(IllegalStateException("네이버 로그인에 실패 했습니다."))
            }
        }
    }

    fun signOut() {
        NaverIdLoginSDK.logout()
    }
}
