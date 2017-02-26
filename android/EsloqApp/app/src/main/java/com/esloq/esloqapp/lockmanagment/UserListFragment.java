package com.esloq.esloqapp.lockmanagment;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.esloq.esloqapp.cursorrecycleradapter.CursorRecyclerAdapter;
import com.esloq.esloqapp.data.Injection;
import com.esloq.esloqapp.data.LoaderProvider;
import com.esloq.esloqapp.util.DividerItemDecoration;
import com.esloq.esloqapp.R;
import com.esloq.esloqapp.selector.SelectableModeCallback;
import com.esloq.esloqapp.cursorrecycleradapter.SelectableViewHolder;
import com.esloq.esloqapp.selector.SingleSelector;
import com.esloq.esloqapp.adduser.AddUserActivity;

/**
 * Fragment that displays the users that have access on a given lock.
 */
public class UserListFragment extends Fragment implements UserListContract.View, View
        .OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Listener for the user's UI actions.
     */
    private UserListContract.UserActionsListener mActionListener;

    /**
     * Adapter to display the log list in the RecyclerView.
     */
    private UserListAdapter userListAdapter;

    /**
     * Type of selector in action mode. Allows for one selection at a time.
     */
    private final SingleSelector mSelector = new SingleSelector();

    /**
     * The lock for which to display the user list.
     */
    private String lockMac;

    /**
     * Provider for the data to show in UI.
     */
    private LoaderProvider mLoaderProvider;

    /* Used to begin/stop contextual menu */
//    private ActionMode actionMode;

    /**
     * Contextual action mode. Menu that appears when a user is clicked.
     */
    private final ActionMode.Callback actionModeCallback = new SelectableModeCallback(mSelector) {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_user_list_context, menu);
            return true;
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete_user:
                    removeUser(mSelector.getPosition());
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.user_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        userListAdapter = new UserListAdapter();
        recyclerView.setAdapter(userListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        FloatingActionButton buttonAddUser = (FloatingActionButton) rootView.findViewById(R.id.button_add_user);
        buttonAddUser.setOnClickListener(this);

        lockMac = getArguments().getString("lockMac");

//        /* Remove contextual menu when viewpager scrolls */
//        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.pager);
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                if(actionMode != null) {
//                    actionMode.finish();
//                }
//            }
//            @Override
//            public void onPageSelected(int position) {}
//            @Override
//            public void onPageScrollStateChanged(int state) {}
//        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoaderProvider = new LoaderProvider(getActivity());
        getActivity().getLoaderManager().initLoader(0, null, this);
        mActionListener = new UserListPresenter(Injection.provideLockDataRepository(getContext()), this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add_user:
                addUser();
                break;
        }
    }

    @Override
    public void showAddUser(String lockMac) {
        Intent intent = new Intent(getActivity(), AddUserActivity.class);
        intent.putExtra("lockMac", lockMac);
        startActivity(intent);
    }

    @Override
    public void showRemoveUserError() {
        Toast.makeText(getContext(), "Unable to remove user.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.getLockUsersCursorLoader(lockMac);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        userListAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        userListAdapter.changeCursor(null);
    }

    /**
     * Start activity to add new user to selected lock.
     */
    private void addUser() {
        mActionListener.openAddUser(lockMac);
    }

    /**
     * Remove selected user from this lock.
     *
     * @param position The position of the user to be removed.
     */
    private void removeUser(int position) {
        Cursor c = userListAdapter.getCursor();
        c.moveToPosition(position);
        int userId = c.getInt(0);
        mActionListener.removeUser(userId, lockMac);
    }

    /**
     * Adapter class that manages the ListView containing the lock's users.
     */
    private class UserListAdapter extends CursorRecyclerAdapter<UserListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder) {
            holder.userName.setText(getCursor().getString(1));
            if (getCursor().getInt(2) == 1) {
                holder.userName.setEnabled(true);
            } else {
                holder.userName.setEnabled(false);
            }
            if (getCursor().getInt(3) == 1) {
                holder.icon.setImageResource(R.drawable.ic_supervisor_account);
            } else {
                holder.icon.setImageResource(R.drawable.ic_person);
            }
        }

        /**
         * Class containing the views contained within a row of the user list.
         */
        class ViewHolder extends SelectableViewHolder {

            private final TextView userName;
            private final ImageView icon;

            public ViewHolder(View itemView) {
                super(itemView, mSelector, getActivity(), actionModeCallback);
                itemView.setOnLongClickListener(this);
                itemView.setOnClickListener(this);
                userName = (TextView) itemView.findViewById(R.id.user_name);
                icon = (ImageView) itemView.findViewById(R.id.user_icon);
            }
        }
    }
}
