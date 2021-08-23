package com.fikarnot.ui.main;


import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fikarnot.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProductsFragment extends Fragment {

    private List<ProductsModel> productsModelList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ProductsAdapter adapter;
    private View view;
    private LinearLayout loading, clear_scren2;
    private int status = 0;

    public ProductsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_products, container, false);
        recyclerView = view.findViewById(R.id.recyclerView2);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,false));
        loading = view.findViewById(R.id.loading2);
        clear_scren2 = view.findViewById(R.id.clear_screen2);


        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference root = db.getReference().child("SALE");
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String title = dataSnapshot.getKey();
                        String description = String.valueOf(dataSnapshot.child("description").getValue());
                        String imageUrl = String.valueOf(dataSnapshot.child("image").getValue());
                        String link = String.valueOf(dataSnapshot.child("link").getValue());
                        String price = String.valueOf(dataSnapshot.child("price").getValue());
                        ProductsModel new_product = new ProductsModel(imageUrl, title, description, link, price);
                        productsModelList.add(new_product);
                    }

                    adapter = new ProductsAdapter(productsModelList, new ProductsAdapter.SelectedProduct() {
                        @Override
                        public void selectedProduct(ProductsModel model) {
                            Dialog myDialog = new Dialog(getContext());
                            myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            myDialog.setContentView(R.layout.view_description_dialog);
                            myDialog.setCancelable(false);
                            myDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.round_corners));
                            myDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            TextView description = myDialog.findViewById(R.id.description);
                            description.setText(model.getDescription());
                            ImageView dismiss = myDialog.findViewById(R.id.dismiss1);
                            dismiss.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    myDialog.dismiss();
                                }
                            });
                            Button open = myDialog.findViewById(R.id.open);
                            open.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(model.link)));
                                }
                            });
                            Button share = myDialog.findViewById(R.id.share);
                            share.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    startActivity(Intent.createChooser(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,model.link),"Share via"));
                                }
                            });
                            myDialog.show();
                        }

                        @Override
                        public void selectedProductLongClick(ProductsModel model) {

                        }
                    });
                    recyclerView.setAdapter(adapter);
                    clear_scren2.setVisibility(View.GONE);
                }else{
                    clear_scren2.setVisibility(View.VISIBLE);
                }
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }



    //Class BackgroundTask STARTS
    class BackgroundTask extends AsyncTask<String, Integer, Boolean> {


        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean result=true;



            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {


        }
    }
    //Class BackgroundTask ENDS



    //Class Product Model STARTS
    private class ProductsModel {
        private String imageUrl;
        private String title;
        private String description;
        private String link;
        private String price;

        public ProductsModel(String imageUrl, String title, String description, String link, String price) {
            this.imageUrl = imageUrl;
            this.title = title;
            this.description = description;
            this.link = link;
            this.price = price;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String imageUrl) {
            this.price = price;
        }
    }
    //Class Product Model ENDS



    //Class ProductAdapter Model STARTS
    private static class ProductsAdapter extends RecyclerView.Adapter<ProductsAdapter.ViewHolder> {

        private List<ProductsModel> products_list;
        private SelectedProduct selectedProduct;

        public ProductsAdapter(List<ProductsModel> products_list, SelectedProduct selectedProduct) {
            this.products_list = products_list;
            this.selectedProduct = selectedProduct;
        }

        @NonNull
        @Override
        public ProductsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_layout,parent,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductsAdapter.ViewHolder holder, int position) {
            ProductsModel product = products_list.get(position);
            String rate = "Rs. " + product.getPrice();
            holder.title.setText(product.getTitle());
            holder.price.setText(rate);
            Picasso.get().load(product.getImageUrl()).into(holder.imageview);
        }

        @Override
        public int getItemCount() {
            return products_list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageview;
            TextView title;
            TextView price;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageview = itemView.findViewById(R.id.product_image);
                title = itemView.findViewById(R.id.product_title);
                price = itemView.findViewById(R.id.price);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedProduct.selectedProduct(products_list.get(getAbsoluteAdapterPosition()));
                    }
                });
            }
        }


        //Interface SelectedProduct STARTS
        public interface SelectedProduct {
            void selectedProduct(ProductsModel model);
            void selectedProductLongClick(ProductsModel model);
        }
        //Interface SelectedProduct ENDS

    }
    //Class ProductAdapter Model ENDS

}
