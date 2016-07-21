package zemingo.com.retrofit_caster;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import zemingo.com.retrofit_caster.response.Chicago;
import zemingo.com.retrofit_caster.response.Result;
import zemingo.com.retrofit_caster.retrofit_objects.MapsRequestHandler;

public class MainActivity extends AppCompatActivity
{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapsRequestHandler.provideMapsService().getChicago().enqueue(new Callback<Result>()
        {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response)
            {
                Log.d(LOG_TAG, "onResponse(), response: " + response);
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t)
            {
                Log.d(LOG_TAG, "onResponse(), error!");
            }
        });

    }
}
