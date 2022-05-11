package com.straiberry.android.checkup.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.straiberry.android.checkup.common.helper.StraiberryCheckupSdkInfo

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StraiberryCheckupSdkInfo.setTokenInfo(
            getString(R.string.checkup_sdk_key),
            "com.straiberry.android.checkup.sample"
        )

        StraiberryCheckupSdkInfo.setUniqueId(getString(R.string.unique_id))

        StraiberryCheckupSdkInfo.setDisplayName(getString(R.string.display_name))

    }
}