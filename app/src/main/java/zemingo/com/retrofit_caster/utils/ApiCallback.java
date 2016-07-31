package zemingo.com.retrofit_caster.utils;



public interface ApiCallback<T, S>
{
    void onSuccess(T response);
    void onFailure(NetworkError<S> error);
}
