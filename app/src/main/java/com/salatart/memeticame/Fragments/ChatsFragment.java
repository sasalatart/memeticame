package com.salatart.memeticame.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

import com.salatart.memeticame.Activities.ParticipantsActivity;
import com.salatart.memeticame.Models.Chat;
import com.salatart.memeticame.R;
import com.salatart.memeticame.Utils.HttpClient;
import com.salatart.memeticame.Utils.ParserUtils;
import com.salatart.memeticame.Utils.Routes;
import com.salatart.memeticame.Views.ChatsAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {
    public static final String NEW_CHAT_FILTER = "newChatFilter";

    private ArrayList<Chat> mChats;
    private ChatsAdapter mAdapter;
    private ListView mChatsListView;
    private OnChatSelected mChatSelectedListener;
    private Routes.OnLogout mOnLogoutListener;
    private ChatsFragment.OnCreateGroupClicked mOnCreateGroupClickedListener;
    private BroadcastReceiver mChatsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Chat chat = intent.getParcelableExtra(Chat.PARCELABLE_KEY);
            mChats.add(chat);
            mAdapter.notifyDataSetChanged();
        }
    };
    private BroadcastReceiver mUsersKickedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int userId = intent.getIntExtra("user_id", 0);
            int chatId = intent.getIntExtra("chat_id", 0);

            for (Chat chat : mChats) {
                if (chat.onUserRemoved(getActivity(), chatId, userId)) {
                    mAdapter.notifyDataSetChanged();
                    break;
                }
            }
        }
    };

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mChatsReceiver, new IntentFilter(NEW_CHAT_FILTER));
        getActivity().registerReceiver(mUsersKickedReceiver, new IntentFilter(ParticipantsActivity.USER_KICKED_FILTER));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mChatsReceiver);
        getActivity().unregisterReceiver(mUsersKickedReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsListView = (ListView) view.findViewById(R.id.list_view_chats);
        mChatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mChatSelectedListener.OnChatSelected(mChats.get(position));
            }
        });
        mChatsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onChatLongClick(mChats.get(position));
                return true;
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
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mOnLogoutListener.OnLogout();
        } else if (id == R.id.action_create_group_chat) {
            mOnCreateGroupClickedListener.OnCreateGroupClicked();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showChats() {
        Request request = Routes.chatsIndexRequest(getActivity());
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERROR", e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    mChats = ParserUtils.chatsFromJsonArray(new JSONArray(response.body().string()));
                    mAdapter = new ChatsAdapter(getContext(), R.layout.list_item_contact, mChats);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mChatsListView.setAdapter(mAdapter);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("ERROR", e.toString());
                } finally {
                    response.body().close();
                }
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
            mChatSelectedListener = (OnChatSelected) context;
            mOnLogoutListener = (Routes.OnLogout) context;
            mOnCreateGroupClickedListener = (OnCreateGroupClicked) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onChatSelected");
        }

    }

    public void onChatLongClick(final Chat chat) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Leave chat")
                .setMessage("Do you really want to leave this chat?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Request request = Routes.chatLeaveRequest(getActivity(), chat.getId());
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
                                        if (response.isSuccessful()) {
                                            mChats.remove(chat);
                                            mAdapter.notifyDataSetChanged();
                                            Toast.makeText(getActivity(), "You have left the chat", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity(), HttpClient.parseErrorMessage(response), Toast.LENGTH_SHORT).show();
                                        }
                                        response.body().close();
                                    }
                                });
                            }
                        });
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public interface OnChatSelected {
        void OnChatSelected(Chat chat);
    }

    public interface OnCreateGroupClicked {
        void OnCreateGroupClicked();
    }
}
