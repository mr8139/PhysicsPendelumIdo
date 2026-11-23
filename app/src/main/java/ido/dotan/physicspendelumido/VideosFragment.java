package ido.dotan.physicspendelumido;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends Fragment {

    private EditText etVideoSearch;
    private Button btnVideoSearch, btnSaveVideo;
    private WebView wvVideo;
    private RecyclerView rvSavedVideos;
    private HelperDB helperDB;
    private VideoAdapter adapter;
    private List<HelperDB.VideoItem> savedVideosList = new ArrayList<>();

    public VideosFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_videos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helperDB = new HelperDB(getContext());
        etVideoSearch = view.findViewById(R.id.etVideoSearch);
        btnVideoSearch = view.findViewById(R.id.btnVideoSearch);
        btnSaveVideo = view.findViewById(R.id.btnSaveVideo);
        wvVideo = view.findViewById(R.id.wvVideo);
        rvSavedVideos = view.findViewById(R.id.rvSavedVideos);

        // WebView settings
        WebSettings webSettings = wvVideo.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvVideo.setWebViewClient(new WebViewClient());

        // RecyclerView settings
        rvSavedVideos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VideoAdapter();
        rvSavedVideos.setAdapter(adapter);

        checkInternetAndInit();

        btnVideoSearch.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                showNoInternetDialog();
                return;
            }
            String query = etVideoSearch.getText().toString().trim().toLowerCase();
            if (query.isEmpty()) return;

            if (query.contains("pendu") || query.contains("pende") || query.contains("מטוטלת")) {
                String url = "https://www.youtube.com/results?search_query=" + query;
                wvVideo.loadUrl(url);
            } else {
                Toast.makeText(getContext(), "Search is limited to pendulum topics!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSaveVideo.setOnClickListener(v -> {
            String currentUrl = wvVideo.getUrl();
            String currentTitle = wvVideo.getTitle();
            if (currentUrl != null && !currentUrl.isEmpty()) {
                helperDB.addVideo(currentTitle != null ? currentTitle : "Video", currentUrl, success -> {
                    if (success) {
                        Toast.makeText(getContext(), "Saved!", Toast.LENGTH_SHORT).show();
                        loadVideosFromDb();
                    }
                });
            }
        });
    }

    private void checkInternetAndInit() {
        if (isNetworkAvailable()) {
            loadVideosFromDb();
        } else {
            btnVideoSearch.setEnabled(false);
            rvSavedVideos.setVisibility(View.GONE);
            showNoInternetDialog();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Internet Connection")
                .setMessage("Internet connection is required to search and view videos. Please check your settings.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void loadVideosFromDb() {
        helperDB.getAllVideos(videos -> {
            savedVideosList = videos;
            adapter.notifyDataSetChanged();
        });
    }

    private class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
            return new VideoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
            HelperDB.VideoItem item = savedVideosList.get(position);
            holder.tvTitle.setText(item.title);
            holder.itemView.setOnClickListener(v -> {
                if (isNetworkAvailable()) {
                    wvVideo.loadUrl(item.url);
                } else {
                    showNoInternetDialog();
                }
            });
            holder.btnDelete.setOnClickListener(v -> {
                helperDB.deleteVideo(item.id, success -> {
                    if (success) loadVideosFromDb();
                });
            });
        }

        @Override
        public int getItemCount() {
            return savedVideosList.size();
        }

        class VideoViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            View btnDelete;
            VideoViewHolder(View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvVideoTitle);
                btnDelete = itemView.findViewById(R.id.btnDeleteVideo);
            }
        }
    }
}
