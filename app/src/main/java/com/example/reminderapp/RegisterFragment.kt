package com.example.reminderapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.findNavController
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.reminderapp.databinding.FragmentLandingBinding
import com.example.reminderapp.databinding.FragmentRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val db = Firebase.firestore

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
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val _btnSignUp = view.findViewById<Button>(R.id.btnSignUp)

        val _userFullName = view.findViewById<EditText>(R.id.editTextFullName)
        val _username = view.findViewById<EditText>(R.id.editTextUsername)
        val _userEmail = view.findViewById<EditText>(R.id.editTextEmail)
        val _userPassword = view.findViewById<EditText>(R.id.editTextPassword)



        _btnSignUp.setOnClickListener {
            val full_name = _userFullName.text.toString()
            val username = _username.text.toString().lowercase()
            val email = _userEmail.text.toString()
            val password = _userPassword.text.toString()
            performRegister(full_name, username, email, password)
        }

        binding.loginLink.setOnClickListener {
            view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }


    private fun performRegister(fullName: String, username: String, email: String, password: String) {

        if (fullName != "" && username != "" && email != "" && password != "") {

            GlobalScope.launch(Dispatchers.IO) {
                val documents = db.collection("users").whereEqualTo("username", username).get().await()

                withContext(Dispatchers.Main) {
                    if (documents.isEmpty) {
                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener {
                                if (!it.isSuccessful) {
                                    SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Cannot Register!")
                                        .setContentText("Error")
                                        .setConfirmText("OK")
                                        .show()
                                } else {
                                    val user = hashMapOf(
                                        "profile_picture" to "",
                                        "email" to email,
                                        "full_name" to fullName,
                                        "username" to username,
                                    )

                                    db.collection("users")
                                        .document(it.result?.user?.uid.toString())
                                        .set(user)
                                        .addOnSuccessListener {

                                            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { tokenTask ->
                                                if (!tokenTask.isSuccessful) {
                                                    return@OnCompleteListener
                                                }
                                                val token = tokenTask.result

                                                db.collection("users").document(Functions.loggedInUser!!.uid).update("token", token.toString())
                                                Toast.makeText(activity, "Account creation success", Toast.LENGTH_SHORT).show()
                                                val intent = Intent(activity, HomeActivity::class.java)
                                                startActivity(intent)
                                            })

                                        }
                                        .addOnFailureListener { e->
                                            SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                                                .setTitleText("Something went wrong")
                                                .setContentText("Error $e")
                                                .setConfirmText("OK")
                                                .show()
                                        }
                                }
                            }

                    } else {
                        SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Oops!")
                            .setContentText("Username already taken. Choose another username")
                            .setConfirmText("OK")
                            .show()
                    }

                }

            }
        } else {
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
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}