package com.app.dependency.ServiceApi;



import com.app.dependency.model.AddUserResponse;
import com.app.dependency.model.FaceSearch;
import com.app.dependency.model.ManualAddUser;
import com.app.dependency.model.SearchHeaderr;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {


    @POST("auth/searchWithEncodedImage")
    Call<FaceSearch> getresult(@Body SearchHeaderr searchHeader);


    @Multipart
    @POST("images/singleImage/")
    Call<AddUserResponse> saveNewUser(@Part MultipartBody.Part file,
                                      @Part("name") RequestBody name,
                                      @Part("user_id") RequestBody user_id);


    @Multipart
    @POST("images/singleImageForApproval")
    Call<ManualAddUser> addNewUser(@Part MultipartBody.Part file,
                                   @Part("user_id") RequestBody userId);

}
