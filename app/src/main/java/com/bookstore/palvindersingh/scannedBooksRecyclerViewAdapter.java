package com.bookstore.palvindersingh;

import android.app.Activity;
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

public class scannedBooksRecyclerViewAdapter extends RecyclerView.Adapter<scannedBooksRecyclerViewAdapter.ViewHolder> {

    //initialise attributes
    private final ArrayList<book> books;
    private Context mContext;

    //boilerplate methods
    scannedBooksRecyclerViewAdapter(Context mContext, ArrayList<book> books) {
        this.books = books;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.scanned_book_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        book book = books.get(position);

        //check and output book state
        if (!book.getMarket()) {
            if (book.getRenting()) {
                holder.bookStatus.setText("on rent");
            } else {
                if (book.getTitle() != "add a book") {
                    holder.bookStatus.setText("off market");
                } else {
                    holder.bookStatus.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            holder.bookStatus.setText("on market");
        }

        //set title
        holder.bookTitle.setText(books.get(position).getTitle());
        //initialise on click listener
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book book = books.get(position);
                if (book.getTitle() == "add a book") {
                    //if no books added yet perform add book floating action button click
                    networkFAB btn = ((Activity) mContext).findViewById(R.id.floating_action_button);
                    btn.performClick();
                } else {
                    //go to view book class
                    Intent intent = new Intent(mContext, viewBookActivity.class);
                    intent.putExtra("book", book);
                    mContext.startActivity(intent);
                }
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
