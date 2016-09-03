package cl.mecolab.memeticame.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import cl.mecolab.memeticame.Models.Chat;
import cl.mecolab.memeticame.Models.User;
import cl.mecolab.memeticame.R;
import cl.mecolab.memeticame.Utils.HttpClient;
import cl.mecolab.memeticame.Utils.Routes;
import cl.mecolab.memeticame.Views.ChatsAdapter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private ArrayList<Chat> mChats;
    private ChatsAdapter mAdapter;
    private ListView mChatsListView;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsListView = (ListView)view.findViewById(R.id.chatsListView);

        showChats();

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chats_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    public void showChats() {
        Request request = Routes.buildConersationsIndexRequest(getActivity());
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    mChats = Chat.fromJsonArray(new JSONArray(response.body().string()));
                    mAdapter = new ChatsAdapter(getContext(), R.layout.contact_list_item, mChats);
                    mChatsListView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                }
            }
        });
    }

    public ArrayList<Chat> getChats() {
        return mChats;
    }
}
