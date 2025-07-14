package com.example.carsale.API;

import com.example.carsale.Model.BaseResponse;
import com.example.carsale.Model.Province;
import com.example.carsale.Model.District;
import com.example.carsale.Model.Ward;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ProvinceApiService {
    // Lấy tất cả hoặc tìm kiếm tỉnh/thành phố (có phân trang)
    @GET("location/provinces")
    Call<BaseResponse<Province>> getProvinces(
            @Query("page") int page,
            @Query("size") int size,
            @Query("query") String query
    );

    // Lấy danh sách quận/huyện theo tỉnh
    @GET("location/districts")
    Call<BaseResponse<District>> getDistricts(@Query("provinceId") String provinceId);

    // Lấy danh sách phường/xã theo quận/huyện
    @GET("location/wards")
    Call<BaseResponse<Ward>> getWards(@Query("districtId") String districtId);
}