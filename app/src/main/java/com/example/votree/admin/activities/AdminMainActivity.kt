package com.example.votree.admin.activities

import DialogFragmentListener
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.votree.R
import com.example.votree.admin.fragments.AccountDetailFragment
import com.example.votree.admin.fragments.AccountListFragment
import com.example.votree.admin.fragments.ProductBoughtListFragment
import com.example.votree.admin.fragments.ProductDetailFragment
import com.example.votree.admin.fragments.ReportDetailFragment
import com.example.votree.admin.fragments.ReportListFragment
import com.example.votree.admin.fragments.TipDetailFragment
import com.example.votree.admin.fragments.TipListFragment
import com.example.votree.admin.fragments.TransactionDetailFragment
import com.example.votree.admin.interfaces.OnItemClickListener
import com.example.votree.databinding.FragmentUserProfileBinding
import com.example.votree.users.activities.SignInActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class AdminMainActivity : AppCompatActivity(), OnItemClickListener, SearchView.OnQueryTextListener, NavigationView.OnNavigationItemSelectedListener, DialogFragmentListener {

    private val CALL_PHONE_PERMISSION_REQUEST_CODE = 1
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var topAppBar: MaterialToolbar
    private val db = Firebase.firestore
    private val SharedPrefs = "sharedPrefs"
    private var currentFlag: Int = 0
    private val backStackListener = FragmentManager.OnBackStackChangedListener {
        val currentBackStackEntryCount = supportFragmentManager.backStackEntryCount
        if (currentBackStackEntryCount == 1) {
            setupNormalActionBar()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_admin)

        // Check for CALL_PHONE permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            // Request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PHONE_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission has already been granted
            // You can now use CALL_PHONE
        }

        drawerLayout = findViewById(R.id.mainLayout)
        topAppBar = findViewById(R.id.topAppBar)
        setSupportActionBar(topAppBar)

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    Log.d("ListActivity", "More clicked")
                    true
                }

                else -> false
            }
        }

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {

                when (currentFlag) {
                    getCurrentFlag() -> return
                    else -> {
                        setCurrentFlag(currentFlag)
                    }
                }

                when (getCurrentFlag()) {
                    0 -> setCurrentFragment(TipListFragment())
                    1 -> setCurrentFragment(AccountListFragment())
                    2 -> setCurrentFragment(ReportListFragment())
                    else -> setCurrentFragment(TipListFragment())
                }

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, getCurrentFragment())
                    .commit()
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            topAppBar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val defaultFragment = when (getCurrentFlag()) {
            0 -> TipListFragment()
            1 -> AccountListFragment()
            2 -> ReportListFragment()
            else -> TipListFragment()
        }
        when (getCurrentFlag()) {
            0 -> {
                topAppBar.title = "Tips"
                setCurrentFragment(TipListFragment())
            }

            1 -> {
                topAppBar.title = "Accounts"
                setCurrentFragment(AccountListFragment())
            }

            2 -> {
                topAppBar.title = "Reports"
                setCurrentFragment(ReportListFragment())
            }

            else -> {
                topAppBar.title = "Tips"
                setCurrentFragment(TipListFragment())
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, defaultFragment)
            .addToBackStack(null)
            .commit()

        supportFragmentManager.addOnBackStackChangedListener(backStackListener)
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CALL_PHONE_PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted
                    // You can now use CALL_PHONE
                } else {
                    // Permission denied
                    // You may inform the user or handle this case as needed
                }
                return
            }
        }
    }

    override fun onDestroy() {
        // Remove the listener when the activity is destroyed to avoid memory leaks
        supportFragmentManager.removeOnBackStackChangedListener(backStackListener)
        super.onDestroy()
    }

    override fun onItemClicked(view: View?, position: Int) {}
    override fun onProductItemClicked(view: View?, position: Int) {
        setCurrentFragment(ProductDetailFragment())
        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.menu.findItem(R.id.search).isVisible = false
        topAppBar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onTransactionItemClicked(view: View?, position: Int) {
        setCurrentFragment(TransactionDetailFragment())
        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.menu.findItem(R.id.search).isVisible = false
        topAppBar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onTipItemClicked(view: View?, position: Int) {
        setCurrentFragment(TipDetailFragment())
        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.menu.findItem(R.id.search).isVisible = false
        topAppBar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onAccountItemClicked(view: View?, position: Int) {
        setCurrentFragment(AccountDetailFragment())
        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.menu.findItem(R.id.search).isVisible = false
        topAppBar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onReportItemClicked(view: View?, position: Int, processStatus: Boolean) {
        setCurrentFragment(ReportDetailFragment())
        val topAppBar: MaterialToolbar = findViewById(R.id.topAppBar)
        topAppBar.setNavigationIcon(R.drawable.icon_back)
        topAppBar.menu.findItem(R.id.search).isVisible = false
        topAppBar.title =
            view?.findViewById<TextView>(R.id.report_list_item_short_description)?.text
        when (processStatus) {
            true -> topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_primary))
            false -> topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_error))
        }
        topAppBar.setNavigationOnClickListener {
            supportFragmentManager.popBackStack()
        }
    }

    override fun searchItem(query: String) {}

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.top_app_bar, menu)
        val search = menu?.findItem(R.id.search)
        val searchView = search?.actionView as SearchView
        searchView.isSubmitButtonEnabled = true
        searchView.setOnQueryTextListener(this)

        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            val fragment = when (getCurrentFlag()) {
                0 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
                1 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? AccountListFragment
                2 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? ReportListFragment
                else -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
            }
            fragment?.searchItem(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            val fragment = when (getCurrentFlag()) {
                0 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
                1 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? AccountListFragment
                2 -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? ReportListFragment
                else -> supportFragmentManager.findFragmentById(R.id.fragment_container) as? TipListFragment
            }
            fragment?.searchItem(query)
        }
        return true
    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when (p0.itemId) {
            R.id.nav_tips -> {
                currentFlag = 0
                setCurrentFragment(TipListFragment())
                drawerLayout.closeDrawer(GravityCompat.START)
                topAppBar.title = "Tips"

                return true
            }

            R.id.nav_accounts -> {
                currentFlag = 1
                setCurrentFragment(AccountListFragment())
                drawerLayout.closeDrawer(GravityCompat.START)
                topAppBar.title = "Accounts"

                return true
            }

            R.id.nav_reports -> {
                currentFlag = 2
                setCurrentFragment(ReportListFragment())
                drawerLayout.closeDrawer(GravityCompat.START)
                topAppBar.title = "Reports"

                return true
            }

            R.id.nav_logout -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes") { _, _ ->
                        SignInActivity().signOut()
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                return true
            }

            else -> return false
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        super.onBackPressed()
    }

    private fun getCurrentFlag(): Int {
        val sharedPreferences = getSharedPreferences(SharedPrefs, MODE_PRIVATE)
        return sharedPreferences.getInt("currentFlag", 0)
    }

    private fun getCurrentFragment(): Fragment {
        val sharedPreferences = getSharedPreferences(SharedPrefs, MODE_PRIVATE)
        val fragmentName = sharedPreferences.getString("currentFragment", null)
        return when (fragmentName) {
            "TipListFragment" -> TipListFragment()
            "AccountListFragment" -> AccountListFragment()
            "ReportListFragment" -> ReportListFragment()
            "TipDetailFragment" -> TipDetailFragment()
            "AccountDetailFragment" -> AccountDetailFragment()
            "ReportDetailFragment" -> ReportDetailFragment()
            "ProductBoughtListFragment" -> ProductBoughtListFragment()
            "ProductDetailFragment" -> ProductDetailFragment()
            else -> TipListFragment()
        }
    }

    fun setCurrentFlag(currentFlag: Int) {
        val sharedPreferences = getSharedPreferences(SharedPrefs, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("currentFlag", currentFlag)
        editor.apply()
    }

    fun setCurrentFragment(currentFragment: Fragment) {
        val sharedPreferences = getSharedPreferences(SharedPrefs, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("currentFragment", extractFragmentName(currentFragment.toString()))
        editor.apply()
    }

    private fun setupNormalActionBar() {
        topAppBar.setTitleTextColor(resources.getColor(R.color.md_theme_primary))
        setSupportActionBar(topAppBar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            topAppBar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        topAppBar.title = when (getCurrentFlag()) {
            0 -> "Tips"
            1 -> "Accounts"
            2 -> "Reports"
            else -> "Tips"
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.more -> {
                    Log.d("ListActivity", "More clicked")
                    true
                }

                else -> false
            }
        }
    }

    private fun extractFragmentName(fragmentString: String): String {
        val endIndex = fragmentString.indexOf('{')
        return fragmentString.substring(0, endIndex)
    }

    fun dateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val inputDate = inputFormat.parse(date)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

        return outputFormat.format(inputDate)
    }

    override fun updateExpireBanDateToFirestore(daysToAdd: Int, userId: String) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, daysToAdd)
        db.collection("users").document(userId)
            .update("expireBanDate", Timestamp(calendar.time))
    }
}
