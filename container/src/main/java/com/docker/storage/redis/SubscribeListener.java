package com.docker.storage.redis;

import chat.main.ServerStart;
import com.docker.utils.GroovyCloudBean;
import redis.clients.jedis.JedisPubSub;

/**
 * Created by lick on 2020/2/12.
 * Description：
 */
public class SubscribeListener extends JedisPubSub {
    private final String TAG = SubscribeListener.class.getSimpleName();
    private static volatile SubscribeListener instance;
    private RedisSubscribeHandler redisSubscribeHandler = (RedisSubscribeHandler) GroovyCloudBean.getBean(GroovyCloudBean.REDISSUBSCRIBEHANDLER);
    @Override
    public void onMessage(String channel, String message) {
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        ServerStart.getInstance().getThreadPool().execute(() -> redisSubscribeHandler.redisCallback(message));
    }

//
//    @Override
//    public void onSubscribe(String channel, int subscribedChannels) {
//        super.onSubscribe(channel, subscribedChannels);
//    }
//
//    @Override
//    public void onUnsubscribe(String channel, int subscribedChannels) {
//        super.onUnsubscribe(channel, subscribedChannels);
//    }
//
//    @Override
//    public void onPUnsubscribe(String pattern, int subscribedChannels) {
//        super.onPUnsubscribe(pattern, subscribedChannels);
//    }
//
//    @Override
//    public void onPSubscribe(String pattern, int subscribedChannels) {
//        super.onPSubscribe(pattern, subscribedChannels);
//    }
//
//    @Override
//    public void onPong(String pattern) {
//        super.onPong(pattern);
//    }
//
    @Override
    public void unsubscribe() {
        super.unsubscribe();
    }
//
    @Override
    public void unsubscribe(String... channels) {
        super.unsubscribe(channels);
    }
//
//    @Override
//    public void subscribe(String... channels) {
//        super.subscribe(channels);
//    }
//
//    @Override
//    public void psubscribe(String... patterns) {
//        super.psubscribe(patterns);
//    }
//
    @Override
    public void punsubscribe() {
        super.punsubscribe();
    }
    @Override
    public void punsubscribe(String... patterns) {
        super.punsubscribe(patterns);
    }
//
//    @Override
//    public void ping() {
//        super.ping();
//    }
//
//    @Override
//    public boolean isSubscribed() {
//        return super.isSubscribed();
//    }
//
//    @Override
//    public void proceedWithPatterns(Client client, String... patterns) {
//        super.proceedWithPatterns(client, patterns);
//    }
//
//    @Override
//    public void proceed(Client client, String... channels) {
//        super.proceed(client, channels);
//    }
//
//    @Override
//    public int getSubscribedChannels() {
//        return super.getSubscribedChannels();
//    }
    public static SubscribeListener getInstance(){
        if(instance == null){
            synchronized (SubscribeListener.class){
                if(instance == null){
                    instance = new SubscribeListener();
                }
            }
        }
        return instance;
    }
}
