package com.lighthouse.beep.ui.feature.login.page.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.lighthouse.auth.google.GoogleTokenResult
import com.lighthouse.auth.google.local.LocalGoogleClient
import com.lighthouse.beep.auth.kakao.KakaoTokenResult
import com.lighthouse.beep.auth.kakao.local.LocalKakaoClient
import com.lighthouse.beep.auth.naver.NaverTokenResult
import com.lighthouse.beep.auth.naver.local.LocalNaverClient
import com.lighthouse.beep.core.ui.compose.rememberLifecycleEvent
import com.lighthouse.beep.model.deviceconfig.AuthProvider
import com.lighthouse.beep.theme.BeepColor
import com.lighthouse.beep.theme.BeepShape
import com.lighthouse.beep.theme.BeepTextStyle
import com.lighthouse.beep.theme.BeepTheme
import com.lighthouse.beep.ui.designsystem.dotindicator.DotIndicator
import com.lighthouse.beep.ui.designsystem.dotindicator.DotShape
import com.lighthouse.beep.ui.designsystem.dotindicator.type.WormType
import com.lighthouse.beep.ui.feature.login.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
internal fun IntroScreen(
    onNavigatePermission: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val isLoading = viewModel.loadingState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loginEvent.collect {
            if (it.isSuccess) {
                onNavigatePermission()
            }
        }
    }

    Box {
        LoginContentScreen(
            list = viewModel.items,
            onLoginSuccess = { provider, accessToken ->
                viewModel.requestLogin(provider, accessToken)
            },
        )
        if (isLoading.value) {
            LoadingScreen()
        }
    }
}

@Composable
internal fun LoginContentScreen(
    list: List<LoginData> = listOf(),
    onLoginSuccess: (provider: AuthProvider, accessToken: String) -> Unit = { _, _ -> },
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        IntroPager(list = list)
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(id = R.string.login_method),
            style = BeepTextStyle.BodyMedium,
            color = BeepColor.Grey50,
        )
        Spacer(modifier = Modifier.size(12.dp))
        NaverLoginButton { result ->
            when (result) {
                is NaverTokenResult.Success -> {
                    onLoginSuccess(AuthProvider.NAVER, result.accessToken)
                }

                is NaverTokenResult.Failed -> {
                }

                is NaverTokenResult.Canceled -> {
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        KakaoLoginButton { result ->
            when (result) {
                is KakaoTokenResult.Success -> {
                    onLoginSuccess(AuthProvider.KAKAO, result.accessToken)
                }

                is KakaoTokenResult.Failed -> {
                }

                is KakaoTokenResult.Canceled -> {
                }
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        GoogleLoginButton { result ->
            when (result) {
                is GoogleTokenResult.Success -> {
                    onLoginSuccess(AuthProvider.GOOGLE, result.idToken)
                }

                is GoogleTokenResult.Failed -> {
                }

                is GoogleTokenResult.Canceled -> {
                }
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.login_description),
                style = BeepTextStyle.BodySmall,
                color = BeepColor.Grey70,
            )
            Spacer(modifier = Modifier.size(8.dp))
            GuestButton {
                onLoginSuccess(AuthProvider.GUEST, "")
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
internal fun LoadingScreen() {
    Dialog(
        onDismissRequest = {},
    ) {
        Surface(color = Color.Transparent) {
            CircularProgressIndicator(
                color = BeepColor.Pink,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun IntroPager(
    list: List<LoginData> = listOf(),
) {
    val lifecycleEvent = rememberLifecycleEvent()
    val pagerState = rememberPagerState()

    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
            var autoScrollJob: Job? = startAutoScroll(this, pagerState, list.size)
            pagerState.interactionSource.interactions.collect { interaction ->
                val interactive = when (interaction) {
                    is PressInteraction.Press -> true
                    is DragInteraction.Start -> true
                    else -> false
                }
                autoScrollJob = if (interactive) {
                    autoScrollJob?.cancel()
                    null
                } else {
                    startAutoScroll(this, pagerState, list.size)
                }
            }
        }
    }

    Surface {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                pageCount = list.size,
                state = pagerState,
            ) { index ->
                val item = list[index]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(id = item.titleRes),
                        style = BeepTextStyle.TitleLarge,
                        color = BeepColor.Grey30,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = stringResource(id = item.descriptionRes),
                        style = BeepTextStyle.TitleMedium,
                        color = BeepColor.Grey50,
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                    IntroImage(lottieRes = item.lottieRes)
                }
            }
            Spacer(modifier = Modifier.size(20.dp))
            DotIndicator(
                dotCount = list.size,
                pagerState = pagerState,
                dotType = WormType(
                    dotShape = DotShape(color = BeepColor.Grey95),
                    wormDotShape = DotShape(color = BeepColor.Pink50),
                ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
internal fun startAutoScroll(
    coroutineScope: CoroutineScope,
    pagerState: PagerState,
    pagerSize: Int,
): Job {
    return coroutineScope.launch {
        while (isActive) {
            delay(3000)
            val nextPage = (pagerState.currentPage + 1) % pagerSize
            pagerState.animateScrollToPage(nextPage)
        }
    }
}

@Composable
internal fun IntroImage(
    @RawRes lottieRes: Int,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(lottieRes),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        modifier = Modifier.size(150.dp),
        composition = composition,
        progress = { progress },
    )
}

@Composable
internal fun LoginButton(
    @StringRes textRes: Int,
    @ColorRes textColorRes: Int,
    @ColorRes backgroundColorRes: Int,
    @DrawableRes iconRes: Int,
    @ColorRes iconTintRes: Int? = null,
    @ColorRes iconBackgroundColorRes: Int = backgroundColorRes,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp),
        color = colorResource(id = backgroundColorRes),
        shape = BeepShape.ButtonShape,
    ) {
        Box(
            modifier = Modifier
                .clickable { onClick() },
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Spacer(modifier = Modifier.size(6.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(colorResource(id = iconBackgroundColorRes), CircleShape),
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(iconRes)
                            .build(),
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.Center),
                        colorFilter = iconTintRes?.let { ColorFilter.tint(colorResource(id = it)) },
                        contentDescription = null,
                    )
                }
            }
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = textRes),
                color = colorResource(id = textColorRes),
                style = BeepTextStyle.TitleSmall,
            )
        }
    }
}

@Composable
internal fun NaverLoginButton(
    onAccessTokenResult: (NaverTokenResult) -> Unit = {},
) {
    val naverClient = LocalNaverClient.current
    val requestNaverLogin =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val tokenResult = naverClient.getAccessToken(result)
            onAccessTokenResult(tokenResult)
        }

    LoginButton(
        textRes = R.string.naver_login,
        textColorRes = R.color.naver_label,
        backgroundColorRes = R.color.naver_container,
        iconRes = R.drawable.icon_naver,
        iconTintRes = R.color.naver_label,
        onClick = {
            naverClient.requestAccessToken(requestNaverLogin) { token ->
                onAccessTokenResult(NaverTokenResult.Success(token))
            }
        },
    )
}

@Composable
internal fun KakaoLoginButton(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onAccessTokenResult: (KakaoTokenResult) -> Unit = {},
) {
    val kakaoClient = LocalKakaoClient.current
    val context = LocalContext.current

    LoginButton(
        textRes = R.string.kakao_login,
        textColorRes = R.color.kakao_label,
        backgroundColorRes = R.color.kakao_container,
        iconRes = R.drawable.icon_kakao,
        onClick = {
            coroutineScope.launch {
                val tokenResult = kakaoClient.getAccessToken(context)
                onAccessTokenResult(tokenResult)
            }
        },
    )
}

@Composable
internal fun GoogleLoginButton(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    onAccessTokenResult: (GoogleTokenResult) -> Unit = {},
) {
    val googleClient = LocalGoogleClient.current
    val requestGoogleLogin =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            coroutineScope.launch {
                val tokenResult = googleClient.getAccessToken(result)
                onAccessTokenResult(tokenResult)
            }
        }

    LoginButton(
        textRes = R.string.google_login,
        textColorRes = R.color.google_label,
        backgroundColorRes = R.color.google_container,
        iconRes = R.drawable.icon_google,
        iconBackgroundColorRes = R.color.google_symbol_background,
        onClick = {
            val intent = googleClient.signInIntent
            requestGoogleLogin.launch(intent)
        },
    )
}

@Composable
internal fun GuestButton(
    onClick: () -> Unit = {},
) {
    Surface(
        shape = RoundedCornerShape(5.dp),
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(4.dp),
        ) {
            Text(
                text = stringResource(id = R.string.guest_login),
                style = BeepTextStyle.BodySmall,
                color = BeepColor.Grey30,
            )
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.icon_right)
                    .build(),
                colorFilter = ColorFilter.tint(BeepColor.Grey70),
                modifier = Modifier.size(16.dp),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
internal fun PreviewGuestButton() {
    BeepTheme {
        IntroScreen()
    }
}
