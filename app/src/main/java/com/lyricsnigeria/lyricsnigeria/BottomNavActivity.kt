package com.lyricsnigeria.lyricsnigeria

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.Display
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import de.hdodenhof.circleimageview.CircleImageView
import es.dmoral.toasty.Toasty

class BottomNavActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    private var mDatabase: DatabaseReference? = null
    private var mChatBase: DatabaseReference? = null

    private var bottomNav: BottomNavigationView? = null
    private var drawerNav: NavigationView? = null
    private var drawer: DrawerLayout? = null

    private var mExpandBtn: ImageView? = null
    private var mSignOut: TextView? = null
    private var mSignIn: TextView? = null

    private val fragment1: Fragment = LyricsFragment()
    private val fragment2: Fragment = VideosFragment()
    private val fragment3: Fragment = NewsFragment()
    private val fragment4: Fragment = ChatFragment()
    private val fragment5: Fragment = FavouritesFragment()
    private val fragment7: Fragment = AboutFragment()
    private val fragment8: Fragment = FeedbackFragment()
    private var active: Fragment = LyricsFragment()

    private var DAY_THEME = 1
    private var NIGHT_THEME = 2
    private var THEME: Int = 0

    private var isDayEnabled: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            setTheme(R.style.DayTheme)
            isDayEnabled = true
        } else {
            if (SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME) {
                setTheme(R.style.NightTheme)
                isDayEnabled = false
            } else {
                setTheme(R.style.AppTheme)
                isDayEnabled = true
            }
        }
        setContentView(R.layout.activity_bottom_nav)

        MobileAds.initialize(this, getString(R.string.admob_app_id))

        drawer = findViewById(R.id.drawer_layout) //get drawer xml id
        drawer!!.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                hideKeyboard(this@BottomNavActivity)//hides keyboard on drawer slide
            }
        })

        bottomNav = findViewById(R.id.navigation_view)
        bottomNav!!.setOnNavigationItemSelectedListener(navListener)
        bottomNav!!.itemIconTintList = null
        val menu: Menu = bottomNav!!.menu

        drawerNav = findViewById(R.id.nav_view)
        drawerNav!!.setNavigationItemSelectedListener(drawerListener)

        if (SharedPreferencesManager(this).retrieveInt("theme", THEME) == DAY_THEME) {
            drawerNav!!.setCheckedItem(R.id.day_theme_menu)
        } else {
            if (SharedPreferencesManager(this).retrieveInt("theme", THEME) == NIGHT_THEME) {
                drawerNav!!.setCheckedItem(R.id.night_theme_menu)
            } else {
                drawerNav!!.setCheckedItem(R.id.day_theme_menu)
            }
        }

        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment1, "LYRICS_FRAG").hide(fragment1).commit()
        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment2, "VIDEOS_FRAG").hide(fragment2).commit()
        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment3, "NEWS_FRAG").hide(fragment3).commit()
        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment4, "CHAT_FRAG").hide(fragment4).commit()
        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment5, "FAVOURITES_FRAG").hide(fragment5).commit()
        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment7, "ABOUT_FRAG").hide(fragment7).commit()
        supportFragmentManager.beginTransaction().add(R.id.frag_container, fragment8, "FEEDBACK_FRAG").hide(fragment8).commit()


        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                val headerView: View = drawerNav!!.inflateHeaderView(R.layout.nav_header_signed_in)
                val profName: TextView = headerView.findViewById(R.id.nav_username)
                profName.text = mAuth!!.currentUser!!.displayName

                val profEmail: TextView = headerView.findViewById(R.id.nav_email)
                profEmail.text = mAuth!!.currentUser!!.email

                val options = RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.blank_portrait)
                        .error(R.drawable.blank_portrait)
                val profPic: CircleImageView = headerView.findViewById(R.id.user_pic)
                Glide.with(this)
                        .load(mAuth!!.currentUser!!.photoUrl.toString())
                        .apply(options)
                        .into(profPic)

                mExpandBtn = headerView.findViewById(R.id.nav_expand_btn)
                mExpandBtn!!.setOnClickListener {
                    if (mSignOut!!.visibility == View.GONE) {
                        mSignOut!!.visibility = View.VISIBLE
                        mExpandBtn!!.setImageResource(R.drawable.ic_expand_less)
                    } else {
                        mSignOut!!.visibility = View.GONE
                        mExpandBtn!!.setImageResource(R.drawable.ic_expand_more)
                    }
                }
                mSignOut = headerView.findViewById(R.id.nav_signout)
                mSignOut!!.setOnClickListener {
                    mAuth!!.signOut()
                    headerView.visibility = View.GONE
                    restartApp()
                }
            } else {
                if (firebaseAuth.currentUser == null) {
                    val headerView: View = drawerNav!!.inflateHeaderView(R.layout.nav_header_signed_out)
                    mSignIn = headerView.findViewById(R.id.nav_signin)
                    mSignIn!!.setOnClickListener {
                        startActivity(Intent(this@BottomNavActivity, SignIn::class.java))
                    }
                }
            }
        }

        mChatBase = FirebaseDatabase.getInstance().reference.child("Chat")
        mChatBase!!.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, p1: String?) {//add listener for child add event
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                if (bottomNav!!.selectedItemId != R.id.nav_chat) {
                    menu.findItem(R.id.nav_chat).setIcon(R.drawable.forum_new_message)
                } else {
                    menu.findItem(R.id.nav_chat).setIcon(R.drawable.forum_message)
                }
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        if (savedInstanceState == null) {//if the app is closed and restarted then savedInstanceState is null, if the device is rotated it is not null and therefore app will retain current form
            supportFragmentManager.beginTransaction().hide(active).show(fragment1).commit()
            active = fragment1
            bottomNav!!.selectedItemId = R.id.nav_lyrics
        }
        supportFragmentManager.beginTransaction().hide(active).show(fragment1).commit()
        active = fragment1
        bottomNav!!.selectedItemId = R.id.nav_lyrics

        mAuth!!.addAuthStateListener(this.mAuthListener!!)
    }

    public override fun onStart() {
        super.onStart()
        //mAuth!!.addAuthStateListener(this.mAuthListener!!)

        mDatabase = FirebaseDatabase.getInstance().reference.child("Documentation")//set database reference for update document
        mDatabase!!.child("1").addValueEventListener(object : ValueEventListener {
            //add value event listener to listen for value of the database path
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                AppUpdater(this@BottomNavActivity)//notify user of new app update available
                        .setUpdateFrom(UpdateFrom.JSON)
                        .setUpdateJSON(dataSnapshot.child("update").value.toString())//reference to the update document location
                        .setDisplay(Display.DIALOG)
                        .showEvery(3)
                        .setButtonDoNotShowAgain("")
                        .setCancelable(false)
                        .start()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun hideKeyboard(activity: Activity) {//hide keyboard function
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private val navListener = object : BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.nav_home -> {
                    drawer!!.openDrawer(GravityCompat.START)
                    return false
                }

                R.id.nav_lyrics -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment1).addToBackStack("Lyrics").commit()
                    active = fragment1
                    hideKeyboard(this@BottomNavActivity)
                    return true
                }
                R.id.nav_videos -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment2).addToBackStack("Videos").commit()
                    active = fragment2
                    hideKeyboard(this@BottomNavActivity)
                    return true
                }
                R.id.nav_news -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment3).addToBackStack("News").commit()
                    active = fragment3
                    hideKeyboard(this@BottomNavActivity)
                    return true
                }
                R.id.nav_chat -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment4).addToBackStack("Chat").commit()
                    active = fragment4
                    val menu: Menu = bottomNav!!.menu
                    menu.findItem(R.id.nav_chat).setIcon(R.drawable.forum_message)
                    hideKeyboard(this@BottomNavActivity)
                    return true
                }
            }
            return false
        }
    }

    private val drawerListener = object : NavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(@NonNull item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.favourites_menu -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment5).addToBackStack("Favourites").commit()
                    active = fragment5
                    drawer!!.closeDrawer(GravityCompat.START)
                    return false
                }
                R.id.about_menu -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment7).addToBackStack("About").commit()
                    active = fragment7
                    drawer!!.closeDrawer(GravityCompat.START)
                    return false
                }
                R.id.feedback_menu -> {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment8).addToBackStack("Feedback").commit()
                    active = fragment8
                    drawer!!.closeDrawer(GravityCompat.START)
                    return false
                }
                R.id.day_theme_menu -> {
                    if (!isDayEnabled) {
                        val THEME = DAY_THEME
                        SharedPreferencesManager(applicationContext).storeInt("theme", THEME)
                        restartApp()

                        drawer!!.closeDrawer(GravityCompat.START)
                        bottomNav!!.selectedItemId = R.id.nav_lyrics
                        isDayEnabled
                        return true
                    } else {
                        Toasty.normal(this@BottomNavActivity, "Day Mode Active").show()
                    }
                }
                R.id.night_theme_menu -> {
                    if (isDayEnabled) {
                        val THEME = NIGHT_THEME
                        SharedPreferencesManager(applicationContext).storeInt("theme", THEME)
                        restartApp()

                        drawer!!.closeDrawer(GravityCompat.START)
                        bottomNav!!.selectedItemId = R.id.nav_lyrics
                        !isDayEnabled
                        return true
                    } else {
                        Toasty.normal(this@BottomNavActivity, "Night Mode Active").show()
                    }
                }
                R.id.rate_menu -> {
                    drawer!!.closeDrawer(GravityCompat.START)
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.lyricsnigeria.lyricsnigeria&hl=en_US")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.lyricsnigeria.lyricsnigeria&hl=en_US")))
                    }
                    return false
                }
                R.id.share_menu -> {
                    drawer!!.closeDrawer(GravityCompat.START)
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT,
                            "Hey!! check out Lyrics Nigeria: Social & Entertainment Hub at : https://play.google.com/store/apps/details?id=com.lyricsnigeria.lyricsnigeria&hl=en_US")
                    sendIntent.type = "text/plain"
                    startActivity(sendIntent)
                    return false
                }
            }
            return false
        }
    }

    override fun onBackPressed() {
        val mFrag = supportFragmentManager.findFragmentByTag("FAVOURITES_FRAG")
        val mFrag3 = supportFragmentManager.findFragmentByTag("ABOUT_FRAG")
        val mFrag4 = supportFragmentManager.findFragmentByTag("FEEDBACK_FRAG")
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            if (mFrag != null && mFrag.isVisible) {
                supportFragmentManager.beginTransaction().hide(active).show(fragment1).commit()
                active = fragment1
                bottomNav!!.selectedItemId = R.id.nav_lyrics
            } else {
                if (mFrag3 != null && mFrag3.isVisible) {
                    supportFragmentManager.beginTransaction().hide(active).show(fragment1).commit()
                    active = fragment1
                    bottomNav!!.selectedItemId = R.id.nav_lyrics
                } else {
                    if (mFrag4 != null && mFrag4.isVisible) {
                        supportFragmentManager.beginTransaction().hide(active).show(fragment1).commit()
                        active = fragment1
                        bottomNav!!.selectedItemId = R.id.nav_lyrics
                    } else {
                        moveTaskToBack(true)
                    }
                }
            }

        }
    }

    class SharedPreferencesManager(context: Context) {

        private var sPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        private var sEditor: SharedPreferences.Editor? = null

        private val editor: SharedPreferences.Editor
            get() {
                return sPreferences.edit()
            }

        fun retrieveInt(tag: String, defValue: Int): Int {
            return sPreferences.getInt(tag, defValue)
        }

        fun storeInt(tag: String, defValue: Int) {
            sEditor = editor
            sEditor!!.putInt(tag, defValue)
            sEditor!!.commit()
        }
    }

    private fun restartApp() {
        val i = Intent(this@BottomNavActivity, BottomNavActivity::class.java)
        startActivity(i)
    }
}

