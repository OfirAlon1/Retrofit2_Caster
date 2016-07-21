package zemingo.com.retrofit_caster.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import zemingo.com.retrofit_caster.response.Chicago;
import zemingo.com.retrofit_caster.response.Result;
import zemingo.com.retrofit_caster.retrofit_objects.Book;

public interface MapsService
{
    @GET("/maps/api/geocode/json?address=Chicago")
    Call<Result> getChicago();

    //    optional query parameter
    @GET("books")
    Call<List<Book>> getBook(@Query("q") String query); //www.../books?q=query

    //    with path element
    @GET("books/{id}")
    Call<Book> getBook2(@Path("id") Long id); //www.../books/3

    //post request. based on the convector we supplies in provideRetrofit : .addConverterFactory(GsonConverterFactory.create())
    @POST("books")
    Call<Book> getBook3(@Body Book book);

    //or we can do:
    @POST("books")
    @FormUrlEncoded
    Call<Book> getBook4(@Field("title") String title,
                        @Field("autor") String author,
                        @Field("description") String description  );


}
