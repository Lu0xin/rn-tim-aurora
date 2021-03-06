package com.timaurora.tim.helper;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.tencent.TIMConversation;
import com.tencent.TIMConversationType;
import com.tencent.TIMElem;
import com.tencent.TIMImageElem;
import com.tencent.TIMManager;
import com.tencent.TIMMessage;
import com.tencent.TIMSnapshot;
import com.tencent.TIMSoundElem;
import com.tencent.TIMTextElem;
import com.tencent.TIMValueCallBack;
import com.tencent.TIMVideo;
import com.tencent.TIMVideoElem;
import com.timaurora.tim.PromiseHelper;
import com.timaurora.tim.callback.TIMValueCb;
import com.timaurora.tim.util.FileUtil;
import com.timaurora.tim.util.MediaUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ChatHelper {
    private static TIMConversation conversation;
    public static TIMMessage lastMsg;
    private static String tag = ChatHelper.class.getSimpleName();
    private static void createConversation(TIMConversationType type,String identifier){
        conversation = TIMManager.getInstance().getConversation(type,identifier);
    }
    private static void sendMsg(TIMElem elem,String tagName, Promise promise){
        TIMMessage msgElem = new TIMMessage();
        if(msgElem.addElement(elem)!=0){
            String msg = tagName + " addElement failed";
            Log.d(tag, msg);
            promise.reject(PromiseHelper.createErrReject(msg));
        }
        conversation.sendMessage(msgElem, new TIMValueCb<TIMMessage>(tagName,promise));
    }
    public static void chatWithSingle(String identifier){
        lastMsg = null;
        createConversation(TIMConversationType.C2C,identifier);
    }
    public static void chatWithGroup(String identifier){
        lastMsg = null;
        createConversation(TIMConversationType.Group,identifier);
    }
    public static void sendText(String text, Promise promise){
        //添加文本内容
        TIMTextElem elem = new TIMTextElem();
        elem.setText(text);
        sendMsg(elem,"sendText",promise);
    }
    public static void sendImg(String path,Promise promise){
        //添加图片
        TIMImageElem elem = new TIMImageElem();
        elem.setPath(path);
        sendMsg(elem,"sendImg",promise);
    }
    public static void sendSound(String path,Integer duration,Promise promise){
        //添加语音
        TIMSoundElem elem = new TIMSoundElem();
        elem.setPath(path); //填写语音文件路径
        elem.setDuration(duration);  //填写语音时长
        sendMsg(elem,"sendSound",promise);
    }
    public static void sendVideo(String path,Promise promise){
        //构建一个视频 Elem
        TIMVideoElem elem = new TIMVideoElem();
        //添加视频内容, 其中的 FileUtil 类可以参考 Demo 中的实现
        TIMVideo video = new TIMVideo();
        video.setType("MP4");
        video.setDuaration(MediaUtil.getInstance().getDuration(path));
        elem.setVideo(video);
        elem.setVideoPath(path);
        //添加视频截图, 其中的 FileUtil 类可以参考 Demo 中的实现
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
        elem.setSnapshotPath(FileUtil.createFile(thumb, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())));
        TIMSnapshot snapshot = new TIMSnapshot();
        snapshot.setType("PNG");
        snapshot.setHeight(thumb.getHeight());
        snapshot.setWidth(thumb.getWidth());
        elem.setSnapshot(snapshot);
        sendMsg(elem,"sendVideo",promise);
    }
    public static void getHistoryMsg(Integer count,Promise promise){
        //获取此会话的消息
        conversation.getMessage(count, //获取此会话最近的 10 条消息
                lastMsg, //不指定从哪条消息开始获取 - 等同于从最新的消息开始往前
                new TIMValueCb("getMessage",promise));
    }
}
