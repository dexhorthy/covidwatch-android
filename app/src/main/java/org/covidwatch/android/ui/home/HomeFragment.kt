package org.covidwatch.android

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import org.covidwatch.android.data.IsCurrentSickSharedPreferenceLiveData
import org.covidwatch.android.databinding.FragmentHomeBinding
import org.covidwatch.android.ui.shared.SharedViewModel
import org.covidwatch.android.ui.home.HomeViewModel
import org.covidwatch.android.ui.shared.SharedViewModelFactory

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private lateinit var preferences : SharedPreferences

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,container,
            false
        )
        binding.shareTheAppButton.setOnClickListener {shareApp()}
        binding.selfReportButton.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_mainFragment_to_selfReportFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeViewModel: HomeViewModel by viewModels()
        val safeContext = context ?: return
        val sharedViewModel: SharedViewModel by viewModels(factoryProducer = {
            SharedViewModelFactory(
                IsCurrentSickSharedPreferenceLiveData(
                    safeContext.getSharedPreferences(
                        safeContext.getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE
                    )
                ),
                safeContext.applicationContext as Application)
        })

        homeViewModel.initialVisit.observe(viewLifecycleOwner, Observer {
            binding.mainTitle.text = if(it) getString(R.string.welcome_back_title) else getString(R.string.you_re_all_set_title)
        })

        sharedViewModel.isCurrentUserSick.observe(viewLifecycleOwner, Observer {
            Log.i("is curr sick in shared", it.toString())
            binding.contactAlertText.text = if(it) getString(R.string.you_reported_alert_text) else getString(R.string.contact_alert_text)
        })

        sharedViewModel.hasPossiblyInteractedWithInfected.observe(viewLifecycleOwner, Observer {
            binding.contactAlertText.visibility = if(it) View.VISIBLE else View.GONE
        })
    }

    override fun onResume() {
        super.onResume()

        val application = requireContext()
        preferences = application.getSharedPreferences(
            application.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        with (preferences.edit()) {
            putBoolean(application.getString(R.string.preference_initial_visit), false);
            apply()
        }
    }

    private fun shareApp() {
        val shareText = getString(R.string.share_intent_text)
        val sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            "$shareText https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        )
        sendIntent.type = "text/plain"
        startActivity(sendIntent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
