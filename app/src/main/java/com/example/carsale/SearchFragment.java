package com.example.carsale;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carsale.Adapter.CarAdapter;
import com.example.carsale.Database.CarHelper;
import com.example.carsale.Model.Car;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchFragment extends Fragment {
    
    // Search UI
    private EditText etSearch;
    private ImageView ivClearSearch;
    private LinearLayout ivFilter;
    private TextView tvFilterCount;
    
    // Filter chips
    private TextView chipAll, chipToyota, chipHonda, chipHyundai, chipPriceLow, chipAutomatic;
    
    // Results UI
    private RecyclerView rvSearchResults;
    private LinearLayout llResultsHeader, llEmptyState, llLoading, llRecentSearches, btnSort;
    private TextView tvResultsCount, btnClearAllFilters;
    
    // Bottom sheet filter
    private View filterBackdrop, bottomSheetFilter;
    private EditText etPriceMin, etPriceMax;
    private TextView tvSelectedBrand, tvSelectedYear, btnResetFilter, btnApplyFilter;
    private LinearLayout llBrandSelector, llYearSelector;
    
    // Car adapter and data
    private CarAdapter carAdapter;
    private final List<Car> searchResults = new ArrayList<>();
    private final List<Car> allCars = new ArrayList<>();
    
    // Search state
    private String searchQuery = "";
    private String selectedBrand = "";
    private String selectedYear = "";
    private double minPrice = 0;
    private double maxPrice = 0;
    private int activeFilters = 0;
    
    // Brand list - sẽ được load từ database
    private List<String> brands = new ArrayList<>();
    
    // Year list - sẽ được load từ database
    private List<String> years = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupListeners();
        loadData();
        setupRecyclerView();
    }

    private void initViews(View view) {
        // Search UI
        etSearch = view.findViewById(R.id.et_search);
        ivClearSearch = view.findViewById(R.id.iv_clear_search);
        ivFilter = view.findViewById(R.id.btn_filter);
        tvFilterCount = view.findViewById(R.id.tv_filter_count);
        
        // Filter chips
        chipAll = view.findViewById(R.id.chip_all);
        chipToyota = view.findViewById(R.id.chip_toyota);
        chipHonda = view.findViewById(R.id.chip_honda);
        chipHyundai = view.findViewById(R.id.chip_hyundai);
        chipPriceLow = view.findViewById(R.id.chip_price_low);
        chipAutomatic = view.findViewById(R.id.chip_automatic);
        
        // Results UI
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        llResultsHeader = view.findViewById(R.id.ll_results_header);
        llEmptyState = view.findViewById(R.id.ll_empty_state);
        llLoading = view.findViewById(R.id.ll_loading);
        llRecentSearches = view.findViewById(R.id.ll_recent_searches);
        
        tvResultsCount = view.findViewById(R.id.tv_results_count);
        btnSort = view.findViewById(R.id.btn_sort);
        btnClearAllFilters = view.findViewById(R.id.btn_clear_all_filters);
        
        // Bottom sheet filter
        filterBackdrop = view.findViewById(R.id.filter_backdrop);
        bottomSheetFilter = view.findViewById(R.id.bottom_sheet_filter);
        
        etPriceMin = view.findViewById(R.id.et_price_min);
        etPriceMax = view.findViewById(R.id.et_price_max);
        tvSelectedBrand = view.findViewById(R.id.tv_selected_brand);
        tvSelectedYear = view.findViewById(R.id.tv_selected_year);
        btnResetFilter = view.findViewById(R.id.btn_reset_filter);
        btnApplyFilter = view.findViewById(R.id.btn_apply_filter);
        
        llBrandSelector = view.findViewById(R.id.ll_brand_selector);
        llYearSelector = view.findViewById(R.id.ll_year_selector);
    }

    private void setupListeners() {
        // Search input listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim();
                ivClearSearch.setVisibility(searchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                performSearch();
            }
        });

        // Clear search button
        ivClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            searchQuery = "";
            performSearch();
        });

        // Filter button
        ivFilter.setOnClickListener(v -> showFilterBottomSheet());

        // Filter backdrop
        filterBackdrop.setOnClickListener(v -> hideFilterBottomSheet());

        // Filter chips
        chipAll.setOnClickListener(v -> selectChip(chipAll, "all"));
        chipToyota.setOnClickListener(v -> selectChip(chipToyota, "Toyota"));
        chipHonda.setOnClickListener(v -> selectChip(chipHonda, "Honda"));
        chipHyundai.setOnClickListener(v -> selectChip(chipHyundai, "Hyundai"));
        chipPriceLow.setOnClickListener(v -> selectChip(chipPriceLow, "price_low"));
        chipAutomatic.setOnClickListener(v -> selectChip(chipAutomatic, "automatic"));

        // Sort button
        btnSort.setOnClickListener(v -> showSortDialog());

        // Clear all filters
        btnClearAllFilters.setOnClickListener(v -> clearAllFilters());

        // Bottom sheet filter
        btnResetFilter.setOnClickListener(v -> resetFilters());
        btnApplyFilter.setOnClickListener(v -> applyFilters());

        // Brand selector
        llBrandSelector.setOnClickListener(v -> showBrandDialog());

        // Year selector
        llYearSelector.setOnClickListener(v -> showYearDialog());
    }

    private void setupRecyclerView() {
        carAdapter = new CarAdapter(requireContext(), searchResults, false, "", new CarAdapter.OnCarActionListener() {
            @Override
            public void onEdit(Car car) {
                // Handle edit car
            }

            @Override
            public void onDelete(Car car) {
                // Handle delete car
            }

            @Override
            public void onCarClick(Car car) {
                // Navigate to car detail
                if (getActivity() != null) {
                    // Intent to car detail activity
                }
            }
        });
        
        rvSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvSearchResults.setAdapter(carAdapter);
    }

    private void loadData() {
        showLoading(true);
        
        // Load brands từ database với fallback data
        CarHelper.getInstance().getAllBrands(new CarHelper.BrandsCallback() {
            @Override
            public void onSuccess(List<String> brandsList) {
                brands.clear();
                brands.addAll(brandsList);
                
                // Load years từ database
                CarHelper.getInstance().getAllYears(new CarHelper.YearsCallback() {
                    @Override
                    public void onSuccess(List<String> yearsList) {
                        years.clear();
                        years.addAll(yearsList);
                        
                        // Load all cars first, then filter client-side
                        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
                            @Override
                            public void onSuccess(List<Car> cars) {
                                allCars.clear();
                                // Filter only available cars client-side
                                for (Car car : cars) {
                                    if ("available".equalsIgnoreCase(car.getStatus())) {
                                        allCars.add(car);
                                    }
                                }
                                showLoading(false);
                                performSearch(); // Show initial results
                            }

                            @Override
                            public void onError(String error) {
                                // Fallback: load cars anyway
                                Toast.makeText(requireContext(), "Lỗi tải dữ liệu xe: " + error, Toast.LENGTH_SHORT).show();
                                showLoading(false);
                                performSearch(); // Show empty results
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        // Fallback: use default years
                        years.clear();
                        years.addAll(Arrays.asList("2024", "2023", "2022", "2021", "2020"));
                        
                        // Continue loading cars
                        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
                            @Override
                            public void onSuccess(List<Car> cars) {
                                allCars.clear();
                                for (Car car : cars) {
                                    if ("available".equalsIgnoreCase(car.getStatus())) {
                                        allCars.add(car);
                                    }
                                }
                                showLoading(false);
                                performSearch();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Lỗi tải dữ liệu xe: " + error, Toast.LENGTH_SHORT).show();
                                showLoading(false);
                                performSearch();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                // Fallback: use default brands
                brands.clear();
                brands.addAll(Arrays.asList("Toyota", "Honda", "Hyundai", "Ford", "Mazda"));
                
                // Continue loading years and cars
                CarHelper.getInstance().getAllYears(new CarHelper.YearsCallback() {
                    @Override
                    public void onSuccess(List<String> yearsList) {
                        years.clear();
                        years.addAll(yearsList);
                        
                        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
                            @Override
                            public void onSuccess(List<Car> cars) {
                                allCars.clear();
                                for (Car car : cars) {
                                    if ("available".equalsIgnoreCase(car.getStatus())) {
                                        allCars.add(car);
                                    }
                                }
                                showLoading(false);
                                performSearch();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Lỗi tải dữ liệu xe: " + error, Toast.LENGTH_SHORT).show();
                                showLoading(false);
                                performSearch();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        years.clear();
                        years.addAll(Arrays.asList("2024", "2023", "2022", "2021", "2020"));
                        
                        CarHelper.getInstance().getAllCars(new CarHelper.CarsListCallback() {
                            @Override
                            public void onSuccess(List<Car> cars) {
                                allCars.clear();
                                for (Car car : cars) {
                                    if ("available".equalsIgnoreCase(car.getStatus())) {
                                        allCars.add(car);
                                    }
                                }
                                showLoading(false);
                                performSearch();
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(requireContext(), "Lỗi tải dữ liệu xe: " + error, Toast.LENGTH_SHORT).show();
                                showLoading(false);
                                performSearch();
                            }
                        });
                    }
                });
            }
        });
    }

    private void selectChip(TextView chip, String filterType) {
        // Reset all chips
        resetChipSelection();
        
        // Select the clicked chip
        chip.setBackgroundResource(R.drawable.chip_selected_bg);
        chip.setTextColor(getResources().getColor(R.color.chip_text_selected));
        
        // Apply filter based on type
        switch (filterType) {
            case "Toyota":
                selectedBrand = "Toyota";
                break;
            case "Honda":
                selectedBrand = "Honda";
                break;
            case "Hyundai":
                selectedBrand = "Hyundai";
                break;
            case "price_low":
                maxPrice = 500000000; // 500 triệu
                break;
            case "automatic":
                // Filter for automatic transmission
                break;
        }
        
        updateFilterCount();
        performSearch();
    }

    private void resetChipSelection() {
        chipAll.setBackgroundResource(R.drawable.chip_normal_bg);
        chipAll.setTextColor(getResources().getColor(R.color.chip_text_normal));
        chipToyota.setBackgroundResource(R.drawable.chip_normal_bg);
        chipToyota.setTextColor(getResources().getColor(R.color.chip_text_normal));
        chipHonda.setBackgroundResource(R.drawable.chip_normal_bg);
        chipHonda.setTextColor(getResources().getColor(R.color.chip_text_normal));
        chipHyundai.setBackgroundResource(R.drawable.chip_normal_bg);
        chipHyundai.setTextColor(getResources().getColor(R.color.chip_text_normal));
        chipPriceLow.setBackgroundResource(R.drawable.chip_normal_bg);
        chipPriceLow.setTextColor(getResources().getColor(R.color.chip_text_normal));
        chipAutomatic.setBackgroundResource(R.drawable.chip_normal_bg);
        chipAutomatic.setTextColor(getResources().getColor(R.color.chip_text_normal));
    }

    private void updateFilterCount() {
        activeFilters = 0;
        if (!selectedBrand.isEmpty()) activeFilters++;
        if (!selectedYear.isEmpty()) activeFilters++;
        if (minPrice > 0 || maxPrice > 0) activeFilters++;
        
        if (activeFilters > 0) {
            tvFilterCount.setVisibility(View.VISIBLE);
            tvFilterCount.setText(String.valueOf(activeFilters));
        } else {
            tvFilterCount.setVisibility(View.GONE);
        }
    }

    private void performSearch() {
        showLoading(true);
        
        // Parse natural language query
        SearchCriteria criteria = parseNaturalLanguageQuery(searchQuery);
        
        // Apply filters
        List<Car> filteredCars = filterCars(criteria);
        
        // Update UI
        searchResults.clear();
        searchResults.addAll(filteredCars);
        carAdapter.notifyDataSetChanged();
        
        updateResultsUI(filteredCars.size());
        showLoading(false);
    }

    private SearchCriteria parseNaturalLanguageQuery(String query) {
        SearchCriteria criteria = new SearchCriteria();
        
        if (query.isEmpty()) {
            return criteria;
        }

        String lowerQuery = query.toLowerCase();
        
        // Parse brand
        for (String brand : brands) {
            if (lowerQuery.contains(brand.toLowerCase())) {
                criteria.brand = brand;
                break;
            }
        }

        // Parse price range
        if (lowerQuery.contains("dưới 200") || lowerQuery.contains("dưới 200 triệu")) {
            criteria.maxPrice = 200000000;
        } else if (lowerQuery.contains("dưới 500") || lowerQuery.contains("dưới 500 triệu")) {
            criteria.maxPrice = 500000000;
        } else if (lowerQuery.contains("dưới 800") || lowerQuery.contains("dưới 800 triệu")) {
            criteria.maxPrice = 800000000;
        } else if (lowerQuery.contains("dưới 1 tỷ")) {
            criteria.maxPrice = 1000000000;
        } else if (lowerQuery.contains("dưới 2 tỷ")) {
            criteria.maxPrice = 2000000000;
        }

        // Parse year range
        Pattern yearPattern = Pattern.compile("(\\d{4})");
        Matcher yearMatcher = yearPattern.matcher(query);
        if (yearMatcher.find()) {
            int year = Integer.parseInt(yearMatcher.group(1));
            criteria.minYear = year;
            criteria.maxYear = year + 3; // Assume range of 3 years
        }

        // Parse transmission
        if (lowerQuery.contains("số tự động") || lowerQuery.contains("tự động")) {
            criteria.transmission = "Số tự động";
        } else if (lowerQuery.contains("số sàn") || lowerQuery.contains("sàn")) {
            criteria.transmission = "Số sàn";
        }

        // Parse condition
        if (lowerQuery.contains("đã qua sử dụng") || lowerQuery.contains("cũ")) {
            criteria.condition = "Đã qua sử dụng";
        } else if (lowerQuery.contains("mới")) {
            criteria.condition = "Mới";
        }

        return criteria;
    }

    private List<Car> filterCars(SearchCriteria criteria) {
        List<Car> filtered = new ArrayList<>();
        
        for (Car car : allCars) {
            if (matchesCriteria(car, criteria)) {
                filtered.add(car);
            }
        }
        
        return filtered;
    }

    private boolean matchesCriteria(Car car, SearchCriteria criteria) {
        // Brand filter
        if (!criteria.brand.isEmpty() && !car.getMake().equals(criteria.brand)) {
            return false;
        }

        // Price filter
        if (criteria.maxPrice > 0 && car.getPrice() > criteria.maxPrice) {
            return false;
        }

        // Year filter
        if (criteria.minYear > 0 && car.getYear() < criteria.minYear) {
            return false;
        }
        if (criteria.maxYear > 0 && car.getYear() > criteria.maxYear) {
            return false;
        }

        // Transmission filter
        if (!criteria.transmission.isEmpty() && !car.getTransmission().equals(criteria.transmission)) {
            return false;
        }

        // Condition filter
        if (!criteria.condition.isEmpty() && !car.getCondition().equals(criteria.condition)) {
            return false;
        }

        return true;
    }

    private void updateResultsUI(int resultCount) {
        if (resultCount > 0) {
            llResultsHeader.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            llRecentSearches.setVisibility(View.GONE);
            tvResultsCount.setText(resultCount + " kết quả");
        } else {
            llResultsHeader.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            llRecentSearches.setVisibility(View.VISIBLE);
            btnClearAllFilters.setVisibility(activeFilters > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void showLoading(boolean show) {
        llLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            llResultsHeader.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.GONE);
            llRecentSearches.setVisibility(View.GONE);
        }
    }

    private void showFilterBottomSheet() {
        filterBackdrop.setVisibility(View.VISIBLE);
        bottomSheetFilter.setVisibility(View.VISIBLE);
        
        Animation slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up);
        bottomSheetFilter.startAnimation(slideUp);
    }

    private void hideFilterBottomSheet() {
        Animation slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                filterBackdrop.setVisibility(View.GONE);
                bottomSheetFilter.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        bottomSheetFilter.startAnimation(slideDown);
    }

    private void showBrandDialog() {
        if (brands.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có dữ liệu hãng xe", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] brandArray = brands.toArray(new String[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn hãng xe")
                .setItems(brandArray, (dialog, which) -> {
                    selectedBrand = brandArray[which];
                    tvSelectedBrand.setText(selectedBrand);
                    tvSelectedBrand.setTextColor(getResources().getColor(R.color.text_primary));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showYearDialog() {
        if (years.isEmpty()) {
            Toast.makeText(requireContext(), "Chưa có dữ liệu năm sản xuất", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] yearArray = years.toArray(new String[0]);
        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn năm sản xuất")
                .setItems(yearArray, (dialog, which) -> {
                    selectedYear = yearArray[which];
                    tvSelectedYear.setText(selectedYear);
                    tvSelectedYear.setTextColor(getResources().getColor(R.color.text_primary));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSortDialog() {
        // TODO: Implement sort dialog
        Toast.makeText(requireContext(), "Tính năng sắp xếp sẽ được cập nhật", Toast.LENGTH_SHORT).show();
    }

    private void clearAllFilters() {
        resetChipSelection();
        selectedBrand = "";
        selectedYear = "";
        minPrice = 0;
        maxPrice = 0;
        activeFilters = 0;
        updateFilterCount();
        performSearch();
    }

    private void resetFilters() {
        etPriceMin.setText("");
        etPriceMax.setText("");
        tvSelectedBrand.setText("Chọn hãng xe");
        tvSelectedYear.setText("Chọn năm sản xuất");
    }

    private void applyFilters() {
        // Get price range
        String minPriceStr = etPriceMin.getText().toString();
        String maxPriceStr = etPriceMax.getText().toString();
        
        if (!minPriceStr.isEmpty()) {
            minPrice = Double.parseDouble(minPriceStr) * 1000000; // Convert to VND
        }
        if (!maxPriceStr.isEmpty()) {
            maxPrice = Double.parseDouble(maxPriceStr) * 1000000; // Convert to VND
        }
        
        // selectedBrand và selectedYear đã được set từ dialog
        
        updateFilterCount();
        performSearch();
        hideFilterBottomSheet();
    }

    // Helper class for search criteria
    private static class SearchCriteria {
        String brand = "";
        int maxPrice = 0;
        int minYear = 0;
        int maxYear = 0;
        String transmission = "";
        String condition = "";
    }
} 