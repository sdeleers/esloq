package com.esloq.esloqapp.lockmanagment;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.esloq.esloqapp.R;

/**
 * Activity that allows the user to view the guest list and the logs. This should only be accessible
 * when the user is admin on the lock.
 */
public class LockManagementActivity extends AppCompatActivity {

    /**
     * Names of the tabs in UI.
     */
    private static final String TAB_USERLIST = "Guest List";
    private static final String TAB_LOGLIST = "Logs";

    /**
     * Mac address of the lock for which to display the lock management.
     */
    private String lockMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Add Up button to toolbar */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        SectionsStatePagerAdapter sectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        assert viewPager != null;
        viewPager.setAdapter(sectionsStatePagerAdapter);

        // Setup the tablayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        assert tabLayout != null;
        tabLayout.setupWithViewPager(viewPager);

        lockMac = getIntent().getStringExtra("lockMac");
        String lockName = getIntent().getStringExtra("lockName");
        setTitle(lockName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lock_management, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
     * Adapter to handle the tabbed fragments.
     */
    private class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

        public SectionsStatePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putString("lockMac", lockMac);
            switch (position) {
                case 0:
                    UserListFragment userListFragment = new UserListFragment();
                    userListFragment.setArguments(bundle);
                    return userListFragment;
                case 1:
                    LogListFragment logListFragment = new LogListFragment();
                    logListFragment.setArguments(bundle);
                    return logListFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return TAB_USERLIST;
                case 1:
                    return TAB_LOGLIST;
                default:
                    return null;
            }
        }
    }
}


