package com.example.reminderapp

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.reminderapp.databinding.FragmentLoginBinding
import com.example.reminderapp.databinding.FragmentRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Firebase.auth.signOut()

        binding.registerLink.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.action_loginFragment_to_registerFragment))

        val _email = view.findViewById<EditText>(R.id.editTextEmail)
        val _password = view.findViewById<EditText>(R.id.editTextPassword)

        val _btnLogin = view.findViewById<Button>(R.id.btnSignIn)

        _btnLogin.setOnClickListener {
            performLogin(_email.text.toString(), _password.text.toString())
        }

    }

    private fun performLogin(email: String, password: String) {

        val pDialog = SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#A5DC86")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()

        if (email != "" && password != "") {
            auth = Firebase.auth
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { tokenTask ->
                            if (!tokenTask.isSuccessful) {
                                return@OnCompleteListener
                            }

                            // Get new FCM registration token
                            val token = tokenTask.result

                            // Log and toast
                            db.collection("users").document(Functions.loggedInUser!!.uid).update("token", token.toString())
                            pDialog.hide()
                            requireView().findNavController()!!.navigate(R.id.action_loginFragment_to_homeActivity)
                        })
                    } else {
                        SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Cannot Login!")
                            .setContentText("Error: ${task.exception}")
                            .setConfirmText("OK")
                            .show()
                    }
                }
        } else {
            pDialog.hide()
            Toast.makeText(context, "Please enter text in email and password field", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}