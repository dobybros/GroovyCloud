// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: Mobile.proto

// This CPP symbol can be defined to use imports that match up to the framework
// imports needed when using CocoaPods.
#if !defined(GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS)
 #define GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS 0
#endif

#if GPB_USE_PROTOBUF_FRAMEWORK_IMPORTS
 #import <Protobuf/GPBProtocolBuffers.h>
#else
 #import "GPBProtocolBuffers.h"
#endif

#if GOOGLE_PROTOBUF_OBJC_VERSION < 30002
#error This file was generated by a newer version of protoc which is incompatible with your Protocol Buffer library sources.
#endif
#if 30002 < GOOGLE_PROTOBUF_OBJC_MIN_SUPPORTED_VERSION
#error This file was generated by an older version of protoc which is incompatible with your Protocol Buffer library sources.
#endif

// @@protoc_insertion_point(imports)

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"

CF_EXTERN_C_BEGIN

NS_ASSUME_NONNULL_BEGIN

#pragma mark - MobileRoot

/**
 * Exposes the extension registry for this file.
 *
 * The base class provides:
 * @code
 *   + (GPBExtensionRegistry *)extensionRegistry;
 * @endcode
 * which is a @c GPBExtensionRegistry that includes all the extensions defined by
 * this file and all files that it depends on.
 **/
@interface MobileRoot : GPBRootObject
@end

#pragma mark - Identity

typedef GPB_ENUM(Identity_FieldNumber) {
  Identity_FieldNumber_Id_p = 1,
  Identity_FieldNumber_SessionId = 2,
  Identity_FieldNumber_UserId = 3,
  Identity_FieldNumber_Service = 4,
  Identity_FieldNumber_Key = 5,
  Identity_FieldNumber_AppId = 6,
  Identity_FieldNumber_Code = 7,
  Identity_FieldNumber_Terminal = 8,
  Identity_FieldNumber_DeviceToken = 9,
  Identity_FieldNumber_SdkVersion = 10,
};

/**
 * 从客户端到服务器端的数据
 **/
@interface Identity : GPBMessage

@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** 业务上的用户会话ID， 和业务服务器通信时会带上的。 在纯推送业务环境中， 此值为空。 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *sessionId;

/** 业务上的用户ID， 和业务服务器通信时会带上的。 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *userId;

/** 如果一个通道需要支持的多种业务， OnlineUserManager 1-* > OnlineUser 1-* > OnlineServiceUser。 为每个Service都需要发送Identity数据注册 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *service;

/** API key， 用于我们做权限控制 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *key;

/** 应用ID */
@property(nonatomic, readwrite, copy, null_resettable) NSString *appId;

/** 通过登陆服务器分配得到的登陆代码。 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *code;

@property(nonatomic, readwrite) int32_t terminal;

@property(nonatomic, readwrite, copy, null_resettable) NSString *deviceToken;

@property(nonatomic, readwrite) int32_t sdkVersion;

@end

#pragma mark - IncomingMessage

typedef GPB_ENUM(IncomingMessage_FieldNumber) {
  IncomingMessage_FieldNumber_Id_p = 1,
  IncomingMessage_FieldNumber_Server = 2,
  IncomingMessage_FieldNumber_UserIdsArray = 3,
  IncomingMessage_FieldNumber_Service = 4,
  IncomingMessage_FieldNumber_ContentType = 5,
  IncomingMessage_FieldNumber_ContentEncode = 6,
  IncomingMessage_FieldNumber_Content = 7,
  IncomingMessage_FieldNumber_UserService = 8,
  IncomingMessage_FieldNumber_NotSaveOfflineMsg = 9,
};

@interface IncomingMessage : GPBMessage

@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** singlechat/\*, singlechat/server1 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *server;

/** 消息要发送给的用户ID */
@property(nonatomic, readwrite, strong, null_resettable) NSMutableArray<NSString*> *userIdsArray;
/** The number of items in @c userIdsArray without causing the array to be created. */
@property(nonatomic, readonly) NSUInteger userIdsArray_Count;

/** 当该通道支持多种services的时候， 需要指定这条消息是属于哪个service的。 如果该通道只支持一个service， 则可以为空 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *service;

/** 业务数据的类型 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *contentType;

/** 业务数据的编码方式， 如果编码为CHUNKED=100， 则content里是包的总长度 */
@property(nonatomic, readwrite) int32_t contentEncode;

/** 业务数据的二进制 */
@property(nonatomic, readwrite, copy, null_resettable) NSData *content;

/** 消息要发送到用户的所属service */
@property(nonatomic, readwrite, copy, null_resettable) NSString *userService;

/** 是否需要存储离线消息 */
@property(nonatomic, readwrite) BOOL notSaveOfflineMsg;

@end

#pragma mark - Acknowledge

typedef GPB_ENUM(Acknowledge_FieldNumber) {
  Acknowledge_FieldNumber_Id_p = 1,
  Acknowledge_FieldNumber_MsgIdsArray = 2,
  Acknowledge_FieldNumber_Service = 3,
};

@interface Acknowledge : GPBMessage

/** 此处可以不用 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** 已收到的消息ID */
@property(nonatomic, readwrite, strong, null_resettable) NSMutableArray<NSString*> *msgIdsArray;
/** The number of items in @c msgIdsArray without causing the array to be created. */
@property(nonatomic, readonly) NSUInteger msgIdsArray_Count;

/** 来自于那个服务 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *service;

@end

#pragma mark - Ping

typedef GPB_ENUM(Ping_FieldNumber) {
  Ping_FieldNumber_Id_p = 1,
};

@interface Ping : GPBMessage

/** 此处可以不用 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

@end

#pragma mark - Result

typedef GPB_ENUM(Result_FieldNumber) {
  Result_FieldNumber_Code = 1,
  Result_FieldNumber_Description_p = 2,
  Result_FieldNumber_ForId = 3,
  Result_FieldNumber_Time = 4,
  Result_FieldNumber_ServerId = 5,
  Result_FieldNumber_ContentEncode = 6,
  Result_FieldNumber_Content = 7,
};

@interface Result : GPBMessage

@property(nonatomic, readwrite) int32_t code;

@property(nonatomic, readwrite, copy, null_resettable) NSString *description_p;

/** 对应Message里的id， 为客户端id */
@property(nonatomic, readwrite, copy, null_resettable) NSString *forId;

/** 返回结果的服务器时间 */
@property(nonatomic, readwrite) int64_t time;

/** 处理这件事所对应的服务器ID， 可能没有。 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *serverId;

/** 业务数据的编码方式， 如果编码为CHUNKED=100， 则content里是包的总长度 */
@property(nonatomic, readwrite) int32_t contentEncode;

/** 业务数据的二进制 */
@property(nonatomic, readwrite, copy, null_resettable) NSData *content;

@end

#pragma mark - OutgoingMessage

typedef GPB_ENUM(OutgoingMessage_FieldNumber) {
  OutgoingMessage_FieldNumber_Id_p = 1,
  OutgoingMessage_FieldNumber_UserId = 2,
  OutgoingMessage_FieldNumber_Service = 3,
  OutgoingMessage_FieldNumber_Time = 4,
  OutgoingMessage_FieldNumber_ContentType = 5,
  OutgoingMessage_FieldNumber_ContentEncode = 6,
  OutgoingMessage_FieldNumber_Content = 7,
  OutgoingMessage_FieldNumber_NeedAck = 8,
};

/**
 * 收到消息之后需要发送Acknowledge， 标记已读。
 **/
@interface OutgoingMessage : GPBMessage

@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** 发送方的用户Id */
@property(nonatomic, readwrite, copy, null_resettable) NSString *userId;

/** 该消息来源于那个service */
@property(nonatomic, readwrite, copy, null_resettable) NSString *service;

/** 该消息的发送时间 */
@property(nonatomic, readwrite) int64_t time;

/** 业务数据的类型 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *contentType;

/** 业务数据的编码方式， 如果编码为CHUNKED=100， 则content里是包的总长度 */
@property(nonatomic, readwrite) int32_t contentEncode;

/** 业务数据的二进制 */
@property(nonatomic, readwrite, copy, null_resettable) NSData *content;

/** 默认为true， 在业务需要的情况下为false， 会导致这条下行消息不做离线存储。 */
@property(nonatomic, readwrite) BOOL needAck;

@end

#pragma mark - IncomingData

typedef GPB_ENUM(IncomingData_FieldNumber) {
  IncomingData_FieldNumber_Id_p = 1,
  IncomingData_FieldNumber_Service = 2,
  IncomingData_FieldNumber_ContentType = 3,
  IncomingData_FieldNumber_ContentEncode = 4,
  IncomingData_FieldNumber_Content = 5,
};

@interface IncomingData : GPBMessage

@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** 业务数据的service */
@property(nonatomic, readwrite, copy, null_resettable) NSString *service;

/** 业务数据的类型 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *contentType;

/** 业务数据的编码方式， 如果编码为CHUNKED=100， 则content里是包的总长度 */
@property(nonatomic, readwrite) int32_t contentEncode;

/** 业务数据的二进制 */
@property(nonatomic, readwrite, copy, null_resettable) NSData *content;

@end

#pragma mark - OutgoingData

typedef GPB_ENUM(OutgoingData_FieldNumber) {
  OutgoingData_FieldNumber_Id_p = 1,
  OutgoingData_FieldNumber_Time = 2,
  OutgoingData_FieldNumber_Service = 3,
  OutgoingData_FieldNumber_ContentType = 4,
  OutgoingData_FieldNumber_ContentEncode = 5,
  OutgoingData_FieldNumber_Content = 6,
  OutgoingData_FieldNumber_NeedAck = 7,
};

@interface OutgoingData : GPBMessage

@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** 该消息的发送时间 */
@property(nonatomic, readwrite) int64_t time;

/** 业务数据的service */
@property(nonatomic, readwrite, copy, null_resettable) NSString *service;

/** 业务数据的类型 */
@property(nonatomic, readwrite, copy, null_resettable) NSString *contentType;

/** 业务数据的编码方式， 如果编码为CHUNKED=100， 则content里是包的总长度 */
@property(nonatomic, readwrite) int32_t contentEncode;

/** 业务数据的二进制 */
@property(nonatomic, readwrite, copy, null_resettable) NSData *content;

/** 默认为true， 在业务需要的情况下为false， 会导致这条下行消息不做离线存储。(暂时不确定要不要) */
@property(nonatomic, readwrite) BOOL needAck;

@end

#pragma mark - Chunk

typedef GPB_ENUM(Chunk_FieldNumber) {
  Chunk_FieldNumber_Id_p = 1,
  Chunk_FieldNumber_Type = 2,
  Chunk_FieldNumber_Content = 3,
  Chunk_FieldNumber_ChunkNum = 4,
  Chunk_FieldNumber_Offset = 5,
  Chunk_FieldNumber_TotalSize = 6,
};

@interface Chunk : GPBMessage

/** 拆包数据的ID */
@property(nonatomic, readwrite, copy, null_resettable) NSString *id_p;

/** 被拆包数据的类型， 例如OutgoingMessage, IncomingData, etc */
@property(nonatomic, readwrite) int32_t type;

/** 业务数据的二进制 */
@property(nonatomic, readwrite, copy, null_resettable) NSData *content;

/** 拆包的编号 */
@property(nonatomic, readwrite) int32_t chunkNum;

/** 拆包数据的偏移位置 */
@property(nonatomic, readwrite) int32_t offset;

/** 整包的总大小 */
@property(nonatomic, readwrite) int32_t totalSize;

@end

NS_ASSUME_NONNULL_END

CF_EXTERN_C_END

#pragma clang diagnostic pop

// @@protoc_insertion_point(global_scope)
