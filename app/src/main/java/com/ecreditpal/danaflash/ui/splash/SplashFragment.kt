package com.ecreditpal.danaflash.ui.splash

import DataStoreKeys
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Space
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.createDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ecreditpal.danaflash.R
import com.ecreditpal.danaflash.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SplashFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return Space(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            val dataStore: DataStore<Preferences> = view.context.createDataStore(
                name = "settings"
            )

            dataStore.data
                .catch {
                    emit(emptyPreferences())
                }
                .map { preferences ->
                    preferences[DataStoreKeys.IS_ACCEPT_PRIVACY] ?: false
                }
                .onEach { delay(1000) }
                .collect { accept ->
                    if (accept) {
                        findNavController().navigate(R.id.action_splashFragment_to_mainActivity)
                        activity?.finish()
                    } else {
                        findNavController().navigate(R.id.action_splashFragment_to_privacyPoliciesFragment)
                    }
                }
        }
    }
}