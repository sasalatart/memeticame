package cl.mecolab.memeticame.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import cl.mecolab.memeticame.Models.Chat;
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
    private OnChatSelected mListener;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsListView = (ListView)view.findViewById(R.id.chatsListView);
        mChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.OnChatSelected(mChats.get(position));
            }
        });

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
            public void onResponse(Call call, final Response response) throws IOException {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mChats = Chat.fromJsonArray(new JSONArray(response.body().string()));
                            mAdapter = new ChatsAdapter(getContext(), R.layout.contact_list_item, mChats);
                            mChatsListView.setAdapter(mAdapter);
                        } catch (JSONException | IOException e) {
                            Log.e("ERROR", e.toString());
                        }
                    }
                });
            }
        });
    }

    public ArrayList<Chat> getChats() {
        return mChats;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnChatSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onChatSelected");
        }

    }

    public interface OnChatSelected {
        void OnChatSelected(Chat chat);
    }
}
