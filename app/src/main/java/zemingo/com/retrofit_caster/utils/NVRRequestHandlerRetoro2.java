package zemingo.com.retrofit_caster.utils;

import com.flir.nvrdvr.application.io.requests.RetroFitCreator;
import com.flir.nvrdvr.communication.errorhandling.NvrCommunicationError;
import com.flir.nvrdvr.communication.ozvisionserver.common.errors.GenericError;
import com.flir.nvrdvr.communication.ozvisionserver.listeners.RequestListener;
import com.flir.nvrdvr.communication.requests.NvrRequests.ActionRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.ChannelConfigRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.DeviceNotificationSettingsRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.GetDeviceNotificationSettingsRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.PrivacyModeRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.PtzCommandRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.SetChannelConfigRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.SetDeviceConfigRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.SetDeviceNameConfigRequest;
import com.flir.nvrdvr.communication.requests.NvrRequests.models.ChannelSettingsInformationRequest;
import com.flir.nvrdvr.communication.requests.RsNvrRequests.GetDeviceConfigurationRequest;
import com.flir.nvrdvr.communication.requests.RsNvrRequests.SetDeviceCredentialsRequest;
import com.flir.nvrdvr.communication.requests.RsNvrRequests.UserInfoRequest;
import com.flir.nvrdvr.communication.responses.fxResponses.RemoteControlResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.BasicGsonResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.ChannelConfigResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.CheckFirmwareResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.DayEventsListResponseBody;
import com.flir.nvrdvr.communication.responses.nvrResponses.DeviceConfigResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.GetMachineModelResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.RebootResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.SetConfigResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.UserManagerResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.deviceStatusResponse.DeviceStatusResponse;
import com.flir.nvrdvr.communication.responses.nvrResponses.shortPlaybacks.ShortPlaybackResponse;
import com.flir.nvrdvr.model.common.interfaces.Device;
import com.flir.nvrdvr.model.common.interfaces.DeviceModel;
import com.flir.nvrdvr.model.nvrModel.models.NvrChannelModel;
import com.flir.nvrdvr.ptz.PtzCommand;
import com.flir.nvrdvr.requests.networkRequests.DayEventsRequest;
import com.flir.nvrdvr.requests.networkRequests.PlaybackShortRequest;
import com.google.gson.Gson;
import com.zemingo.logrecorder.ZLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit.Callback;
import retrofit.Endpoint;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
//import retrofit.Endpoint;
//import retrofit.RestAdapter;
//import retrofit.RetrofitError;
//import retrofit.client.OkClient;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//import retrofit2.Retrofit;
//import retrofit2.converter.gson.GsonConverterFactory;
//import retrofit2.http.Body;
//import retrofit2.http.GET;
//import retrofit2.http.POST;

public class NVRRequestHandlerRetoro2
{
    private static final String LOG_TAG = NvrRequestsHandler.class.getSimpleName();
    private MyService mServiceRETRO2;
    private Retrofit mRetrofit = null;
    private MyService mService;

    public NVRRequestHandlerRetoro2(DeviceModel model, String deviceAddress)
    {
        NvrRequestInterceptor requestInterceptor = new NvrRequestInterceptor(model.getDeviceCredentials());

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(new nvrEndPoint(deviceAddress))
                .setRequestInterceptor(requestInterceptor)
                .setLogLevel(RetroFitCreator.getLogLevel()) //-
                .setLog(RetroFitCreator.getRetroLog()) //-
                .setErrorHandler(new RetroFitResponseErrorHandler()) //-
                .setClient(new OkClient(RetroFitCreator.createNewHttpClient())) //-
                .setConverter(new NvrConverter(new Gson()))
                .setExecutors(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()) //-
                .build();

        mService = restAdapter.create(MyService.class);

        mRetrofit = new Retrofit.Builder()
                .baseUrl(deviceAddress)
                .addConverterFactory(GsonConverterFactory.create())
                .client(provideLoggingCapableHttpClient(model))
                .callbackExecutor(ThreadManager.getNetworkExecutors())
                .build();

        mServiceRETRO2 = mRetrofit.create(MyService.class);
    }

    public OkHttpClient provideLoggingCapableHttpClient(DeviceModel model)
    {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new NVRRequestInterceptorRetro2(model.getDeviceCredentials()))
                .build();
    }

    class nvrEndPoint implements Endpoint
    {
        private String mUrl;

        public nvrEndPoint(String url)
        {
            super();
            mUrl = url;
        }

        public void setUrl(String url)
        {
            this.mUrl = url;
        }

        @Override
        public String getName()
        {
            return "default";
        }

        @Override
        public String getUrl()
        {
            if(mUrl == null)
            {
                throw new IllegalStateException("mUrl not set.");
            }
            return mUrl;
        }
    }

    //http://admin:123456@127.0.0.1:50655/cgi-bin/configManager.cgi?action=getConfig&
    public interface MyService
    {
        //no converter - checked
        @POST("/API/1.0/ptz")
        void sendPtzCommand(@Body PtzCommandRequest ptzCommandRequest, retrofit.Callback<BasicGsonResponse> callback);

        //no converter - checked
        @POST("/API/1.0/updateChannelSettings")
        void updateDeviceNotificationSettings(@Body DeviceNotificationSettingsRequest deviceNotificationSettingsRequest, retrofit.Callback<DeviceNotificationSettingsRequest> callback);

        //no converter - checked
        @POST("/API/1.0/getChannelSettings")
        void loadDeviceNotificationSettings(@Body GetDeviceNotificationSettingsRequest getDeviceNotificationSettingsRequest, retrofit.Callback<DeviceNotificationSettingsRequest> callback);

        //no converter
        @retrofit2.http.POST("/API/1.0/mediaFileFind")
        Call<ShortPlaybackResponse> getPlaybacksShortApi(@retrofit2.http.Body PlaybackShortRequest playbackRequest/*, Callback<ShortPlaybackResponse> callback*/);

        //no converter - checked
        @retrofit2.http.POST("/API/1.0/mediaFileFind")
        Call<DayEventsListResponseBody> getDayEvents(@retrofit2.http.Body DayEventsRequest dayEventsRequest/*, Callback<DayEventsListResponseBody> callback*/);

        //no converter - checked
        @retrofit2.http.POST("/API/1.0/userManager")
        Call<UserManagerResponse> getDevicePermissions(@retrofit2.http.Body UserInfoRequest userInfoRequest/*, Callback<UserManagerResponse> callback*/);

        //no converter - checked
        @POST("/API/1.0/userManager")
        void setDeviceCredentials(@Body SetDeviceCredentialsRequest setDeviceCredentialsRequest, retrofit.Callback<UserManagerResponse> callback);

        //no converter - checked
        @POST("/API/1.0/getConfig")
        void getDeviceConfiguration(@Body ActionRequest actionRequest, retrofit.Callback<DeviceConfigResponse> callback);

        //no converter - checked
        @POST("/API/1.0/getConfig")
        void getChannelConfiguration(@Body ChannelConfigRequest channelConfigRequest, retrofit.Callback<ChannelConfigResponse> callback);

        //no converter - checked
        @POST("/API/1.0/setConfig")
        void setChannelConfiguration(@Body SetChannelConfigRequest setChannelConfigRequest, retrofit.Callback<SetConfigResponse> callback);

        //no converter - checked
        @POST("/API/1.0/setConfig")
        void setDeviceConfiguration(@Body SetDeviceConfigRequest setDeviceConfigRequest, retrofit.Callback<SetConfigResponse> callback);

        //no converter - checked
        @POST("/API/1.0/setConfig")
        void setDeviceNameConfiguration(@Body SetDeviceNameConfigRequest setDeviceConfigRequest, retrofit.Callback<SetConfigResponse> callback);

        //no converter
        @POST("/API/1.0/upgradeFirmware")
        void upgradeFirmware(@Body ActionRequest actionRequest, retrofit.Callback<BasicGsonResponse> callback);

        //no converter - checked
        @POST("/API/1.0/deviceStatus")
        void getDeviceStatus(retrofit.Callback<DeviceStatusResponse> callback);

        //no converter - checked
        @POST("/API/1.0/remoteControl")
        void setPrivacyModeEnabled(@Body PrivacyModeRequest actionRequest, retrofit.Callback<RemoteControlResponse> callback);

        @retrofit2.http.GET("/API/1.0/checkNewFirmware")
        Call<CheckFirmwareResponse> checkNewFirmware(/*Callback<CheckFirmwareResponse> callback*/);

        @retrofit2.http.GET("/cgi-bin/configManager.cgi?action=getConfig&name=General.MachineName")
        Call<GetMachineModelResponse> getMachineModel(/*Callback<GetMachineModelResponse> callback*/);

        @retrofit2.http.GET("/cgi-bin/magicBox.cgi?action=reboot")
        Call<RebootResponse> rebootDevice();
    }

    public void rebootDevice(final ApiCallback<RebootResponse, ApiError> callback)
    {
        Call<RebootResponse> rebootDevice = mServiceRETRO2.rebootDevice();
        rebootDevice.enqueue(new ApiServerCallback<RebootResponse, ApiError>(callback, mRetrofit)
        {
        });

//        mService.rebootDevice(new Callback<RebootResponse>()
//        {
//            @Override
//            public void success(RebootResponse o, Response response)
//            {
//                listener.success(null);
//            }
//
//            @Override
//            public void failure(RetrofitError error)
//            {
//                listener.error(new NvrCommunicationError(error));
//            }
//        });
    }

    public void getMachineModel(final ApiCallback<GetMachineModelResponse, ApiError> callback)
    {
        Call<GetMachineModelResponse> getMachineModelResponse = mServiceRETRO2.getMachineModel();
        getMachineModelResponse.enqueue(new ApiServerCallback<GetMachineModelResponse, ApiError>(callback, mRetrofit)
        {
            @Override
            public void onResponse(Call<GetMachineModelResponse> call, retrofit2.Response<GetMachineModelResponse> response)
            {
                if (response.isSuccessful() && response.body().isUnauthorized())
                super.onResponse(call, response);

            }

            @Override
            public void onFailure(Call<GetMachineModelResponse> call, Throwable t)
            {
                super.onFailure(call, t);
            }
        });

//        mService.getMachineModel(new Callback<GetMachineModelResponse>()
//        {
//            @Override
//            public void success(GetMachineModelResponse getMachineModelResponse, Response response)
//            {
//                if (getMachineModelResponse.isUnauthorized())
//                {
//                    NvrCommunicationError nvrCommunicationError = new NvrCommunicationError("unauthorized");
//                    nvrCommunicationError.setStatusCode(CommunicationError.STATUS_UNAUTHORIZED);
//                    callback.error(nvrCommunicationError);
//                    return;
//                }
//
//                callback.success(getMachineModelResponse.getMachineModel());
//            }
//
//            @Override
//            public void failure(RetrofitError error)
//            {
//                callback.error(new NvrCommunicationError(error));
//            }
//        });
    }

    public void checkNewFirmware(final RequestListener<CheckFirmwareResponse> callback)
    {
        mServiceRETRO2.checkNewFirmware().enqueue(new retrofit2.Callback<CheckFirmwareResponse>()
        {
            @Override
            public void onResponse(Call<CheckFirmwareResponse> call, retrofit2.Response<CheckFirmwareResponse> response)
            {
                sendCallback(response, callback);
            }

            @Override
            public void onFailure(Call<CheckFirmwareResponse> call, Throwable t)
            {
                sendErrorCallback(callback);
            }
        });


//        mService.checkNewFirmware(new Callback<CheckFirmwareResponse>()
//        {
//            @Override
//            public void success(CheckFirmwareResponse checkFirmwareResponse, Response response)
//            {
//                callback.success(checkFirmwareResponse);
//            }
//
//            @Override
//            public void failure(RetrofitError error)
//            {
//                callback.error(new NvrCommunicationError(error));
//            }
//        });
    }

    public void getDayEventsApi(int channelId, String startTime, String endTime, final RequestListener<DayEventsListResponseBody> callback)
    {
        int channelMask = 1 << (channelId - 1);
        ZLogger.d(LOG_TAG, "getDayEventsApi(), channel mask: " + channelMask + ", start time " + startTime + ", endTime: " + endTime);

        mServiceRETRO2.getDayEvents(new DayEventsRequest(channelMask, startTime, endTime)).enqueue(new retrofit2.Callback<DayEventsListResponseBody>()
        {
            @Override
            public void onResponse(Call<DayEventsListResponseBody> call, retrofit2.Response<DayEventsListResponseBody> response)
            {
                sendCallback(response, callback);
                ZLogger.d(LOG_TAG, "success(), day: " + response.body());
            }

            @Override
            public void onFailure(Call<DayEventsListResponseBody> call, Throwable t)
            {
                sendErrorCallback(callback);
            }
        });



//        mService.getDayEvents(new DayEventsRequest(channelMask, startTime, endTime), new Callback<DayEventsListResponseBody>()
//        {
//            @Override
//            public void success(DayEventsListResponseBody playbacksResponse, Response response)
//            {
//                ZLogger.d(LOG_TAG, "success(), day: " + playbacksResponse);
//                callback.success(playbacksResponse);
//            }
//
//            @Override
//            public void failure(RetrofitError error)
//            {
//                callback.error(new NvrCommunicationError(error));
//            }
//        });
    }

    public void getPlaybackShortApi(int channelId, String startTime, String endTime, final RequestListener<ArrayList<ShortPlaybackResponse.ShortPlaybackInfo>> callback)
    {
        ArrayList<ShortPlaybackResponse.ShortPlaybackInfo> playbacksList = new ArrayList<>();
        getPlaybackShortApi(channelId, startTime, endTime, 0, 1000, playbacksList, callback);
    }

    private void getPlaybackShortApi(final int channelId, final String startTime, final String endTime, final int requestIndex, final int count, final ArrayList<ShortPlaybackResponse.ShortPlaybackInfo> result, final RequestListener<ArrayList<ShortPlaybackResponse.ShortPlaybackInfo>> callback)
    {
        ZLogger.d(LOG_TAG, "getPlaybackShortApi(), channel: " + channelId + ", index:" + requestIndex);
        mServiceRETRO2.getPlaybacksShortApi(new PlaybackShortRequest(channelId - 1, startTime, endTime, requestIndex, count)).enqueue(new retrofit2.Callback<ShortPlaybackResponse>()
        {
            @Override
            public void onResponse(Call<ShortPlaybackResponse> call, retrofit2.Response<ShortPlaybackResponse> response)
            {
                if (response.isSuccessful())
                {
                    ZLogger.d(LOG_TAG, "getPlaybackShortApi() success(), ");
                    if (response.body() == null || response.body().getRecordings() == null)
                    {
                        ZLogger.d(LOG_TAG, "getPlaybackShortApi() - success(), but response null");
                        callback.success(result);
                        return;
                    }

                    List<ShortPlaybackResponse.ShortPlaybackInfo> playbacksList = response.body().getRecordings();
                    ZLogger.d(LOG_TAG, "getPlaybackShortApi() - success(), got " + playbacksList.size() + " events");
                    result.addAll(playbacksList);
                    if (playbacksList.size() == count)
                    {
                        ZLogger.d(LOG_TAG, "getPlaybackShortApi() success(), got evnet as requested try to get more");
                        getPlaybackShortApi(channelId, startTime, endTime, requestIndex + count, count, result, callback);
                        return;
                    }
                    ZLogger.d(LOG_TAG, "getPlaybackShortApi success(), no more events");
                    callback.success(result);
                }
                else
                {
                    callback.error(new NvrCommunicationError(response.message(), response.code()));
                }
            }

            @Override
            public void onFailure(Call<ShortPlaybackResponse> call, Throwable t)
            {
                if (!result.isEmpty())
                {
                    ZLogger.e(LOG_TAG, "getPlaybackShortApi - failure(), but has some recordings " + result.size());
                    callback.success(result);
                    return;
                }
                ZLogger.e(LOG_TAG, "getPlaybackShortApi failure(), . without recordings");
                sendErrorCallback(callback);
            }
        });



//        mService.getPlaybacksShortApi(new PlaybackShortRequest(channelId - 1, startTime, endTime, requestIndex, count), new Callback<ShortPlaybackResponse>()
//        {
//            @Override
//            public void success(ShortPlaybackResponse playbacksResponse, Response response)
//            {
//                ZLogger.d(LOG_TAG, "getPlaybackShortApi() success(), ");
//                if (playbacksResponse == null || playbacksResponse.getRecordings() == null)
//                {
//                    ZLogger.d(LOG_TAG, "getPlaybackShortApi() - success(), but response null");
//                    callback.success(result);
//                    return;
//                }
//
//                List<ShortPlaybackResponse.ShortPlaybackInfo> playbacksList = playbacksResponse.getRecordings();
//                ZLogger.d(LOG_TAG, "getPlaybackShortApi() - success(), got " + playbacksList.size() + " events");
//                result.addAll(playbacksList);
//                if (playbacksList.size() == count)
//                {
//                    ZLogger.d(LOG_TAG, "getPlaybackShortApi() success(), got evnet as requested try to get more");
//                    getPlaybackShortApi(channelId, startTime, endTime, requestIndex + count, count, result, callback);
//                    return;
//                }
//                ZLogger.d(LOG_TAG, "getPlaybackShortApi success(), no more events");
//                callback.success(result);
//            }
//
//            @Override
//            public void failure(RetrofitError error)
//            {
//                if (!result.isEmpty())
//                {
//                    ZLogger.e(LOG_TAG, "getPlaybackShortApi - failure(), but has some recordings " + result.size());
//                    callback.success(result);
//                    return;
//                }
//                ZLogger.e(LOG_TAG, "getPlaybackShortApi failure(), . without recordings");
//                callback.error(new NvrCommunicationError(error));
//            }
//        });
    }

    public void getDevicePermissions(String userName, final RequestListener<UserManagerResponse> callback)
    {
        UserInfoRequest userInfoRequest = new UserInfoRequest();
        userInfoRequest.setName(userName);
        mServiceRETRO2.getDevicePermissions(userInfoRequest).enqueue(new retrofit2.Callback<UserManagerResponse>()
        {
            @Override
            public void onResponse(Call<UserManagerResponse> call, retrofit2.Response<UserManagerResponse> response)
            {
                if (callback != null)
                {
                    sendCallback(response, callback);
                }
            }

            @Override
            public void onFailure(Call<UserManagerResponse> call, Throwable t)
            {
                sendErrorCallback(callback);
            }
        });

//        mService.getDevicePermissions(userInfoRequest, new Callback<UserManagerResponse>()
//        {
//
//            @Override
//            public void success(UserManagerResponse userManagerResponse, Response response)
//            {
//                if (callback != null)
//                {
//                    callback.success(userManagerResponse);
//                }
//            }
//
//            @Override
//            public void failure(RetrofitError error)
//            {
//                callback.error(new NvrCommunicationError(error));
//            }
//        });
    }

    public void setDeviceCredentials(String userName, String oldPassword, String newPassword, final RequestListener<UserManagerResponse> callback)
    {
        SetDeviceCredentialsRequest setDeviceCredentialsRequest = new SetDeviceCredentialsRequest(userName, oldPassword, newPassword);
        mService.setDeviceCredentials(setDeviceCredentialsRequest, new retrofit.Callback<UserManagerResponse>()
        {
            @Override
            public void success(UserManagerResponse userManagerResponse, Response response)
            {
                if (callback != null)
                {
                    callback.success(userManagerResponse);
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }

    public void sendPtzCommand(PtzCommand command, int channelId, final RequestListener<Boolean> callback)
    {
        PtzCommandRequest request = new PtzCommandRequest();
        request.setChannel(channelId - 1);
        request.setAction(command.getAction());
        request.setDirection(command.getDirection());
        request.setName(command.getCommand());
        request.setSpeed(4);
        ZLogger.d(LOG_TAG, "sendPtzCommand(), " + request);
        mService.sendPtzCommand(request, new Callback<BasicGsonResponse>()
        {
            @Override
            public void success(BasicGsonResponse basicGsonResponse, Response response)
            {
                ZLogger.d(LOG_TAG, "sendPtzCommand - success(), -  " + basicGsonResponse.isOk());
                callback.success(basicGsonResponse.isOk());
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }

    public void updateChannelSettings(Device device, boolean isPushNotificationAllowed, final RequestListener<Boolean> callback)
    {
        DeviceNotificationSettingsRequest deviceNotificationSettingsRequest = new DeviceNotificationSettingsRequest(device.getChannels().size(), isPushNotificationAllowed);
        mService.updateDeviceNotificationSettings(deviceNotificationSettingsRequest, new Callback<DeviceNotificationSettingsRequest>()
        {
            @Override
            public void success(DeviceNotificationSettingsRequest notificationSettingsRequest, Response response)
            {
                callback.success(true);
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }

    public void loadDeviceNotificationSettings(final RequestListener<Boolean> callback)
    {
        mService.loadDeviceNotificationSettings(new GetDeviceNotificationSettingsRequest(), new Callback<DeviceNotificationSettingsRequest>()
        {
            @Override
            public void success(DeviceNotificationSettingsRequest notificationSettingsResponse, Response response)
            {
                callback.success(notificationSettingsResponse.isEnabled());
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }

    public void upgradeFirmware(final RequestListener<Boolean> callback)
    {
        ActionRequest request = new ActionRequest();
        request.setAction("UPGRADE_FW");
        mService.upgradeFirmware(request, new Callback<BasicGsonResponse>()
        {
            @Override
            public void success(BasicGsonResponse basicGsonResponse, Response response)
            {
                callback.success(basicGsonResponse.isOk());
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }

    public void saveChannelConfiguration(int channelId, String name, boolean audioState, boolean maskingTrigger, boolean motionTrigger, boolean outOfFoucsTrigger, final RequestListener<Void> callback)
    {
        ArrayList<ChannelSettingsInformationRequest> channels = new ArrayList<>();

        ChannelSettingsInformationRequest channel = new ChannelSettingsInformationRequest(channelId - 1, name, motionTrigger, maskingTrigger, outOfFoucsTrigger, audioState);
        channels.add(channel);
        SetChannelConfigRequest setChannelConfigRequest = new SetChannelConfigRequest(channels);

        mService.setChannelConfiguration(setChannelConfigRequest, new Callback<SetConfigResponse>()
        {
            @Override
            public void success(SetConfigResponse setConfigResponse, Response response)
            {
                ZLogger.d(LOG_TAG, "success(), ");
                if (setConfigResponse == null)
                {
                    callback.error(new GenericError("Response is null"));
                }
                else if (!setConfigResponse.isSuccess())
                {
                    callback.error(new NvrCommunicationError(setConfigResponse.getError()));
                }
                else
                {
                    callback.success(null);
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                if (callback != null)
                {
                    callback.error(new NvrCommunicationError(error));
                }
                ZLogger.e(LOG_TAG, "failure(), ." + error);
            }
        });
    }

    public void getChannelConfiguration(final NvrChannelModel channel, final RequestListener<ChannelConfigResponse> callback)
    {
        int channelMask = 1 << (channel.getChannelId() - 1);
        ChannelConfigRequest channelConfigRequest = new ChannelConfigRequest(channelMask);
        mService.getChannelConfiguration(channelConfigRequest, new Callback<ChannelConfigResponse>()
        {
            @Override
            public void success(ChannelConfigResponse channelConfigResponse, Response response)
            {
                if (callback != null)
                {
                    callback.success(channelConfigResponse);
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                if (callback != null)
                {
                    callback.error(new NvrCommunicationError(error));
                }
            }
        });
    }

    public void saveDeviceConfiguration(String deviceName, Boolean diskErrorTrigger, Boolean noDiskTrigger, final RequestListener<Void> callback)
    {
        SetDeviceConfigRequest setDeviceConfigRequest = new SetDeviceConfigRequest(deviceName, noDiskTrigger, diskErrorTrigger);
        mService.setDeviceConfiguration(setDeviceConfigRequest, new Callback<SetConfigResponse>()
        {
            @Override
            public void success(SetConfigResponse setConfigResponse, Response response)
            {
                ZLogger.d(LOG_TAG, "success(), ");
                if (setConfigResponse == null)
                {
                    callback.error(new GenericError("Response is null"));
                }
                else if (!setConfigResponse.isSuccess())
                {
                    callback.error(new NvrCommunicationError(setConfigResponse.getError()));
                }
                else
                {
                    callback.success(null);
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                if (callback != null)
                {
                    callback.error(new NvrCommunicationError(error));
                }
                ZLogger.e(LOG_TAG, "failure(), ." + error);
            }
        });
    }

    public void saveDeviceNameConfiguration(String deviceName, final RequestListener<Void> callback)
    {
        SetDeviceNameConfigRequest setDeviceNameConfigRequest = new SetDeviceNameConfigRequest(deviceName);
        mService.setDeviceNameConfiguration(setDeviceNameConfigRequest, new Callback<SetConfigResponse>()
        {
            @Override
            public void success(SetConfigResponse setConfigResponse, Response response)
            {
                ZLogger.d(LOG_TAG, "success(), ");
                if (setConfigResponse == null)
                {
                    callback.error(new GenericError("Response is null"));
                }
                else if (!setConfigResponse.isSuccess())
                {
                    callback.error(new NvrCommunicationError(setConfigResponse.getError()));
                }
                else
                {
                    callback.success(null);
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                if (callback != null)
                {
                    callback.error(new NvrCommunicationError(error));
                }
                ZLogger.e(LOG_TAG, "failure(), ." + error);
            }
        });
    }

    public void getDeviceConfiguration(final RequestListener<DeviceConfigResponse> callback)
    {
        ActionRequest actionRequest = new GetDeviceConfigurationRequest();
        mService.getDeviceConfiguration(actionRequest, new Callback<DeviceConfigResponse>()
        {
            @Override
            public void success(DeviceConfigResponse deviceConfigResponse, Response response)
            {
                if (callback != null)
                {
                    callback.success(deviceConfigResponse);
                }
            }

            @Override
            public void failure(RetrofitError error)
            {
                if (callback != null)
                {
                    callback.error(new NvrCommunicationError(error));
                }
            }
        });
    }

    public void getDeviceStatus(final RequestListener<DeviceStatusResponse> callback)
    {
        mService.getDeviceStatus(new Callback<DeviceStatusResponse>()
        {

            @Override
            public void success(DeviceStatusResponse deviceStatusResponse, Response response)
            {
                callback.success(deviceStatusResponse);
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }

    public void setPrivacyModeEnabled(int channelId, boolean isEnabled, final RequestListener<Void> callback)
    {
        PrivacyModeRequest.PrivacyActions action = isEnabled ? PrivacyModeRequest.PrivacyActions.ENTER_PRIVACY : PrivacyModeRequest.PrivacyActions.LEAVE_PRIVACY;
        int channelMask = 1 << (channelId - 1);

        PrivacyModeRequest request = new PrivacyModeRequest(String.valueOf(channelMask), action);
        mService.setPrivacyModeEnabled(request, new Callback<RemoteControlResponse>()
        {
            @Override
            public void success(RemoteControlResponse remoteControlResponse, Response response)
            {
                callback.success(null);
            }

            @Override
            public void failure(RetrofitError error)
            {
                callback.error(new NvrCommunicationError(error));
            }
        });
    }
}
