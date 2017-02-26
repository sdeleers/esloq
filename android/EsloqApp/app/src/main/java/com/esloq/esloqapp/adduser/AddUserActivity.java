package com.esloq.esloqapp.adduser;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.esloq.esloqapp.R;
import com.esloq.esloqapp.data.Injection;

/**
 * Activity to add a user to a lock. To add a user to a lock the local user enters the email address
 * of the user he wants to add and if it's a guest or administrator.
 */
public class AddUserActivity extends AppCompatActivity implements AddUserContract.View {

    /**
     * Listener for the user's UI actions.
     */
    private AddUserContract.UserActionsListener mActionListener;

    /**
     * Lock that the user needs to be added to.
     */
    private String lockMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Add Up to toolbar */
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        /* Spinner */
        Spinner spinner = (Spinner) findViewById(R.id.permission_spinner);
        /* Create an ArrayAdapter using the string array and a default spinner layout */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.permissions_array, android.R.layout.simple_spinner_item);
        /* Specify the layout to use when the list of choices appears */
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /* Apply the adapter to the spinner */
        assert spinner != null;
        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
//                GridLayout layout = (GridLayout) findViewById(R.id.guest_options);
//                assert layout != null;
//                if (position == 1) {
//                    layout.setVisibility(View.GONE);
//                } else {
//                    layout.setVisibility(View.VISIBLE);
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parentView) {
//
//            }
//        });

        /* Set focus to email field */
        View view = findViewById(R.id.email_add_user);
        assert view != null;
        view.requestFocus();

        final Intent intent = getIntent();
        lockMac = intent.getStringExtra("lockMac");

        mActionListener = new AddUserPresenter(Injection.provideLockDataRepository(getApplicationContext()), this);

//        final Calendar c = Calendar.getInstance();
//        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
//        EditText fromDateView = (EditText) findViewById(R.id.from_date);
//        assert fromDateView != null;
//        EditText untilDateView = (EditText) findViewById(R.id.until_date);
//        assert untilDateView != null;
//        fromDateView.setText(dateFormat.format(c.getTime()));
//        untilDateView.setText(dateFormat.format(c.getTime()));
//        dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
//        ((EditText) findViewById(R.id.from_time)).setText(dateFormat.format(c.getTime()));
//        ((EditText) findViewById(R.id.until_time)).setText(dateFormat.format(c.getTime()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                addUser();
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
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

//    @Override
//    public void onClick(final View view){
//        final Calendar c = Calendar.getInstance();
//        final int year = c.get(Calendar.YEAR);
//        final int month = c.get(Calendar.MONTH);
//        final int day = c.get(Calendar.DAY_OF_MONTH);
//        final int hour = c.get(Calendar.HOUR);
//        final int minute = c.get(Calendar.MINUTE);
//        switch (view.getId()) {
//            case R.id.from_date:
//            case R.id.until_date:
//                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog
//                        .OnDateSetListener() {
//                    @Override
//                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
//                        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
//                        ((EditText) view).setText(dateFormat.format(new GregorianCalendar(year,
//                                month, day).getTime()));
//                    }
//                }, year, month, day);
//                datePickerDialog.show();
//                break;
//            case R.id.from_time:
//            case R.id.until_time:
//                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog
//                        .OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
//                        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
//                        ((EditText) view).setText(dateFormat.format(new GregorianCalendar(year,
//                                month, day, hour, minute).getTime()));
//                    }
//                }, hour, minute, true);
//                timePickerDialog.show();
//                break;
//
//        }
//    }

    @Override
    public void showAddUserError() {
        Toast.makeText(this, "Unable to add user.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUserList() {
        finish();
    }

    /**
     * Sends add user request to the presenter.
     */
    private void addUser() {
        EditText emailView = (EditText) findViewById(R.id.email_add_user);
        assert emailView != null;
        String email = emailView.getText().toString().toLowerCase();
        Spinner permissionSpinner = (Spinner) findViewById(R.id.permission_spinner);
        assert permissionSpinner != null;
        int itemPosition = permissionSpinner.getSelectedItemPosition();
        boolean isAdmin = itemPosition == 1;
        mActionListener.addUser(email, lockMac, isAdmin);
    }
}
