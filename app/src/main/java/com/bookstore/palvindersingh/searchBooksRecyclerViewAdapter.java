package com.bookstore.palvindersingh;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class searchBooksRecyclerViewAdapter extends RecyclerView.Adapter<searchBooksRecyclerViewAdapter.ViewHolder> {

    //initialise attributes
    private final ArrayList<book> books;
    private Context mContext;

    //boilerplate methods
    searchBooksRecyclerViewAdapter(Context mContext, ArrayList<book> books) {
        this.books = books;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public searchBooksRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanned_book_item, parent, false);
        return new searchBooksRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull searchBooksRecyclerViewAdapter.ViewHolder holder, final int position) {
        book book = books.get(position);

        //set book data
        holder.bookTitle.setText(book.getTitle());

        holder.bookStatus.setVisibility(View.GONE);

        //initialise on click listener
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book book = books.get(position);
                //go to view searched book activity
                Intent intent = new Intent(mContext, viewSearchBookActivity.class);
                intent.putExtra("book", book);
                mContext.startActivity(intent);
            }
        });
    }

    //method to return array size
    @Override
    public int getItemCount() {
        return books.size();
    }

    //boilerplate class to represent layout
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookTitle;
        CardView parentLayout;
        TextView bookStatus;

        ViewHolder(View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.bookTitle);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            bookStatus = itemView.findViewById(R.id.bookStatus);
        }
    }
}
