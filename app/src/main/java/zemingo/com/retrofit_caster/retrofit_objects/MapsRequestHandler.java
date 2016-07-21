package zemingo.com.retrofit_caster.retrofit_objects;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import zemingo.com.retrofit_caster.BuildConfig;
import zemingo.com.retrofit_caster.interfaces.MapsService;

public class MapsRequestHandler
{
    private static final String BASE_URL = "http://maps.googleapis.com";

    public static Retrofit provideRetrofit()
    {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(provideLoggingCapableHttpClient())
                .build();
    }

    public static MapsService provideMapsService()
    {
        return provideRetrofit().create(MapsService.class);
    }

    public static OkHttpClient provideLoggingCapableHttpClient()
    {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(/*BuildConfig.DEBUG ?*/ HttpLoggingInterceptor.Level.BODY /*: HttpLoggingInterceptor.Level.NONE*/);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }
}
