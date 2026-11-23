package ido.dotan.physicspendelumido;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private HistoryAdapter adapter;
    private List<HelperDB.CalculationItem> calculationsList = new ArrayList<>();
    private HelperDB helperDB;
    private String currentUserCode;

    public HistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvHistory = view.findViewById(R.id.rvHistory);
        helperDB = new HelperDB(getContext());

        SharedPreferences userPrefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        currentUserCode = userPrefs.getString("user_code", "");

        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryAdapter();
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        if (currentUserCode.isEmpty()) return;

        helperDB.getUserCalculations(currentUserCode, calculations -> {
            calculationsList = calculations;
            adapter.notifyDataSetChanged();
            if (calculationsList.isEmpty()) {
                Toast.makeText(getContext(), "No history found for this user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.calculation_item, parent, false);
            return new HistoryViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            HelperDB.CalculationItem item = calculationsList.get(position);
            
            holder.tvDate.setText("Date: " + item.date);
            holder.tvL.setText(String.format(Locale.US, "L: %.2f m", item.l));
            holder.tvG.setText(String.format(Locale.US, "g: %.2f m/s²", item.g));
            holder.tvT.setText(String.format(Locale.US, "T: %.2f s", item.t));

            holder.btnDelete.setOnClickListener(v -> {
                helperDB.deleteCalculation(item.id, success -> {
                    if (success) {
                        loadHistory();
                        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return calculationsList.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate, tvL, tvG, tvT;
            Button btnDelete;

            HistoryViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tvCalcDate);
                tvL = itemView.findViewById(R.id.tvCalcL);
                tvG = itemView.findViewById(R.id.tvCalcG);
                tvT = itemView.findViewById(R.id.tvCalcT);
                btnDelete = itemView.findViewById(R.id.btnDeleteCalc);
            }
        }
    }
}
