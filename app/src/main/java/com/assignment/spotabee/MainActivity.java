package com.assignment.spotabee;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;


import com.paypal.android.sdk.payments.PaymentActivity;
import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.assignment.spotabee.database.AppDatabase;
import com.assignment.spotabee.database.DatabaseInitializer;
import com.assignment.spotabee.database.Description;
import com.assignment.spotabee.fragments.DonationLogin;
import com.assignment.spotabee.fragments.FragmentAboutUs;
import com.assignment.spotabee.fragments.FragmentHome;
import com.assignment.spotabee.fragments.FragmentHowTo;
import com.assignment.spotabee.fragments.FragmentLeaderboard;
import com.assignment.spotabee.fragments.FragmentMap;
import com.assignment.spotabee.fragments.PaymentInfo;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.ramotion.paperonboarding.PaperOnboardingFragment;
import com.ramotion.paperonboarding.PaperOnboardingPage;
import com.ramotion.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

import static com.assignment.spotabee.Config.Config.PAYPAL_REQUEST_CODE;


/**
 * This is the main backbone of the app. It handles all fragments
 * and non-fragment specific methods.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * The activity request code for choosing an account.
     */
    public static final int CHOOSE_ACCOUNT = 1;

    /**
     * The activity request code for PayPal result.
     */
    private int payPalResultCode;

    /**
     * The context of the application that is called
     * throughout the fragments and classes.
     */
    private static Context contextOfApplication;

    /**
     * A debugging tag for the log, so that the user can see
     * where the message is being created.
     */
    private static final String TAG = "MainActivityDebug";

    /**
     * An intent contains the data from PayPal.
     */
    private Intent payPalData;

    /**
     *
     */
    private boolean mReturningWithResult = false;

    /**
     * Handles the main creation of application wide resources.
     * Examples of this include requesting permissions,
     * saving a context, initializing the database, and
     * starting the account manager.
     *
     * @param savedInstanceState The state that the application
     *                           is saved in.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SharedPreferences preferences = getSharedPreferences("com.assigment.spotabee", MODE_PRIVATE);
        preferences.edit().putBoolean("firstrun", true).apply();

        if (preferences.getBoolean("firstrun", true)) {

            final PaperOnboardingFragment onBoardingFragment
                    = PaperOnboardingFragment.newInstance(
                    getDataForOnboarding());

            FragmentTransaction ft
                    = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, onBoardingFragment);
            ft.commit();
            preferences.edit().putBoolean("firstrun", false).apply();

            onBoardingFragment.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
                @Override
                public void onRightOut() {
                    displaySelectedScreen(R.id.nav_home);
                }
            });
        } else {
            displaySelectedScreen(R.id.nav_home);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AccountManager accountManager = (AccountManager)
                getSystemService(Context.ACCOUNT_SERVICE);

        contextOfApplication = this;

        PermissionsManager.getInstance()
                .requestAllManifestPermissionsIfNecessary(this,
                        new PermissionsResultAction() {
                            @Override
                            public void onGranted() {
                                Log.v(TAG, "All permissions accepted");
                            }

                            @Override
                            public void onDenied(final String permission) {
                                Log.v(TAG, "Not all permissions accepted");
                                Log.v(TAG, "Turned down permission: "
                                        + permission);
                            }
                        });

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED
                ) {
            Intent intent =
                    accountManager.newChooseAccountIntent(null,
                            null, new String[]{"com.google"},
                            null, null,
                            null, null);
            startActivityForResult(intent, CHOOSE_ACCOUNT);
        }


        AppDatabase db = AppDatabase.getAppDatabase(getApplicationContext());

        //This clears out the database, and is called every time!
        // Remove if you need persistence!
        db.descriptionDao().nukeTable();
        db.descriptionDao().nukeUserScores();
        //This clears out the database, and is called every time!
        // Remove if you need persistence!

        DatabaseInitializer.populateAsync(db);

        Description description = new Description(
                51.4816, -3.1791,
                "Cardiff", "Rose", "None",
                1, "17-05-2018", "15:39");

        db.descriptionDao().insertDescriptions(description);


        DrawerLayout mDrawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//

    }

    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage("Welcome!", "Bees are one of the most"
                + " useful animals on Earth. Without their help in pollination, much"
                + " of the plant life you know today would die out. Bees are in danger,"
                + " and we would like your help in gathering data about them through"
                + " your interactions in everyday life.",
                Color.parseColor("#678FB4"), R.drawable.bee_2, R.drawable.bee);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("How We Will Use Data", "Using"
                + " the pictures uploaded by you, we would like to identify which"
                + " plants in Wales are attracting bees, along with the locations"
                + " of those plants. Therefore we can deduce where we need more plant"
                + " life and what kinds of plants to help these bees in their mission.",
                Color.parseColor("#65B0B4"), R.drawable.bee_trail_1, R.drawable.bee);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("So How Do I Help?", "All you"
                + " need to do to help is to keep an eye out for any bees you might see"
                + " and to snap a picture if you can. On the home screen, there are options"
                + " to take a picture from the app, use an existing photo, or to submit"
                + " information without a picture. ",
                Color.parseColor("#9B90BC"), R.drawable.bee_trail_3, R.drawable.bee);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        return elements;
    }

    /**
     * Allows fragments or java classes
     * to retrieve the activity from anywhere in the
     * app.
     *
     * @return The activity.
     */
    public AppCompatActivity getActivity() {
        return this;
    }

    /**
     * A static method that retrieves Activity context.
     *
     * @return The context for the current activity
     */
    public static Context getContextOfApplication() {
        return contextOfApplication;
    }

    /**
     * Handles navigation when device back button is pressed.
     * As of now, it navigates to the previous screen, though
     * in unique cases it makes sense for this to be changed.
     * Like navigating around login or PayPal pages.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Creates the option menu.
     *
     * @param menu The list of menu items.
     * @return Boolean whether or not it was created without
     * any error.
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handles what happens when an item is selected in the
     * options menu. This is blank for now.
     *
     * @param item The selected item.
     * @return Which item the user has tapped on.
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Method that handles all screen changes through the nav drawer.
     *
     * @param itemId A page, identified by "R.id.page_name",
     *               that the app wants to open.
     */
    private void displaySelectedScreen(final int itemId) {
        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_home:
                fragment = new FragmentHome();
                break;
            case R.id.nav_howto:
                fragment = new FragmentHowTo();
                break;
            case R.id.nav_results:
                fragment = new FragmentMap();
                break;
            case R.id.nav_aboutus:
                fragment = new FragmentAboutUs();
                break;
            case R.id.nav_leaderboard:
                fragment = new FragmentLeaderboard();
                break;
            case R.id.nav_donate:
                fragment = new DonationLogin();
                break;
            case R.id.nav_resources:
                fragment = new FragmentDownloadPdfGuide();
                break;
            default:
                Log.v(TAG, itemId + " is being requested"
                        + " in 'displaySelectedScreen, but nothing"
                        + " exists to handle that.");

        }

        //replacing the fragment
        if (fragment != null) {
            FragmentTransaction ft
                    = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    /**
     * Starts the process of changing fragments
     * when an item is selected from the Nav menu.
     *
     * @param item The menu item that should be loaded.
     * @return Returns a boolean.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }

    /**
     * Creates gets details and confirms operation from account picker.
     *
     * @param requestCode An integer that relates to the permission. So
     *                    location might be 1, account access 2, and so on.
     * @param resultCode If the result succeeded or failed
     * @param data The intent that is being requested
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_ACCOUNT && resultCode == RESULT_OK) {
            String accountName
                    = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.v(TAG, accountName);
        } else if (requestCode == CHOOSE_ACCOUNT) {
            Log.v(TAG, "There was an error in the account picker");
        } else if (requestCode == Activity.RESULT_CANCELED) {
            FragmentTransaction fragmentTransaction
                    = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new FragmentHome());
            fragmentTransaction.commit();
        } else if (requestCode == PAYPAL_REQUEST_CODE) {
            mReturningWithResult = true;
            payPalData = data;
            payPalResultCode = resultCode;
        } else {
            Log.v(TAG, "Nothing exists to handle that request code"
                    + requestCode);
        }
    }

    /**
     * Handles the destruction of the
     * MainActivity when the app is closed.
     */
    @Override
    protected void onDestroy() {
        AppDatabase.destroyInstance();
        super.onDestroy();
    }

    /**
     * Handles the results of the PayPal transaction.
     *
     * @param resultCode If the result when through or not.
     *                   Will equal 'RESULT_OK' if there
     *                   were no errors.
     * @param data The intent data that can be retrieved
     *             from the PayPal result.
     */
    public void payPalResult(final int resultCode,
                             final Intent data) {
        if (resultCode == RESULT_OK) {
            PaymentConfirmation confirmation
                    = data.getParcelableExtra(PaymentActivity
                    .EXTRA_RESULT_CONFIRMATION);
            if (confirmation != null) {
                try {
                    String paymentDetails
                            = confirmation.toJSONObject().toString(7);
                    PaymentInfo paymentInfo = new PaymentInfo();
                    Bundle args = new Bundle();
                    args.putString("paymentInfo", paymentDetails);
                    paymentInfo.setArguments(args);
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.content_frame, paymentInfo).commit();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), "Cancel", Toast.LENGTH_SHORT).show();
        } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(getActivity(), "Invalid", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Handles the resuming of the app
     * from the PayPal feature. Causes issues
     * if does not reset the mReturningWithResult.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mReturningWithResult) {
            payPalResult(payPalResultCode, payPalData);
        }
        // Reset the boolean flag back to false for next time.
        mReturningWithResult = false;
    }
}
