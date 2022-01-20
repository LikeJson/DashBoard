package com.dashboard.kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.dashboard.kotlin.clashhelper.clashConfig
import kotlinx.android.synthetic.main.fragment_setting_page.*
import kotlinx.android.synthetic.main.toolbar.*

class SettingPage : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        toolbar.navigationIcon = ResourcesCompat.getDrawable(
            resources,
            R.drawable.ic_back,
            context?.theme
        )
        toolbar.setNavigationOnClickListener {
            val controller = it.findNavController()
            controller.popBackStack()
        }

        core_path.setText(clashConfig.corePath)
        scripts_path.setText(clashConfig.scriptsPath)

        core_path.setOnFocusChangeListener { _, b ->
            if (!b){
                clashConfig.corePath = core_path.text.toString()
            }
        }
        scripts_path.setOnFocusChangeListener { _, b ->
            if (!b){
                clashConfig.scriptsPath = scripts_path.text.toString()
            }
        }
    }
}