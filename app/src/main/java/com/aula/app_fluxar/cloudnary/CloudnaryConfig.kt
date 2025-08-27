package com.aula.app_fluxar.cloudnary

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudnaryConfig {
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            val config: HashMap<String, String> = HashMap()
            config["cloud_name"] = "dbvus0e8r"
            config["api_key"] = "211167251358872"
            config["api_secret"] = "tmSjyRPWPCk2JR2a7bEYLnSU5B4"

            MediaManager.init(context, config)
            initialized = true
        }

    }
}