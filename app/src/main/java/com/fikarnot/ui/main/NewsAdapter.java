package com.fikarnot.ui.main;

import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fikarnot.R;

import java.util.List;


class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<NewsModel> newsList;
    private NewsFragment.SelectedNews selectedNews;

    public NewsAdapter(List<NewsModel> newsList, NewsFragment.SelectedNews selectedNews) {
        this.newsList = newsList;
        this.selectedNews = selectedNews;
    }

    @NonNull
    @Override
    public NewsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_news,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NewsModel news = newsList.get(position);
        holder.title_tv.setText(news.getTitle());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            holder.description_tv.setText(Html.fromHtml(news.getDescription(),Html.FROM_HTML_MODE_LEGACY));
        }else {
            holder.description_tv.setText(Html.fromHtml(news.getDescription()));
        }
        holder.title_tv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                selectedNews.selectedNews(news.getNewslink());
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title_tv;
        TextView description_tv;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title_tv = itemView.findViewById(R.id.title);
            description_tv = itemView.findViewById(R.id.description);
        }
    }
}
