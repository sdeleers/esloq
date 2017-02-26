package com.esloq.esloqapp.lockmanagment;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.esloq.esloqapp.cursorrecycleradapter.CursorRecyclerAdapter;
import com.esloq.esloqapp.data.LoaderProvider;
import com.esloq.esloqapp.util.DividerItemDecoration;
import com.esloq.esloqapp.R;

import java.text.DateFormat;
import java.util.GregorianCalendar;

/**
 * Fragment that displays the logs that have occurred on a given lock.
 */
public class LogListFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Adapter to display the lock list in the RecyclerView.
     */
    private LogListAdapter logListAdapter;

    /**
     * The lock for which to display the log list.
     */
    private String lockMac;

    /**
     * Provider for the data to show in UI.
     */
    private LoaderProvider mLoaderProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_list, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.log_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        logListAdapter = new LogListAdapter();
        recyclerView.setAdapter(logListAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        lockMac = getArguments().getString("lockMac");

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoaderProvider = new LoaderProvider(getActivity());
        getActivity().getLoaderManager().initLoader(1, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mLoaderProvider.getLockLogsCursorLoader(lockMac);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        logListAdapter.changeCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        logListAdapter.changeCursor(null);
    }

    /**
     * Adapter class that is responsible for selecting what data to display in each ViewHolder.
     */
    private class LogListAdapter extends CursorRecyclerAdapter<LogListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.log_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder) {
            holder.userName.setText(getCursor().getString(0));
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(getCursor().getLong(1)*1000);
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d | k:mm"); // SimpleDateFormat mainly for machine readable formats
            DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
            holder.dateAndTime.setText(dateFormat.format(calendar.getTime()));
            if(getCursor().getInt(2) == 1) {
                holder.lockState.setImageResource(R.drawable.ic_lock);
            }
            else {
                holder.lockState.setImageResource(R.drawable.ic_lock_open);
            }
        }

        /**
         * Class containing the views contained within a row of the log list.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView userName;
            final TextView dateAndTime;
            final ImageView lockState;

            public ViewHolder(View itemView) {
                super(itemView);
                userName = (TextView) itemView.findViewById(R.id.user_name);
                dateAndTime = (TextView) itemView.findViewById(R.id.date_and_time);
                lockState = (ImageView) itemView.findViewById(R.id.lock_state);
            }
        }
    }
}
