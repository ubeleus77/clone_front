package com.trade_accounting.services.interfaces.api;

import com.trade_accounting.models.dto.EmployeeDto;
import com.trade_accounting.models.dto.PageDto;
import com.trade_accounting.models.dto.ProductDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

import java.util.List;
import java.util.Map;

public interface ProductApi {

    @Headers("Accept: application/json")
    @GET("{url}")
    Call<List<ProductDto>> getAll(@Path(value = "url", encoded = true) String url);

    @Headers("Accept: application/json")
    @GET("{url}/{id}")
    Call <ProductDto> getById(@Path(value = "url", encoded = true) String url, @Path(value="id") Long id);

    @Headers("Accept: application/json")
    @GET("{url}/pages")
    Call<PageDto<ProductDto>> getPage(@Path(value = "url", encoded = true) String url,
                                       @QueryMap Map<String, String> query,
                                       @QueryMap Map<String, String> sortParams,
                                       @Query("pageNumber") Integer pageNumber,
                                       @Query("rowsLimit") Integer rowsLimit);

    @Headers("Accept: application/json")
    @POST("{url}")
    Call <Void> create(@Path(value = "url", encoded = true) String url, @Body ProductDto productDto);

    @Headers("Accept: application/json")
    @PUT("{url}")
    Call<Void> update(@Path(value = "url", encoded = true) String url, @Body ProductDto productDto);

    @Headers("Accept: application/json")
    @DELETE("{url}/{id}")
    Call <Void> deleteById(@Path(value = "url", encoded = true) String url, @Path(value="id") Long id);

    @Headers("Accept: application/json")
    @GET("{url}/pg/{id}")
    Call<List<ProductDto>> getAllByProductGroupId(@Path(value = "url", encoded = true) String url, @Path(value="id") Long id);

    @Headers("Accept: application/json")
    @GET("{url}/search")
    Call<List<ProductDto>> search(@Path(value = "url", encoded = true) String url, @Query("query") String query);

    @Headers("Accept: application/json")
    @GET("{url}/searchByFilter")
    Call<List<ProductDto>> searchByFilter(@Path(value = "url", encoded = true) String productUrl, @QueryMap Map<String, String> query);
}
