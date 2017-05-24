package br.com.luisfelipeas5.networkdatalogger.adapter;

import android.net.TrafficStats;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import br.com.luisfelipeas5.networkdatalogger.databinding.LayoutApplicationAdapterItemBinding;
import br.com.luisfelipeas5.networkdatalogger.model.AppData;

public class AppDataAdapter extends RecyclerView.Adapter<AppDataAdapter.ViewHolder> {

    private List<AppData> mAppDataList;
    private long mTotalReceived;
    private long mTotalTransmitted;

    public AppDataAdapter(List<AppData> appDataList) {
        mAppDataList = appDataList;

        mTotalReceived = TrafficStats.getTotalRxBytes();
        mTotalTransmitted = TrafficStats.getTotalTxBytes();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutApplicationAdapterItemBinding adapterItemBinding
                = LayoutApplicationAdapterItemBinding.inflate(inflater, parent, false);
        return new ViewHolder(adapterItemBinding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppData appData = mAppDataList.get(position);
        holder.mBinding.setAppData(appData);

        int receivedPercent = getPercentage(appData.getReceived(), mTotalReceived);
        holder.mBinding.progressBarReceived.setProgress(receivedPercent);

        int transmittedPercent = getPercentage(appData.getTransmitted(), mTotalTransmitted);
        holder.mBinding.progressBarTransmitted.setProgress(transmittedPercent);
    }

    private int getPercentage(long numerator, long denominator) {
        return (int)(numerator * 100.0 / denominator + 0.5);
    }

    @Override
    public int getItemCount() {
        return mAppDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        LayoutApplicationAdapterItemBinding mBinding;

        ViewHolder(LayoutApplicationAdapterItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }
    }
}
