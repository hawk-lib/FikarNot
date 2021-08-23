package com.fikarnot.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fikarnot.R;

import java.util.ArrayList;
import java.util.List;

public class AccountsRecyclerAdapter extends RecyclerView.Adapter<AccountsRecyclerAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<AccountsModel> accountsList;
    private List<AccountsModel> getAccountsListFiltered;
    private SelectedAccount selectedAccount;

    public AccountsRecyclerAdapter(Context context, List<AccountsModel> accountsModelList, SelectedAccount selectedAccount) {
        this.context = context;
        this.accountsList = accountsModelList;
        this.selectedAccount = selectedAccount;
        this.getAccountsListFiltered = accountsModelList;

    }

    @NonNull
    @Override
    public AccountsRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_accounts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountsRecyclerAdapter.ViewHolder holder, int position) {

        AccountsModel model = accountsList.get(position);

        String username = model.getUsername();
        String prefix = model.getTitle().substring(0,1);
        String title = model.getTitle();


        holder.titleTV.setText(title);
        holder.prefixTV.setText(prefix);
        holder.usernameTV.setText(username);

    }

    @Override
    public int getItemCount() {
        return accountsList.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filterResults = new FilterResults();
                if (charSequence == null || charSequence.length() == 0){
                    filterResults.count = getAccountsListFiltered.size();
                    filterResults.values = getAccountsListFiltered;
                } else {
                    String searchChr = charSequence.toString().toLowerCase();
                    List<AccountsModel> resultData = new ArrayList<>();
                    for (AccountsModel model : getAccountsListFiltered){
                        if (model.getTitle().toLowerCase().contains(searchChr)){
                            resultData.add(model);
                        }
                    }
                    filterResults.count = resultData.size();
                    filterResults.values = resultData;
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                accountsList = (List<AccountsModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public interface SelectedAccount {
        void selectedAccount(AccountsModel model);
        void selectedAccountLongClick(AccountsModel model);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView prefixTV;
        TextView usernameTV;
        TextView titleTV;
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            prefixTV = itemView.findViewById(R.id.prefix);
            usernameTV = itemView.findViewById(R.id.username);
            titleTV = itemView.findViewById(R.id.title);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    selectedAccount.selectedAccount(accountsList.get(getAbsoluteAdapterPosition()));
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    selectedAccount.selectedAccountLongClick(accountsList.get(getAbsoluteAdapterPosition()));
                    return false;
                }
            });
        }
    }
}
