package com.lyricsnigeria.lyricsnigeria

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import es.dmoral.toasty.Toasty
import java.util.*

class SignIn : AppCompatActivity() {

    private var mGoogleButton: SignInButton? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var progressDialog: ProgressDialog? = null

    private var mCallbackManager: CallbackManager? = null
    private val TAG = "FACELOG"
    private var mFacebookBtn: Button? = null
    private var mTwitterBtn: Button? = null

    private var DAY_THEME = 1
    private var NIGHT_THEME = 2
    private var THEME: Int = 0

    private var textView: TextView? = null

    public override fun onStart() {
        super.onStart()

        mAuth!!.addAuthStateListener(mAuthListener!!)

        val currentUser = mAuth!!.currentUser
        if (currentUser != null) {
            updateUI()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            setTheme(R.style.DayTheme)
        } else {
            if (BottomNavActivity.SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME) {
                setTheme(R.style.NightTheme)
            } else {
                setTheme(R.style.AppTheme)
            }
        }
        setContentView(R.layout.activity_sign_in)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Welcome"

        mGoogleButton = findViewById<View>(R.id.google_button) as SignInButton
        mAuth = FirebaseAuth.getInstance()
        mFacebookBtn = findViewById<View>(R.id.facebook_button) as Button
        mTwitterBtn = findViewById<View>(R.id.twitter_button) as Button


        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                startActivity(Intent(this@SignIn, BottomNavActivity::class.java))
                finish()
            }
        }

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .enableAutoManage(this) { Toasty.normal(this@SignIn, "Error").show() }
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        mGoogleButton!!.setOnClickListener { signIn() }

        progressDialog = ProgressDialog(this@SignIn)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.setMessage("Signing In")
        progressDialog!!.setCancelable(false)


        mCallbackManager = CallbackManager.Factory.create()
        mFacebookBtn!!.setOnClickListener { facebookSignIn() }
        mTwitterBtn!!.setOnClickListener { signInTwitter() }


        textView = findViewById(R.id.sign_in_note)
        val text = "By Signing In, you accept that you have read and agree to Lyrics Nigeria's Terms & Conditions"
        val ss = SpannableString(text)

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openDialogue()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = Color.BLUE
                ds.isUnderlineText = true
            }
        }
        ss.setSpan(clickableSpan, 75, 93, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

        textView!!.text = ss
        textView!!.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun facebookSignIn() {
        progressDialog!!.show()

        LoginManager.getInstance().logInWithReadPermissions(this@SignIn, Arrays.asList("email", "public_profile"))
        LoginManager.getInstance().registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                progressDialog!!.hide()
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                progressDialog!!.hide()
                // ...
            }
        })
    }

    private fun signIn() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
        progressDialog!!.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Toasty.normal(this@SignIn, "Error Signing In").show()
                progressDialog!!.hide()
            }
        }

        mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "signInWithCredential:success")
                        val user = mAuth!!.currentUser
                        progressDialog!!.hide()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "signInWithCredential:failure", task.exception)
                        Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        progressDialog!!.hide()
                    }
                }
    }

    companion object {
        private val RC_SIGN_IN = 1
    }

    private fun updateUI() {

        startActivity(Intent(this@SignIn, BottomNavActivity::class.java))
        finish()
        //send to BottomNavActivity on Facebook login success
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        val user = mAuth!!.currentUser
                        updateUI()
                        progressDialog!!.hide()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Toasty.normal(this@SignIn, "Authentication failed.").show()
                        progressDialog!!.hide()
                    }

                    // ...
                }
    }

    private fun signInTwitter() {
        val provider = OAuthProvider.newBuilder("twitter.com")
        val pendingResultTask = mAuth!!.pendingAuthResult
        if (pendingResultTask != null) {
            progressDialog!!.show()
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            OnSuccessListener<AuthResult> {
                                // User is signed in.
                                // IdP data available in
                                // authResult.getAdditionalUserInfo().getProfile().
                                // The OAuth access token can also be retrieved:
                                // authResult.getCredential().getAccessToken().
                                // The OAuth secret can be retrieved by calling:
                                // authResult.getCredential().getSecret().
                                progressDialog!!.hide()
                            })
                    .addOnFailureListener {
                        // Handle failure.
                        Toasty.normal(this@SignIn, "Authentication failed.").show()
                        progressDialog!!.hide()
                    }
        } else {
            // There's no pending result so you need to start the sign-in flow.
            progressDialog!!.show()
            mAuth!!.startActivityForSignInWithProvider(/* activity= */this, provider.build())
                    .addOnSuccessListener {
                        // User is signed in.
                        // IdP data available in
                        // authResult.getAdditionalUserInfo().getProfile().
                        // The OAuth access token can also be retrieved:
                        // authResult.getCredential().getAccessToken().
                        // The OAuth secret can be retrieved by calling:
                        // authResult.getCredential().getSecret().
                        progressDialog!!.hide()
                    }
                    .addOnFailureListener {
                        // Handle failure.
                        Toasty.normal(this@SignIn, "Authentication failed.").show()
                        progressDialog!!.hide()
                    }
        }
    }//Twitter Auth with Firebase

    fun openDialogue() {
        val exampleDialog = TermsDialogue()
        exampleDialog.show(supportFragmentManager, "terms dialogue")
    }

    override fun onPause() {
        super.onPause()
        progressDialog!!.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressDialog!!.dismiss()
    }
}


