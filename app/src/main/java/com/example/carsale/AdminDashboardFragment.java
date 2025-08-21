package com.example.carsale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.example.carsale.Model.Payment;
import com.example.carsale.Model.Car;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.widget.TextView;
import java.util.*;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {
    private BarChart barChartDepositByMonth;
    private PieChart pieChartDepositByMake;
    private LineChart lineChartRevenueByMonth;
    private TextView tvTotalDeposits, tvTotalRevenue;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        barChartDepositByMonth = view.findViewById(R.id.barChartDepositByMonth);
        pieChartDepositByMake = view.findViewById(R.id.pieChartDepositByMake);
        lineChartRevenueByMonth = view.findViewById(R.id.lineChartRevenueByMonth);
        tvTotalDeposits = view.findViewById(R.id.tvTotalDeposits);
        tvTotalRevenue = view.findViewById(R.id.tvTotalRevenue);
        db = FirebaseFirestore.getInstance();
        loadDashboardData();
        return view;
    }

    private void loadDashboardData() {
        db.collection("payments")
                .whereEqualTo("confirmed", true)
                .get()
                .addOnSuccessListener(query -> {
                    Map<Integer, Integer> depositCountByMonth = new HashMap<>();
                    Map<String, Integer> depositCountByMake = new HashMap<>();
                    Map<Integer, Double> revenueByMonth = new HashMap<>();
                    Map<String, String> carMakeCache = new HashMap<>();

                    List<Payment> payments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Payment payment = doc.toObject(Payment.class);
                        payments.add(payment);
                    }

                    Set<String> carIds = new HashSet<>();
                    for (Payment payment : payments) {
                        carIds.add(payment.getCarId());
                    }
                    if (carIds.isEmpty()) {
                        showCharts(depositCountByMonth, depositCountByMake, revenueByMonth);
                        updateSummaryStats(payments);
                        return;
                    }

                    db.collection("cars")
                            .whereIn("id", new ArrayList<>(carIds))
                            .get()
                            .addOnSuccessListener(carQuery -> {
                                for (QueryDocumentSnapshot carDoc : carQuery) {
                                    Car car = carDoc.toObject(Car.class);
                                    carMakeCache.put(car.getId(), car.getMake());
                                }

                                Calendar cal = Calendar.getInstance();
                                for (Payment payment : payments) {
                                    cal.setTimeInMillis(payment.getTimestamp());
                                    int month = cal.get(Calendar.MONTH) + 1;
                                    depositCountByMonth.put(month, depositCountByMonth.getOrDefault(month, 0) + 1);
                                    revenueByMonth.put(month, revenueByMonth.getOrDefault(month, 0.0) + payment.getAmount());
                                    String make = carMakeCache.get(payment.getCarId());
                                    if (make != null) {
                                        depositCountByMake.put(make, depositCountByMake.getOrDefault(make, 0) + 1);
                                    }
                                }

                                showCharts(depositCountByMonth, depositCountByMake, revenueByMonth);
                                updateSummaryStats(payments);
                            });
                });
    }

    private void showCharts(Map<Integer, Integer> depositCountByMonth,
                            Map<String, Integer> depositCountByMake,
                            Map<Integer, Double> revenueByMonth) {
        // BarChart: Số lượng đặt cọc theo tháng
        List<BarEntry> barEntries = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            int count = depositCountByMonth.getOrDefault(i, 0);
            barEntries.add(new BarEntry(i, count));
        }
        BarDataSet barDataSet = new BarDataSet(barEntries, "Số lượng đặt cọc");
        barDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData barData = new BarData(barDataSet);
        barChartDepositByMonth.setData(barData);
        barChartDepositByMonth.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartDepositByMonth.getDescription().setText("Tháng");
        barChartDepositByMonth.invalidate();

        // PieChart: Tỷ lệ đặt cọc theo hãng xe
        List<PieEntry> pieEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : depositCountByMake.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Hãng xe");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData pieData = new PieData(pieDataSet);
        pieChartDepositByMake.setData(pieData);
        pieChartDepositByMake.setUsePercentValues(true);
        pieChartDepositByMake.getDescription().setText("Tỷ lệ đặt cọc theo hãng");
        pieChartDepositByMake.invalidate();

        // LineChart: Doanh thu đặt cọc theo tháng
        List<Entry> lineEntries = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            double revenue = revenueByMonth.getOrDefault(i, 0.0);
            lineEntries.add(new Entry(i, (float) revenue));
        }
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Doanh thu đặt cọc");
        lineDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        lineDataSet.setCircleColors(ColorTemplate.MATERIAL_COLORS);
        LineData lineData = new LineData(lineDataSet);
        lineChartRevenueByMonth.setData(lineData);
        lineChartRevenueByMonth.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChartRevenueByMonth.getDescription().setText("Tháng");
        lineChartRevenueByMonth.invalidate();
    }

    private void updateSummaryStats(List<Payment> payments) {
        // Tính tổng số đặt cọc
        int totalDeposits = payments.size();
        tvTotalDeposits.setText(String.valueOf(totalDeposits));

        // Tính tổng doanh thu
        double totalRevenue = 0.0;
        for (Payment payment : payments) {
            totalRevenue += payment.getAmount();
        }

        // Format số tiền theo định dạng Việt Nam
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedRevenue = formatter.format(totalRevenue);
        tvTotalRevenue.setText(formattedRevenue);
    }
} 