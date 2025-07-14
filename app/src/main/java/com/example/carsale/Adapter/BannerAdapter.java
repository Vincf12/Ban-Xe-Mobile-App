package com.example.carsale.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.R;

import java.util.Arrays;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    
    private final Context context;
    private final List<Integer> bannerImages;
    private final List<String> bannerTitles;
    private final List<String> bannerSubtitles;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(int position);
    }

    public BannerAdapter(Context context, List<Integer> bannerImages) {
        this.context = context;
        this.bannerImages = bannerImages;
        
        // Khởi tạo text cho banner
        this.bannerTitles = Arrays.asList(
            "Khuyến mãi đặc biệt",
            "Xe mới về",
            "Dịch vụ bảo hành",
            "Tư vấn miễn phí",
            "Đào Lửa Auto",
            "Chất lượng hàng đầu"
        );
        
        this.bannerSubtitles = Arrays.asList(
            "Giảm giá lên đến 20% cho xe mới",
            "Những mẫu xe mới nhất 2024",
            "Bảo hành chính hãng 5 năm",
            "Đội ngũ chuyên gia tư vấn 24/7",
            "Đại lý xe uy tín số 1",
            "Cam kết chất lượng 100%"
        );
    }

    public void setOnBannerClickListener(OnBannerClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        int actualPosition = position % bannerImages.size();
        int imageRes = bannerImages.get(actualPosition);
        String title = bannerTitles.get(actualPosition);
        String subtitle = bannerSubtitles.get(actualPosition);
        
        holder.bannerImage.setImageResource(imageRes);
        holder.bannerTitle.setText(title);
        holder.bannerSubtitle.setText(subtitle);
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(actualPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng lớn để tạo hiệu ứng vô hạn
        return Integer.MAX_VALUE;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView bannerImage;
        TextView bannerTitle;
        TextView bannerSubtitle;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            bannerImage = itemView.findViewById(R.id.banner_image);
            bannerTitle = itemView.findViewById(R.id.banner_title);
            bannerSubtitle = itemView.findViewById(R.id.banner_subtitle);
        }
    }
} 