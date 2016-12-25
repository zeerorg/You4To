package com.example.rishabh.you4to;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rishabh on 12/23/16.
 */

public class MyRecyclerView {
    public static class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListHolder>{

        private LayoutInflater inflater;
        private List<ListItem> list;
        private List<ListHolder> views;

        public void setItemClickCallback(ItemClickCallback itemClickCallback) {
            this.itemClickCallback = itemClickCallback;
        }

        private ItemClickCallback itemClickCallback;

        public void setListData(List<ListItem> listData) {
            this.list = listData;
        }

        public interface ItemClickCallback{
            void onItemClick(int p);
        }

        public ListAdapter(List<ListItem> list, Context context){
            this.inflater = LayoutInflater.from(context);
            this.list = list;
            this.views = new ArrayList<>();
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.card_item, parent, false);
            ListHolder h = new ListHolder(view);
            this.views.add(h);
            return h;
        }

        @Override
        public void onBindViewHolder(ListHolder holder, int position){
            //Log.e("item", item.getText()+" : "+item.getImage()+" : "+position);
            ListItem item = list.get(position);
            holder.text.setText(item.getText());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public List<ListHolder> getViews(){
            return this.views;
        }

        class ListHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

            private TextView text;
            private View container;

            public ListHolder(View itemView) {
                super(itemView);

                text = (TextView) itemView.findViewById(R.id.card_text);

                container = itemView.findViewById(R.id.card_root);
                container.setOnClickListener(this);
            }

            public View getContainer(){
                return container;
            }

            @Override
            public void onClick(View view) {
                itemClickCallback.onItemClick(getAdapterPosition());
            }
        }

    }

    public static class ListData{

        public String[] text;

        public ListData(){
            text = new String[] {"Suicide Squad", "Jason Bourne", "Batman: The Killing Joke"};
        }
        public List<ListItem> getListData(){
            List<ListItem> data = new ArrayList<ListItem>();

            for(int y=0; y<text.length; y++){
                data.add(new ListItem(text[y]));
            }

            return data;

        }

        public void setListData(String[] text){
            this.text = text;
        }
    }

    public static class ListItem{

        private String text;

        public ListItem(String text){
            setText(text);
        }

        public ListItem(){}

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
