package com.esloq.esloqapp.addlock;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.esloq.esloqapp.data.Injection;
import com.esloq.esloqapp.R;

/**
 * Activity to add a new esloq to the user's lock list; this causes the local user to become an
 * admin on the lock. This can only be done when there is no admin on the lock yet, and typically
 * only happens when the local user has bought a new esloq or after the current user has reset
 * the lock.
 */
public class AddLockActivity extends AppCompatActivity implements AddLockContract.View {

    /**
     * Listener for the user's UI actions.
     */
    private AddLockContract.UserActionsListener mActionListener;

    /**
     * MAC address of the Bluetooth IC of the lock that will be added.
     */
    private String lockMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Add Up to toolbar */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* Set focus to username field */
        View view = findViewById(R.id.add_lock_name);
        assert view != null;
        view.requestFocus();

        final Intent intent = getIntent();
        lockMac = intent.getStringExtra("lockMac");

        mActionListener = new AddLockPresenter(Injection.provideLockDataRepository(getApplicationContext()), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_lock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                addLock();
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it. E.g.: for up button
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Removes focus from editText when user clicks outside of it.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void showAddLockError() {
        Toast.makeText(getApplicationContext(), "Unable to add lock", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLockList() {
        finish();
    }

    /**
     * Sends add lock request to the presenter.
     */
    private void addLock() {
        EditText lockNameView = (EditText) findViewById(R.id.add_lock_name);
        assert lockNameView != null;
        String lockName = lockNameView.getText().toString();
        RadioButton locksRight = (RadioButton) findViewById(R.id.locks_right);
        assert locksRight != null;
        boolean lockClockwise = locksRight.isChecked();
        mActionListener.addLock(lockMac, lockName, lockClockwise);
    }

}
