package com.lighthouse.beep.domain.usecase.user

import com.lighthouse.beep.auth.repository.AuthRepository
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {

    operator fun invoke(): String {
        return authRepository.getCurrentUserId()
    }
}