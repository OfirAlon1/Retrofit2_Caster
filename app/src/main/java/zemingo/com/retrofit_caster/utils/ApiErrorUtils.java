package zemingo.com.retrofit_caster.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ApiErrorUtils
{

    public static <T> T parseError(Response<?> response, Retrofit retrofit, Type outputType)
    {
        Converter<ResponseBody, T> converter = retrofit.responseBodyConverter(outputType, new Annotation[0]);
        T error;

        try
        {
            error = converter.convert(response.errorBody());
        }
        catch (IOException e)
        {
            return null;
        }

        return error;
    }
}