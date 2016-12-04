package com.salatart.memeticame.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.salatart.memeticame.Activities.MemesActivity;
import com.salatart.memeticame.Listeners.OnRequestIndexListener;
import com.salatart.memeticame.Listeners.OnRequestShowListener;
import com.salatart.memeticame.Listeners.OnSearchClickListener;
import com.salatart.memeticame.Models.Meme;
import com.salatart.memeticame.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by sasalatart on 10/31/16.
 */

public class MemeUtils {
    public static String getNameFromUrl(String url) {
        int start = url.lastIndexOf("/") + 1;
        int end = url.lastIndexOf("?");
        return start < end ? url.substring(start, end) : url.substring(start);
    }

    public static String createName(String baseName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        baseName = baseName.replaceAll("[^A-Za-z0-9 ]", "");
        return baseName.replace(' ', '_') + Meme.SEPARATOR + timestamp + ".jpg";
    }

    public static String cleanName(String fullName) {
        int indexOfSeparator = fullName.indexOf(Meme.SEPARATOR);
        return fullName.replace('_', ' ').substring(0, indexOfSeparator);
    }

    public static void replaceMeme(ArrayList<Meme> memes, Meme newMeme) {
        for (Meme meme : memes) {
            if (meme.getId() == newMeme.getId()) {
                memes.set(memes.indexOf(meme), newMeme);
                return;
            }
        }
    }

    public static void createRequest(Request request, final OnRequestShowListener<Meme> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.memeFromJson(new JSONObject(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }

    public static void rateRequest(Request request, final OnRequestShowListener<Meme> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.memeFromJson(new JSONObject(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }

    public static void myRatingRequest(Request request, final OnRequestShowListener<Float> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(Float.parseFloat(new JSONObject(response.body().string()).get("value").toString()));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }

    public static void searchRequest(Request request, final OnRequestIndexListener<Meme> listener) {
        HttpClient.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                listener.OnFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        listener.OnSuccess(ParserUtils.memesFromJsonArray(new JSONArray(response.body().string())));
                    } catch (JSONException e) {
                        listener.OnFailure(e.toString());
                    }
                } else {
                    listener.OnFailure(HttpClient.parseErrorMessage(response));
                }

                response.body().close();
            }
        });
    }

    public static void onSearchClick(final Activity activity) {
        MemeUtils.processSearch(activity, new OnSearchClickListener() {
            @Override
            public void OnSearchRequested(String name, ArrayList<String> searchTags) {
                final ProgressDialog progressDialog = ProgressDialog.show(activity, "Please wait", "Searching for memes...", true);
                Request request = Routes.memesSearch(activity, name, searchTags);
                MemeUtils.searchRequest(request, new OnRequestIndexListener<Meme>() {
                    @Override
                    public void OnSuccess(ArrayList<Meme> memes) {
                        activity.startActivity(MemesActivity.getIntent(activity, memes, "Search results"));
                        progressDialog.dismiss();
                        activity.finish();
                    }

                    @Override
                    public void OnFailure(String message) {
                        CallbackUtils.onUnsuccessfulRequest(activity, message);
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    public static void processSearch(final Context context, final OnSearchClickListener listener) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View promptView = layoutInflater.inflate(R.layout.prompt_search_memes, null);

        final EditText tagsInput = (EditText) promptView.findViewById(R.id.tags_edit_search);
        final EditText nameInput = (EditText) promptView.findViewById(R.id.meme_name_search);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptView);
        alertDialogBuilder.setTitle("Search for memes");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nameSearch = nameInput.getText().toString();
                String tagsSearch = tagsInput.getText().toString();

                if (tagsSearch.isEmpty() && nameSearch.isEmpty()) {
                    Toast.makeText(context, "You must insert at least a name or a tag.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<String> searchTags = new ArrayList<String>(Arrays.asList(tagsSearch.split(" ")));
                listener.OnSearchRequested(nameSearch, searchTags);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static Point getBitmapScaledDimensions(View view, Bitmap bitmap) {
        int maxSize = view.getHeight();

        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();

        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return new Point(outWidth, outHeight);
    }
}
