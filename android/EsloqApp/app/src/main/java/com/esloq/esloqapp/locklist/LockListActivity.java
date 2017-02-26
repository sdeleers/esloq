package com.esloq.esloqapp.locklist;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.esloq.esloqapp.BuildConfig;
import com.esloq.esloqapp.cursorrecycleradapter.CursorRecyclerAdapter;
import com.esloq.esloqapp.data.Injection;
import com.esloq.esloqapp.data.LoaderProvider;
import com.esloq.esloqapp.home.HomeActivity;
import com.esloq.esloqapp.util.DividerItemDecoration;
import com.esloq.esloqapp.lockmanagment.LockManagementActivity;
import com.esloq.esloqapp.R;
import com.esloq.esloqapp.scan.ScanActivity;
import com.esloq.esloqapp.selector.SelectableModeCallback;
import com.esloq.esloqapp.cursorrecycleradapter.SelectableViewHolder;
import com.esloq.esloqapp.selector.Selector;
import com.esloq.esloqapp.selector.SingleSelector;
import com.esloq.esloqapp.cursorrecycleradapter.ViewClickListener;
import com.esloq.esloqapp.lock.LockActivity;

import io.fabric.sdk.android.Fabric;

/**
 * Activity that displays the locks that the local user has access to. The permissions that the
 * local user has on these locks may vary. When the user clicks on a lock the
 * <code>LockActivity</code> is started and a <code>Lock</code> representation of this lock is sent
 * along with the intent. From this activity the user can also add a lock that is in Bluetooth range.
 */
public class LockListActivity extends HomeActivity implements LockListContract.View,
        ViewClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Listener for the user's UI actions.
     */
    private LockListContract.UserActionsListener mActionListener;

    /**
     * Adapter to display the lock list in the RecyclerView.
     */
    private LockListAdapter lockListAdapter;

    /**
     * Type of selector in action mode. Allows for one selection at a time.
     */
    private final Selector mSelector = new SingleSelector();

    /**
     * Provider for the data to show in UI.
     */
    private LoaderProvider mLoaderProvider;

    /**
     * Contextual action mode, menu that appears when a user is clicked.
     */
    private final ActionMode.Callback actionModeCallback = new SelectableModeCallback(mSelector) {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_lock_list, menu);
            return true;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete_user:
                    removeLock(mSelector.getPosition());
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_lock_list);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lock_list);
        assert recyclerView != null;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lockListAdapter = new LockListAdapter();
        recyclerView.setAdapter(lockListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle(R.string.title_activity_locklist);

        mActionListener = new LockListPresenter(Injection.provideLockDataRepository(getApplicationContext()), this);

        // Set up floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_lock);
        assert fab != null;
        fab.setImageResource(R.drawable.ic_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.addLock();
            }
        });

        mLoaderProvider = new LoaderProvider(this);
        getLoaderManager().initLoader(0, null, this);
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
     * Gets called when the user clicks the Add Lock button. This causes the ScanActivity to be
     * started which will scan for nearby esloq devices that the user might want to add.
     */
    @Override
    public void showScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }

    @Override
    public void showRemoveLockError() {
        Toast.makeText(getApplicationContext(), "Unable to remove lock.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLockDetails(String lockMac, String lockName) {
        final Intent intent = new Intent(this, LockActivity.class);
        intent.putExtra("lockMac", lockMac);
        intent.putExtra("lockName", lockName);
        startActivity(intent);
    }

    @Override
    public void showLockManagement(String lockMac, String lockName) {
        final Intent intent = new Intent(this, LockManagementActivity.class);
        intent.putExtra("lockMac", lockMac);
        intent.putExtra("lockName", lockName);
        startActivity(intent);
    }

    @Override
    public void onViewClicked(int position, View view) {
        Cursor c = lockListAdapter.getCursor();
        c.moveToPosition(position);
        switch (view.getId()) {
            case R.id.lock_name:
                mActionListener.openLockDetails(c.getString(0), c.getString(1));
                break;
            case R.id.lock_management:
                mActionListener.openLockManagement(c.getString(0), c.getString(1));
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.getLocksCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        lockListAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        lockListAdapter.changeCursor(null);
    }

    /**
     * Sends request to server to delete the lock.
     *
     * @param position The position of lock to be deleted.
     */
    private void removeLock(int position) {
        Cursor c = lockListAdapter.getCursor();
        c.moveToPosition(position);
        String mac = c.getString(0);
        mActionListener.removeLock(mac);
    }

    /**
     * Adapter class that is responsible for selecting what data to display in each ViewHolder.
     */
    private class LockListAdapter extends CursorRecyclerAdapter<LockListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lock_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder) {
            holder.lockName.setText(getCursor().getString(1));
            if (getCursor().getInt(2) == 1) {
                holder.lockManagement.setVisibility(View.VISIBLE);
            } else {
                holder.lockManagement.setVisibility(View.GONE);
            }
        }

        /**
         * Class containing the views contained within a row of the lock list.
         */
        class ViewHolder extends SelectableViewHolder {
            private final TextView lockName;
            private final ImageView lockManagement;

            public ViewHolder(View itemView) {
                super(itemView, mSelector, LockListActivity.this, actionModeCallback);
                setViewClickListener(LockListActivity.this);
                //itemView.setOnLongClickListener(this);
                lockName = (TextView) itemView.findViewById(R.id.lock_name);
                lockManagement = (ImageView) itemView.findViewById(R.id.lock_management);
                lockName.setOnClickListener(this);
                lockManagement.setOnClickListener(this);
                lockName.setOnLongClickListener(this);
                lockManagement.setOnLongClickListener(this);
            }
        }
    }
}
