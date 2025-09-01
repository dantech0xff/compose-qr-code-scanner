package com.creative.qrcodescanner.usecase.setting

import android.content.Context
import com.creative.qrcodescanner.BuildConfig
import com.creative.qrcodescanner.R
import com.creative.qrcodescanner.data.entity.UserSettingData
import com.creative.qrcodescanner.repo.user.UserDataRepo
import com.creative.qrcodescanner.ui.setting.ListSettingUIState
import com.creative.qrcodescanner.ui.setting.SettingId
import com.creative.qrcodescanner.ui.setting.SettingItemUIState
import com.creative.qrcodescanner.usecase.base.BaseFlowUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

/**
 * Created by dan on 20/01/2024
 *
 * Copyright © 2024 1010 Creative. All rights reserved.
 */
 
@ViewModelScoped
class GetAppSettingFlowUseCase @Inject constructor(
    private val userDataRepo: UserDataRepo,
    @ApplicationContext private val context: Context
) : BaseFlowUseCase<Unit, ListSettingUIState>() {
    override fun execute(input: Unit): Flow<ListSettingUIState> {
        return userDataRepo.userSettingData.catch {
            emit(UserSettingData(isEnableVibrate = false, isEnableSound = false))
        }.map {
            ListSettingUIState(mutableListOf<SettingItemUIState>().apply {
                add(SettingItemUIState.SettingHeaderUIState(context.getString(R.string.main_setting).hashCode(), title = context.getString(R.string.main_setting)))
                add(SettingItemUIState.SwitchUIState(SettingId.SOUND.value, title = context.getString(R.string.sound), isEnable = it.isEnableSound))
                add(SettingItemUIState.SwitchUIState(SettingId.VIBRATE.value, title = context.getString(R.string.vibrate), isEnable = it.isEnableVibrate))
                add(SettingItemUIState.SwitchUIState(SettingId.KEEP_SCANNING.value, title = context.getString(R.string.keep_scanning), isEnable = it.isKeepScanning))

                    add(SettingItemUIState.SwitchUIState(SettingId.PREMIUM.value, title = context.getString(R.string.force_premium), isEnable = it.isPremium))

                add(SettingItemUIState.DividerUIState(UUID.randomUUID().hashCode()))

                add(
                    SettingItemUIState.SettingHeaderUIState(
                        context.getString(R.string.panda_scanner_setting).hashCode(),
                        title = context.getString(R.string.panda_scanner_setting)
                    )
                )
                add(SettingItemUIState.TextUIState(SettingId.ABOUT_US.value, title = context.getString(R.string.about_us), iconRes = R.drawable.ic_information_circle))
                add(SettingItemUIState.TextUIState(SettingId.RATE_US.value, title = context.getString(R.string.rate_us), iconRes = R.drawable.ic_heart))
                add(
                    SettingItemUIState.TextUIState(
                        SettingId.MANAGE_SUBSCRIPTION.value,
                        title = context.getString(R.string.manage_subscription),
                        iconRes = R.drawable.ic_gift
                    )
                )
            })
        }
    }
}