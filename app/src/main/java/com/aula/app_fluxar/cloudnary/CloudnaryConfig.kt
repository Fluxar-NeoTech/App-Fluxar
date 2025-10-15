package com.aula.app_fluxar.cloudnary

import android.content.Context
import com.aula.app_fluxar.BuildConfig
import com.cloudinary.android.MediaManager

object CloudnaryConfig {
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val config: HashMap<String, String> = HashMap()
            config["cloud_name"] = BuildConfig.CLOUD_NAME
            config["api_key"] = BuildConfig.API_KEY
            config["api_secret"] = BuildConfig.API_SECRET

            MediaManager.init(context, config)
            initialized = true
        }

    }
}