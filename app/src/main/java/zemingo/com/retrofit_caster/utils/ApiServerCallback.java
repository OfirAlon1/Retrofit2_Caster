package zemingo.com.retrofit_caster.utils;

import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


// This calss have to be abstract in order the getErrorType method to work !!!
public abstract class ApiServerCallback<T, S> implements Callback<T>
{
    private static final String LOG_TAG = ApiServerCallback.class.getSimpleName();
    private ApiCallback<T, S> mCallback;
    private Retrofit mRetrofit;

    public ApiServerCallback(ApiCallback<T, S> callback, Retrofit retrofit)
    {
        mCallback = callback;
        mRetrofit = retrofit;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response)
    {
        if (response.isSuccessful())
        {
            mCallback.onSuccess(response.body());
        }
        else
        {
            Type type = getErrorType();
            if (type != null)
            {
                NetworkError<S> generalError;
                try
                {
                    S error = ApiErrorUtils.parseError(response, mRetrofit, type);
                    generalError = new NetworkError<>(error);
                }
                catch (Exception e)
                {
                    Log.e(LOG_TAG, "Failed parsing error"  , e);
                    generalError = new NetworkError<S>(null);
                }
                mCallback.onFailure(generalError);
            }
            else
            {
                mCallback.onFailure(new NetworkError<S>(null));
            }
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t)
    {
        mCallback.onFailure(new NetworkError<S>(null));
    }

    private Type getErrorType()
    {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        if (parameterizedType.getActualTypeArguments().length > 1)
        {
            return parameterizedType.getActualTypeArguments()[1];
        }
        return null;
    }

}