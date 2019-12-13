package com.medanis.myphoneloginapp

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var phoneNumberTxt: EditText? = null
    private var phoneNumber: String? = null
    private var progressDialog: ProgressDialog? = null
    var ccp: CountryCodePicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()

        val auth = FirebaseAuth.getInstance()
        auth.setLanguageCode("fr")

        verification_btn.setOnClickListener {
            phoneNumberTxt = findViewById(R.id.msisdn)
            ccp = findViewById(R.id.ccp)
            val countryCode = ccp?.selectedCountryCodeWithPlus
            phoneNumber = phoneNumberTxt?.text.toString()
            phoneNumber = countryCode + phoneNumber
            Log.d("User Phone Number", phoneNumber)
            initView()
            if (phoneNumber != null && !phoneNumber!!.isEmpty()) {
                startPhoneNumberVerification(phoneNumber!!,mCallbacks)
                showProgressDialog(this, "Sending a verification code", false)
            } else {
                showToast("Please enter a valid number to continue!")
            }
        }
    }

    private fun initView() {
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                if (progressDialog != null) {
                    dismissProgressDialog(progressDialog)
                }
                notifyUserAndRetry("Your Phone Number Verification is failed. Retry again!")
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(
                    "onVerificationCompleted",
                    "onVerificationCompleted:$credential"
                )
                if (progressDialog != null) {
                    dismissProgressDialog(progressDialog)
                }
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w("onVerificationFailed", "onVerificationFailed", e)
                if (progressDialog != null) {
                    dismissProgressDialog(progressDialog)
                }
                if (e is FirebaseAuthInvalidCredentialsException) { // Invalid request
// [START_EXCLUDE]
//                    msisdn.error = "Invalid phone number."
                    Log.e("Exception:", "Invalid phone number.$e")
                    // [END_EXCLUDE]
                }
                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException$e")
                } else if (e is FirebaseTooManyRequestsException) {
                    Log.e("Exception:", "FirebaseTooManyRequestsException$e")
                }
                notifyUserAndRetry("Your Phone Number Verification is failed. Retry again!")
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) { //for low level version which doesn't do aoto verififcation save the verification code and the token
                Log.d("onCodeSent", "onCodeSent:$verificationId")
                Log.i("Verification code:", verificationId)
            }
        }
    }

    private fun showLoginActivity() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }

    private fun showMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun startPhoneNumberVerification(phoneNumber: String, mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this,  // Activity (for callback binding)
            mCallbacks
        )
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    val user = task.result!!.user
                    Log.d("Sign in with phone auth", "Success $user")
                    showMainActivity()
                } else {
                    notifyUserAndRetry("Your Phone Number Verification is failed.Retry again!")
                }
            }
    }

    /**
     * Method to show progress dialog
     *
     * @param mActivity
     * @param message
     * @param isCancelable
     * @return dialog
     */
    fun showProgressDialog(
        mActivity: Context?,
        message: String?,
        isCancelable: Boolean
    ): ProgressDialog {
        progressDialog = ProgressDialog(mActivity)
        progressDialog!!.show()
        progressDialog!!.setCancelable(isCancelable)
        progressDialog!!.setCanceledOnTouchOutside(false)
        progressDialog!!.setMessage(message)
        return progressDialog as ProgressDialog
    }

    /**
     * Method to dismiss progress dialog
     *
     * @param progressDialog
     */
    fun dismissProgressDialog(progressDialog: ProgressDialog?) {
        if (progressDialog != null && progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }

    fun notifyUserAndRetry(message: String?) {
        val alertDialogBuilder =
            AlertDialog.Builder(this)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton(
            "Ok"
        ) { arg0, arg1 -> showLoginActivity() }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> showLoginActivity() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}